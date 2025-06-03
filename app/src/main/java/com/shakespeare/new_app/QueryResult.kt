package com.example.database

data class QueryResult<T>(
    val success: Boolean,
    val data: T? = null,
    val exception: Throwable? = null
) {
    fun isSuccess() = success
    fun exceptionOrNull() = exception
}