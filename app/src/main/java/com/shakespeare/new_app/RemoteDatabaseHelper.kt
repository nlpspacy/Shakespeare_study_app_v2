package com.example.database

import android.util.Log
import com.shakespeare.new_app.ShowPlaySharedDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class RemoteDatabaseHelperHttp(
    showPlaySharedDb: ShowPlaySharedDb,
    url: String
) {

    private val client = OkHttpClient()
//    private val endpoint = "http://10.0.2.2:8000/query" // use 10.0.2.2 to refer to localhost from Android emulator
    private val endpoint = "https://android-sqlitecloud-api-production.up.railway.app/query"
    private val baseUrl = "https://android-sqlitecloud-api-production.up.railway.app"

    fun runQueryFromJava(sql: String, callback: (QueryResult) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().put("sql", sql).toString()
                val body = json.toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url(endpoint)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    val resultBody = response.body?.string()
                    Log.d("RemoteHttp", "Response: $resultBody")

                    if (!response.isSuccessful || resultBody == null) {
                        withContext(Dispatchers.Main) {
                            callback(QueryResult(false, null, Exception("HTTP error")))
                        }
                        return@use
                    }

                    val jsonObj = JSONObject(resultBody)
                    if (jsonObj.optBoolean("success", false)) {
                        val rows = mutableListOf<Map<String, String>>()
                        val jsonRows = jsonObj.getJSONArray("rows")
                        for (i in 0 until jsonRows.length()) {
                            val rowObj = jsonRows.getJSONObject(i)
                            val rowMap = mutableMapOf<String, String>()
                            for (key in rowObj.keys()) {
                                rowMap[key] = rowObj.getString(key)
                            }
                            rows.add(rowMap)
                        }
                        withContext(Dispatchers.Main) {
                            callback(QueryResult(true, rows))
                        }
                    } else {
                        val error = jsonObj.optString("error", "Unknown error")
                        withContext(Dispatchers.Main) {
                            callback(QueryResult(false, null, Exception(error)))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RemoteHttp", "Query failed", e)
                withContext(Dispatchers.Main) {
                    callback(QueryResult(false, null, e))
                }
            }
        }
    }

    interface SimpleCallback {
        fun onResult(success: Boolean, message: String?)
    }


    fun createUser(username: String, callback: HttpCallback) {
        val json = JSONObject()
        json.put("username", username)

        val body = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder()
            .url("$baseUrl/create_user")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
//                callback.onResult(false, null)
                callback.onError(e) // ✅ NEW way
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
//                    val responseBody = response.body?.string()
//                    callback.onResult(true, responseBody)

                    Log.d("createUser", "Success")
                    callback.onSuccess("User created successfully") // <<✅ This is where you call success

                } else {
//                    callback.onResult(false, null)

                    callback.onError(IOException("Unexpected code $response"))

                }
            }
        })
    }
}

// Shared data class

data class QueryResult(
    val isSuccess: Boolean,
    val data: List<Map<String, String>>? = null,
    val error: Throwable? = null
)


interface HttpCallback {
    fun onSuccess(message: String)
    fun onError(e: Exception)
}
