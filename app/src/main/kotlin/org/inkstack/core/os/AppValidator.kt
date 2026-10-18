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
 * Verifies the running APK is signed with a known Inkstack release certificate.
 *
 * [TRUSTED_CERT_SHA256] is empty until a release keystore is defined; until then every
 * build is treated as trusted so update/crash reporting paths are not falsely gated.
 */
@Singleton
class AppValidator @Inject constructor(
	@ApplicationContext private val context: Context,
) {
	val isOriginalApp = suspendLazy(Dispatchers.Default) {
		val trusted = TRUSTED_CERT_SHA256
		if (trusted.isEmpty()) {
			true
		} else {
			val certificates = mapOf(trusted.hexToByteArray() to PackageManager.CERT_INPUT_SHA256)
			PackageInfoCompat.hasSignatures(
				context.packageManager,
				context.packageName,
				certificates,
				false,
			)
		}
	}

	private companion object {
		/** Lowercase hex SHA-256 of the Inkstack release signing cert; empty = trust all. */
		private const val TRUSTED_CERT_SHA256 = ""
	}
}
