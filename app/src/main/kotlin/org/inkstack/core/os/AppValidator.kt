package org.inkstack.core.os

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import org.inkstack.parsers.util.suspendlazy.suspendLazy
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Verifies the running APK is signed with the Inkstack release certificate.
 *
 * Debug / nightly builds (different applicationId or unsigned-for-store certs) fail this check
 * on purpose so updater and integrity-gated paths stay release-only.
 */
@Singleton
class AppValidator @Inject constructor(
	@ApplicationContext private val context: Context,
) {
	val isOriginalApp = suspendLazy(Dispatchers.Default) {
		val certificates = mapOf(TRUSTED_CERT_SHA256.hexToByteArray() to PackageManager.CERT_INPUT_SHA256)
		PackageInfoCompat.hasSignatures(
			context.packageManager,
			context.packageName,
			certificates,
			false,
		)
	}

	private companion object {
		/** Lowercase hex SHA-256 of the Inkstack release signing cert (`keystore/inkstack-release.jks`). */
		private const val TRUSTED_CERT_SHA256 =
			"5aeeee856e5bcac2cc9872a1fd8ca84974a29b8d30d75661248fd9f4b6b91315"
	}
}

private fun String.hexToByteArray(): ByteArray {
	require(length % 2 == 0) { "Hex string must have an even length" }
	return ByteArray(length / 2) { index ->
		val i = index * 2
		substring(i, i + 2).toInt(16).toByte()
	}
}
