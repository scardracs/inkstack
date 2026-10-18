package org.inkstack.sync.data

import dagger.Reusable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.inkstack.core.exceptions.SyncApiException
import org.inkstack.core.network.BaseHttpClient
import org.inkstack.core.util.ext.toRequestBody
import org.inkstack.parsers.util.await
import org.inkstack.parsers.util.parseJson
import org.inkstack.parsers.util.parseRaw
import org.inkstack.parsers.util.removeSurrounding
import javax.inject.Inject

@Reusable
class SyncAuthApi @Inject constructor(
	@BaseHttpClient private val okHttpClient: OkHttpClient,
) {

	suspend fun authenticate(syncURL: String, email: String, password: String): String {
		val body = JSONObject(
			mapOf("email" to email, "password" to password),
		).toRequestBody()
		val request = Request.Builder()
			.url("$syncURL/auth")
			.post(body)
			.build()
		val response = okHttpClient.newCall(request).await()
		if (response.isSuccessful) {
			return response.parseJson().getString("token")
		} else {
			val code = response.code
			val message = response.parseRaw().removeSurrounding('"')
			throw SyncApiException(message, code)
		}
	}

	suspend fun forgotPassword(syncURL: String, email: String) {
		val body = JSONObject(
			mapOf("email" to email),
		).toRequestBody()
		val request = Request.Builder()
			.url("$syncURL/forgot-password")
			.post(body)
			.build()
		val response = okHttpClient.newCall(request).await()
		if (!response.isSuccessful) {
			val code = response.code
			val message = response.parseRaw().removeSurrounding('"')
			throw SyncApiException(message, code)
		}
	}

	suspend fun resetPassword(syncURL: String, resetToken: String, password: String) {
		val body = JSONObject(
			mapOf("reset_token" to resetToken, "password" to password),
		).toRequestBody()
		val request = Request.Builder()
			.url("$syncURL/reset-password")
			.post(body)
			.build()
		val response = okHttpClient.newCall(request).await()
		if (!response.isSuccessful) {
			val code = response.code
			val message = response.parseRaw().removeSurrounding('"')
			throw SyncApiException(message, code)
		}
	}
}
