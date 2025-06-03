package com.example.database

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject

import android.util.Log
import kotlinx.coroutines.*
import java.io.IOException

class RemoteDatabaseHelperHttp(private val context: android.content.Context) {

        companion object {
            const val BASE_URL = "https://your-railway-url-here.up.railway.app"
        }

    fun runQueryFromJava(
        sql: String,
        callback: QueryResultCallback<List<Map<String, String>>>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = postRequest(sql)  // This should return raw JSON string
                val rows = parseResponse(response) // MutableList<Map<String, String>>

                withContext(Dispatchers.Main) {
                    @Suppress("UNCHECKED_CAST")
                    callback.onResult(QueryResult(true, rows.toList(), null))
                }
            } catch (e: Exception) {
                Log.e("RemoteHelper", "Query failed", e)
                withContext(Dispatchers.Main) {
                    callback.onResult(QueryResult(false, null, e))
                }
            }
        }
    }

//    private fun postRequest(sql: String): String {
//        // Placeholder for actual HTTP POST logic.
//        // Replace with your actual network logic using OkHttp, HttpUrlConnection, etc.
//        throw NotImplementedError("Implement postRequest to send SQL to server and return response")
//    }

    private fun postRequest(sql: String): String {
        val baseUrl = "https://android-sqlitecloud-api-production.up.railway.app/query"
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("sql", sql)
        }

        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(baseUrl)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected HTTP response code: ${response.code}")
            }

            return response.body?.string()
                ?: throw IOException("Empty response body")
        }
    }

    private fun parseResponse(response: String): MutableList<Map<String, String>> {
        val rows = mutableListOf<Map<String, String>>()
        val json = JSONObject(response)

        val success = json.optBoolean("success", false)
        if (!success) throw Exception(json.optString("error", "Unknown error"))

        val columns = json.getJSONArray("columns")
        val values = json.getJSONArray("values")

        for (i in 0 until values.length()) {
            val row = values.getJSONArray(i)
            val map = mutableMapOf<String, String>()
            for (j in 0 until columns.length()) {
                val key = columns.getString(j)
                val value = row.optString(j, "")
                map[key] = value
            }
            rows.add(map)
        }
        return rows
    }

        fun runInsert(sql: String, callback: InsertCallback) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val json = JSONObject().apply {
                        put("sql", sql)
                    }

                    val requestBody = RequestBody.create(
                        "application/json".toMediaTypeOrNull(),
                        json.toString()
                    )

                    val request = Request.Builder()
                        .url("$BASE_URL/insert")
                        .post(requestBody)
                        .build()

                    val client = OkHttpClient()
                    val response = client.newCall(request).execute()

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            callback.onInsertSuccess()
                        } else {
                            callback.onInsertFailure(Exception("HTTP error ${response.code}"))
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        callback.onInsertFailure(e)
                    }
                }
            }
        }

    }
