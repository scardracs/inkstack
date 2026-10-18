package org.inkstack.core.util

import android.webkit.MimeTypeMap
import org.jetbrains.annotations.Blocking
import org.inkstack.core.util.ext.MimeType
import org.inkstack.core.util.ext.toMimeTypeOrNull
import org.inkstack.parsers.util.nullIfEmpty
import org.inkstack.parsers.util.removeSuffix
import org.inkstack.parsers.util.runCatchingCancellable
import java.io.File
import java.nio.file.Files
import coil3.util.MimeTypeMap as CoilMimeTypeMap

object MimeTypes {

	// Neither Coil's table nor the platform's knows JPEG XL. Without this, .jxl pages are not
	// recognised as images in local archives, and downloaded ones are saved without an extension.
	private const val EXTENSION_JXL = "jxl"
	val JXL = MimeType("image/jxl")

	fun getMimeTypeFromExtension(fileName: String): MimeType? {
		val extension = getNormalizedExtension(fileName) ?: return null
		if (extension == EXTENSION_JXL) {
			return JXL
		}
		return CoilMimeTypeMap.getMimeTypeFromExtension(extension)?.toMimeTypeOrNull()
	}

	fun getMimeTypeFromUrl(url: String): MimeType? {
		val fileName = url.substringBefore('#').substringBefore('?').substringAfterLast('/')
		if (getNormalizedExtension(fileName) == EXTENSION_JXL) {
			return JXL
		}
		return CoilMimeTypeMap.getMimeTypeFromUrl(url)?.toMimeTypeOrNull()
	}

	fun getExtension(mimeType: MimeType?): String? {
		if (mimeType == JXL) {
			return EXTENSION_JXL
		}
		return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType?.toString() ?: return null)?.nullIfEmpty()
	}

	@Blocking
	fun probeMimeType(file: File): MimeType? {
		runCatchingCancellable {
			Files.probeContentType(file.toPath())?.toMimeTypeOrNull()
		}.getOrNull()?.let { return it }
		return getMimeTypeFromExtension(file.name)
	}

	fun getNormalizedExtension(name: String): String? = name
		.lowercase()
		.removeSuffix('~')
		.removeSuffix(".tmp")
		.substringAfterLast('.', "")
		.takeIf { it.length in 2..5 }
}
