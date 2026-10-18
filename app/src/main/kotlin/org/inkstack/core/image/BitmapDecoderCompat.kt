package org.inkstack.core.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.ImageDecoder
import androidx.core.graphics.createBitmap
import com.awxkee.jxlcoder.JxlCoder
import com.awxkee.jxlcoder.PreferredColorConfig
import com.awxkee.jxlcoder.ScaleMode
import okio.BufferedSource
import okio.ByteString
import okio.ByteString.Companion.toByteString
import okio.IOException
import okio.buffer
import okio.source
import org.aomedia.avif.android.AvifDecoder
import org.aomedia.avif.android.AvifDecoder.Info
import org.jetbrains.annotations.Blocking
import org.inkstack.core.util.MimeTypes
import org.inkstack.core.util.ext.MimeType
import org.inkstack.core.util.ext.printStackTraceDebug
import org.inkstack.core.util.ext.readByteBuffer
import org.inkstack.core.util.ext.toMimeTypeOrNull
import org.inkstack.parsers.util.runCatchingCancellable
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer

object BitmapDecoderCompat {

	private const val FORMAT_AVIF = "avif"
	private const val FORMAT_JXL = "jxl"

	/** A bare JPEG XL codestream. */
	private val JXL_CODESTREAM = byteArrayOf(0xFF.toByte(), 0x0A).toByteString()

	/** The signature box that opens a JPEG XL in its ISOBMFF container. */
	private val JXL_CONTAINER = byteArrayOf(
		0x00, 0x00, 0x00, 0x0C, 0x4A, 0x58, 0x4C, 0x20, 0x0D, 0x0A, 0x87.toByte(), 0x0A,
	).toByteString()

	private const val JXL_HEADER_SIZE = 12

	@Blocking
	fun decode(file: File, preferHardware: Boolean = true): Bitmap {
		// By content, not name: sites serve JPEG XL under .jpg URLs to browsers that accept it
		if (isJxl(file)) {
			return decodeJxl(file.readBytes())
		}
		return when (val format = probeMimeType(file)?.subtype) {
			FORMAT_AVIF -> file.source().buffer().use { decodeAvif(it.readByteBuffer()) }
			else -> ImageDecoder.decodeBitmap(
				ImageDecoder.createSource(file),
				DecoderConfigListener(isMutable = false, preferHardware = preferHardware),
			)
		}
	}

	@Blocking
	fun decode(stream: InputStream, type: MimeType?, isMutable: Boolean = false): Bitmap {
		val format = type?.subtype
		if (format == FORMAT_JXL) {
			return decodeJxl(stream.readBytes(), isMutable)
		}
		val byteBuffer = stream.source().buffer().use { it.readByteBuffer() }
		return when {
			format == FORMAT_AVIF || AvifDecoder.isAvifImage(byteBuffer) -> decodeAvif(byteBuffer)
			isJxl(byteBuffer) -> decodeJxl(ByteArray(byteBuffer.remaining()).also { byteBuffer.duplicate().get(it) }, isMutable)
			else -> ImageDecoder.decodeBitmap(
				ImageDecoder.createSource(byteBuffer),
				DecoderConfigListener(isMutable = isMutable, preferHardware = !isMutable),
			)
		}
	}

	/** Checks the signature without consuming it, which is what Coil's decoder factories need. */
	fun isJxl(source: BufferedSource): Boolean =
		source.rangeEquals(0, JXL_CONTAINER) || source.rangeEquals(0, JXL_CODESTREAM)

	@Blocking
	fun isJxl(file: File): Boolean = runCatchingCancellable {
		file.source().buffer().use { isJxl(it) }
	}.getOrDefault(false)

	/**
	 * Free memory to ask for before decoding [file] into an ARGB bitmap (e.g. PNG convert).
	 *
	 * Prefer pixel-bounds × 4 over file size: JPEG/WebP/JXL compress far below decoded RGBA.
	 */
	@Blocking
	fun estimateDecodedSize(file: File): Long {
		val fallback = file.length() * 2
		if (isJxl(file) && JxlSupport.isAvailable) {
			val size = runCatchingCancellable { JxlCoder.getSize(file.readBytes()) }.getOrNull()
			if (size != null) {
				return maxOf(fallback, size.width.toLong() * size.height * 4)
			}
		}
		val bounds = runCatchingCancellable {
			val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
			BitmapFactory.decodeFile(file.path, options)
			if (options.outWidth > 0 && options.outHeight > 0) {
				options.outWidth.toLong() * options.outHeight * 4
			} else {
				null
			}
		}.getOrNull()
		return bounds?.coerceAtLeast(fallback) ?: fallback
	}

	@Blocking
	fun createRegionDecoder(inoutStream: InputStream): BitmapRegionDecoder? = try {
		BitmapRegionDecoder.newInstance(inoutStream)
	} catch (e: IOException) {
		e.printStackTraceDebug()
		null
	}

	@Blocking
	fun probeMimeType(file: File): MimeType? {
		if (isJxl(file)) {
			return MimeTypes.JXL
		}
		return MimeTypes.probeMimeType(file) ?: detectBitmapType(file)
	}

	@Blocking
	private fun detectBitmapType(file: File): MimeType? = runCatchingCancellable {
		val options = BitmapFactory.Options().apply {
			inJustDecodeBounds = true
		}
		BitmapFactory.decodeFile(file.path, options)?.recycle()
		options.outMimeType?.toMimeTypeOrNull()
	}.getOrNull()

	private fun decodeAvif(bytes: ByteBuffer): Bitmap {
		val info = Info()
		if (!AvifDecoder.getInfo(bytes, bytes.remaining(), info)) {
			throw ImageDecodeException(
				null,
				FORMAT_AVIF,
				"Requested to decode byte buffer which cannot be handled by AvifDecoder",
			)
		}
		val config = if (info.depth == 8 || info.alphaPresent) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
		val bitmap = createBitmap(info.width, info.height, config)
		if (!AvifDecoder.decode(bytes, bytes.remaining(), bitmap)) {
			bitmap.recycle()
			throw ImageDecodeException(null, FORMAT_AVIF)
		}
		return bitmap
	}

	private fun decodeJxl(bytes: ByteArray, isMutable: Boolean = false): Bitmap {
		if (!JxlSupport.isAvailable) {
			throw ImageDecodeException(null, FORMAT_JXL, "JPEG XL is not supported on this device")
		}
		// 8-bit RGBA whatever the source depth: a HARDWARE or F16 bitmap cannot be re-encoded to PNG
		// by the reader's conversion fallback the same way
		val bitmap = JxlCoder.decode(bytes, PreferredColorConfig.RGBA_8888, ScaleMode.FIT)
		if (!isMutable || bitmap.isMutable) {
			return bitmap
		}
		return bitmap.copy(Bitmap.Config.ARGB_8888, true).also { bitmap.recycle() }
	}

	private fun isJxl(header: ByteString): Boolean =
		header.startsWith(JXL_CONTAINER) || header.startsWith(JXL_CODESTREAM)

	private fun isJxl(buffer: ByteBuffer): Boolean {
		val size = minOf(buffer.remaining(), JXL_HEADER_SIZE)
		val header = ByteArray(size).also { buffer.duplicate().get(it) }
		return isJxl(header.toByteString())
	}

	private class DecoderConfigListener(
		private val isMutable: Boolean,
		private val preferHardware: Boolean,
	) : ImageDecoder.OnHeaderDecodedListener {

		override fun onHeaderDecoded(
			decoder: ImageDecoder,
			info: ImageDecoder.ImageInfo,
			source: ImageDecoder.Source
		) {
			decoder.isMutableRequired = isMutable
			// HARDWARE bitmaps cannot be compressed or drawn into a Canvas; keep SOFTWARE for those paths.
			decoder.allocator = if (preferHardware && !isMutable) {
				ImageDecoder.ALLOCATOR_HARDWARE
			} else {
				ImageDecoder.ALLOCATOR_SOFTWARE
			}
		}
	}
}
