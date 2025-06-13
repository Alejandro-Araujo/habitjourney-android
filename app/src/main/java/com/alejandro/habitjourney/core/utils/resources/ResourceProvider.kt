package com.alejandro.habitjourney.core.utils.resources

import androidx.annotation.StringRes


interface ResourceProvider {
    fun getString(@StringRes resId: Int): String
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String
    fun getQuantityString(resId: Int, quantity: Int, vararg formatArgs: Any): String
}
