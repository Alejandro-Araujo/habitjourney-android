package com.alejandro.habitjourney.core.data.remote.network


sealed class NetworkResponse<out T> {
    data class Success<T>(val data: T) : NetworkResponse<T>()
    data class Error(val exception: Throwable) : NetworkResponse<Nothing>()
    data object Loading : NetworkResponse<Nothing>()

    companion object {
        fun <T> success(data: T): NetworkResponse<T> = Success(data)
        fun error(exception: Throwable): NetworkResponse<Nothing> = Error(exception)
        fun loading(): NetworkResponse<Nothing> = Loading
    }

    inline fun onSuccess(action: (T) -> Unit): NetworkResponse<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (Throwable) -> Unit): NetworkResponse<T> {
        if (this is Error) action(exception)
        return this
    }

    inline fun onLoading(action: () -> Unit): NetworkResponse<T> {
        if (this is Loading) action()
        return this
    }

    fun <R> map(transform: (T) -> R): NetworkResponse<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(exception)
            is Loading -> Loading
        }
    }
}