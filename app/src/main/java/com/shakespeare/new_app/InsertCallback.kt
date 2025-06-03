package com.example.database

interface InsertCallback {
    fun onInsertSuccess()
    fun onInsertFailure(e: Throwable)
//    fun onSuccess()
//    fun onError(error: Throwable)
//    fun onInsertFailure(exception: Throwable?)
}
