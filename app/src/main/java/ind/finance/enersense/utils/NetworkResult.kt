package ind.finance.enersense.utils

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>()
    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>()
    object Empty : NetworkResult<Nothing>()
}

suspend fun <T> safeApiCall(apiCall: suspend () -> retrofit2.Response<T>): NetworkResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Empty
            }
        } else {
            NetworkResult.Error(response.code(), response.errorBody()?.string() ?: response.message())
        }
    } catch (e: Throwable) {
        NetworkResult.Exception(e)
    }
}
