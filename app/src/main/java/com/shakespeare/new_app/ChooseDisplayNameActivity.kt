package com.shakespeare.new_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.database.InsertCallback
import com.example.database.QueryResult
import com.example.database.QueryResultCallback
import com.example.database.RemoteDatabaseHelperHttp
import com.google.firebase.auth.FirebaseAuth

class ChooseDisplayNameActivity : AppCompatActivity() {

    private lateinit var editTextDisplayName: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var dbHelper: RemoteDatabaseHelperHttp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_display_name)

        editTextDisplayName = findViewById(R.id.editTextDisplayName)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        dbHelper = RemoteDatabaseHelperHttp(this)

        buttonSubmit.setOnClickListener {
            val inputName = editTextDisplayName.text.toString().trim()
            if (inputName.isEmpty()) {
                Toast.makeText(this, "Please enter a display name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkAndInsertDisplayName(inputName)
        }
    }

    private fun checkAndInsertDisplayName(displayName: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val checkSql = "SELECT username FROM user_displaynames WHERE username = '${displayName.replace("'", "''")}'"
        dbHelper.runQueryFromJava(checkSql, object : QueryResultCallback<List<Map<String, String>>> {
            override fun onResult(result: QueryResult<List<Map<String, String>>>) {
                if (result.success && result.data?.isNotEmpty() == true) {
                    Toast.makeText(this@ChooseDisplayNameActivity, "That name is already taken", Toast.LENGTH_SHORT).show()
                } else {
                    val insertSql = """
                        INSERT INTO user_displaynames (uid, username)
                        VALUES ('$uid', '${displayName.replace("'", "''")}')
                    """.trimIndent()

                    dbHelper.runInsert(insertSql, object : InsertCallback {
                        override fun onInsertSuccess() {
                            getSharedPreferences("prefs", MODE_PRIVATE)
                                .edit()
                                .putString("username", displayName)
                                .apply()
                            Toast.makeText(this@ChooseDisplayNameActivity, "Welcome, $displayName!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@ChooseDisplayNameActivity, MainActivity::class.java))
                            finish()
                        }

                        override fun onInsertFailure(e: Throwable) {
                            Log.e("ChooseDisplayName", "Insert failed", e)
                            Toast.makeText(this@ChooseDisplayNameActivity, "Failed to save name", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        })
    }
}
