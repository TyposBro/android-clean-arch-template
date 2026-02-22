package com.example.app.core.network.models

import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

sealed class RepositoryResult<out T> {
    data class Success<out T>(val data: T) : RepositoryResult<T>()
    data class Error(
        val message: String,
        val code: Int? = null,
        val errorCode: String? = null,
        val retryable: Boolean = false
    ) : RepositoryResult<Nothing>()
}

suspend inline fun <T> safeApiCall(crossinline apiCall: suspend () -> Response<T>): RepositoryResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let { RepositoryResult.Success(it) }
                ?: RepositoryResult.Success(
                    // Handle Void/Unit responses (like 204 No Content) gracefully
                    @Suppress("UNCHECKED_CAST")
                    (Unit as T)
                )
        } else {
            val code = response.code()
            val errorBody = response.errorBody()?.string()
            var errorMessage: String? = null
            var errorCode: String? = null
            var retryable = code in 500..599

            // 1. Try Parsing JSON
            if (!errorBody.isNullOrBlank()) {
                try {
                    val json = JSONObject(errorBody)
                    errorMessage = json.optString("message").takeIf { it.isNotEmpty() }
                        ?: json.optString("error").takeIf { it.isNotEmpty() }
                    errorCode = json.optString("errorCode").takeIf { it.isNotEmpty() }
                    if (json.has("retryable")) retryable = json.optBoolean("retryable")
                } catch (e: Exception) {
                    // Not JSON. If it's plain text (not HTML), use it.
                    if (!errorBody.trim().startsWith("<")) {
                        errorMessage = errorBody
                    }
                }
            }

            // 2. Fallback to HTTP Status Message
            if (errorMessage.isNullOrBlank()) {
                errorMessage = response.message().takeIf { it.isNotEmpty() }
            }

            // 3. Last Resort: Generic Code Description
            if (errorMessage.isNullOrBlank()) {
                errorMessage = when (code) {
                    404 -> "Resource not found (404)"
                    401 -> "Unauthorized (401)"
                    403 -> "Forbidden (403)"
                    500 -> "Internal Server Error (500)"
                    else -> "Unknown error ($code)"
                }
            }

            RepositoryResult.Error(errorMessage!!, code, errorCode, retryable)
        }
    } catch (e: SocketTimeoutException) {
        RepositoryResult.Error("The server took too long to respond.", 408, "TIMEOUT", true)
    } catch (e: IOException) {
        RepositoryResult.Error("Network connection failed.", null, "NETWORK_ERROR", true)
    } catch (e: Exception) {
        android.util.Log.e("SafeApiCall", "Api call failed", e)
        RepositoryResult.Error("Unexpected error: ${e.javaClass.simpleName} - ${e.message ?: "Unknown"}")
    }
}
