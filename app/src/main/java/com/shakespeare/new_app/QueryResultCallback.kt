//package com.shakespeare.new_app

package com.example.database

fun interface QueryResultCallback<T> {
    fun onResult(result: QueryResult<T>)
}