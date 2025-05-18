package com.alejandro.habitjourney.core.data.remote.exception

import android.content.Context
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.core.data.remote.interceptor.ApiException
import com.alejandro.habitjourney.core.data.remote.interceptor.NoConnectivityException
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


@Singleton
class ErrorHandler @Inject constructor(
    @ApplicationContext private val applicationContext: Context
){

    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is NoConnectivityException ->  applicationContext.getString(R.string.no_internet_connection)
            is SocketTimeoutException -> applicationContext.getString(R.string.connection_timeout)
            is UnknownHostException -> applicationContext.getString(R.string.server_not_found)
            is HttpException -> {
                when (throwable.code()) {
                    401 -> applicationContext.getString(R.string.session_expired)
                    403 -> applicationContext.getString(R.string.permission_denied)
                    404 -> applicationContext.getString(R.string.resource_not_found)
                    500, 501, 502, 503 -> applicationContext.getString(R.string.server_error)
                    else -> applicationContext.getString(R.string.network_error, throwable.code())
                }
            }
            is ApiException -> {
                when (throwable.code) {
                    401 -> applicationContext.getString(R.string.session_expired)
                    403 -> applicationContext.getString(R.string.permission_denied)
                    404 -> applicationContext.getString(R.string.resource_not_found)
                    500, 501, 502, 503 -> applicationContext.getString(R.string.server_error)
                    else -> applicationContext.getString(R.string.api_error, throwable.code, throwable.message)
                }
            }
            is IOException -> applicationContext.getString(R.string.unexpected_network_error)
            else -> applicationContext.getString(R.string.unexpected_error)
        }
    }
}