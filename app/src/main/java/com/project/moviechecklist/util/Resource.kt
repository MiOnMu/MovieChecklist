package com.project.moviechecklist.util

sealed class Resource<out T>(val data: T? = null, val message: String? = null, val code: Int? = null) {
    class Success<out T>(data: T) : Resource<T>(data)
    class Error<out T>(message: String, data: T? = null, code: Int? = null) : Resource<T>(data, message, code)
    class Loading<out T>(data: T? = null) : Resource<T>(data)
}
