package com.example.database

import android.util.Log
import com.example.database.QueryResult
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class RemoteDatabaseHelperHttp {

    private val baseUrl = "https://android-sqlitecloud-api-production.up.railway.app"
    private val client = OkHttpClient()

    fun runQueryFromJava(sql: String, callback: (QueryResult<List<Map<String, String>>>) -> Unit) {
        val fullUrl = "$baseUrl/query"  // ✅ query endpoint
        val json = JSONObject().apply {
            put("sql", sql)
        }
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(fullUrl)
            .post(requestBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                val resultBody = response.body?.string()
                Log.d("RemoteHttp", "Response: $resultBody")

                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        callback(QueryResult(false, null, Exception("HTTP error")))
                    }
                    return@launch
                }

//                val rows = JsonUtils.parseJsonRows(resultBody!!)

                val json = JSONObject(resultBody!!)
                val rows = mutableListOf<Map<String, String>>()

                val columns = json.getJSONArray("columns")
                val values = json.getJSONArray("values")

                for (i in 0 until values.length()) {
                    val row = values.getJSONArray(i)
                    val map = mutableMapOf<String, String>()
                    for (j in 0 until columns.length()) {
                        map[columns.getString(j)] = row.getString(j)
                    }
                    rows.add(map)
                }

                withContext(Dispatchers.Main) {
                    callback(QueryResult(true, rows, null))
                }
            } catch (e: Exception) {
                Log.e("RemoteHttp", "Exception during query", e)
                withContext(Dispatchers.Main) {
                    callback(QueryResult(false, null, e))
                }
            }
        }
    }

    fun createUser(username: String, callback: HttpCallback) {
        val fullUrl = "$baseUrl/create_user"  // ✅ create_user endpoint
        val json = JSONObject().apply {
            put("username", username)
        }
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(fullUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback.onError(IOException("Unexpected code $response"))
                } else {
                    callback.onSuccess("User created successfully")
                }
            }
        })
    }

    fun updateSelection(username: String, characterName: String, isUser: Boolean) {
        val fullUrl = "$baseUrl/update_selection"  // ✅ update_selection endpoint
        val json = JSONObject().apply {
            put("username", username)
            put("character_full_name", characterName)
            put("is_user", isUser)
        }
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(fullUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UpdateServer", "Failed to update server", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("UpdateServer", "Server error: ${response.code}")
                } else {
                    Log.d("UpdateServer", "Update successful")
                }
            }
        })
    }

    interface HttpCallback {
        fun onSuccess(message: String)
        fun onError(e: Exception)
//        fun exceptionOrNull(e: Exception) // ChatGPT makes it clear this should not be included:
        // exceptionOrNull() is not something you call manually —
        //it was part of the QueryResult class (to read an error), not something the HTTP callback needs.
        //In fact, exceptionOrNull is a method for results, not for HTTP callbacks.
    }

    fun runInsert(sql: String, callback: InsertCallback) {
        runQueryFromJava(sql) { result ->
            if (result.success) {
                Log.d("RemoteInsert", "✅ Insert succeeded.")
                callback.onInsertSuccess()
            } else {
                Log.e("RemoteInsert", "❌ Insert failed", result.exception)
                callback.onInsertFailure(result.exception)
            }
        }
    }

    interface InsertCallback {
        fun onInsertSuccess()
        fun onInsertFailure(exception: Throwable?)
    }

}
