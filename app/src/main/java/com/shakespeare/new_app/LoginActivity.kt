package com.shakespeare.new_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.database.InsertCallback
import com.example.database.QueryResult
import com.example.database.QueryResultCallback
import com.example.database.RemoteDatabaseHelperHttp
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Use the res/values/strings.xml file
//            .requestIdToken("698320345115-3s1du12v50cqvctmss54h4fmpuk5md5s.apps.googleusercontent.com")
//            .requestIdToken(getString(R.string.default_web_client_id)) // From google-services.json
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()

        // Setup Sign-In Button
        findViewById<Button>(R.id.google_sign_in_button).setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("LoginActivity", "Google sign in failed", e)
                Toast.makeText(this, "Sign-in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

//    private fun firebaseAuthWithGoogle(idToken: String) {
//        val credential = GoogleAuthProvider.getCredential(idToken, null)
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    val user = auth.currentUser
//                    Log.d("LoginActivity", "signInWithCredential:success ${user?.displayName}")
//                    // Go to main screen
//                    startActivity(Intent(this, MainActivity::class.java))
//                    finish()
//                } else {
//                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
//                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
//                }
//
//
//            }
//    }

//        private fun firebaseAuthWithGoogle(idToken: String) {
//            val credential = GoogleAuthProvider.getCredential(idToken, null)
//            auth.signInWithCredential(credential)
//                .addOnCompleteListener(this) { task ->
//                    if (task.isSuccessful) {
//                        val user = FirebaseAuth.getInstance().currentUser
//                        if (user != null) {
//                            // ✅ Build SQL and insert user
//                            val uid = user.uid
//                            val name = user.displayName ?: "Unknown"
//                            val email = user.email ?: "Unknown"
//
//                            val sql = """
//                    INSERT OR IGNORE INTO users (uid, display_name, email)
//                    VALUES ('$uid', '${name.replace("'", "''")}', '${email.replace("'", "''")}')
//                """.trimIndent()
//
//                            RemoteDatabaseHelperHttp(this).runInsert(sql, object : InsertCallback {
//                                override fun onInsertSuccess() {
//                                    Log.d("LoginActivity", "User inserted into SQLiteCloud")
//                                }
//
//                                override fun onInsertFailure(e: Throwable) {
//                                    Log.e("LoginActivity", "Insert failed", e)
//                                }
//                            })
//                        }
//
//                        // ✅ Proceed to main screen
//                        startActivity(Intent(this, MainActivity::class.java))
//                        finish()
//                    } else {
//                        // ❌ Handle failed login
//                        Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
//                        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
//                    }
//                }
//        }

    // This version does not check whether the selected display name is unique, and it does not add it to the database.
    // Replaced with updated version below.
//    private fun firebaseAuthWithGoogle(idToken: String) {
//        val credential = GoogleAuthProvider.getCredential(idToken, null)
//        val auth = FirebaseAuth.getInstance()
//
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    val user = auth.currentUser
//                    if (user != null) {
//                        val uid = user.uid
//                        val name = user.displayName ?: "Unknown"
//                        val email = user.email ?: "Unknown"
//
//                        val dbHelper = RemoteDatabaseHelperHttp(this)
//                        val checkSql = "SELECT login_count FROM users WHERE uid = '$uid'"
//
//                        dbHelper.runQueryFromJava(checkSql, object :
//                            QueryResultCallback<List<Map<String, String>>> {
//                            override fun onResult(result: QueryResult<List<Map<String, String>>>) {
//                                if (result.success && !result.data.isNullOrEmpty()) {
//                                    // ✅ User exists: update login_count
//                                    val currentCount = result.data[0]["login_count"]?.toIntOrNull() ?: 1
//                                    val updateSql = "UPDATE users SET login_count = ${currentCount + 1} WHERE uid = '$uid'"
//
//                                    dbHelper.runInsert(updateSql, object : InsertCallback {
//                                        override fun onInsertSuccess() {
//                                            Log.d("LoginActivity", "Updated login count for user $uid")
//                                        }
//
//                                        override fun onInsertFailure(e: Throwable) {
//                                            Log.e("LoginActivity", "Failed to update login count", e)
//                                        }
//                                    })
//                                } else {
//                                    // ❌ New user: insert with created_at and login_count = 1
//                                    val insertSql = """
//                                    INSERT INTO users (uid, display_name, email, created_at, login_count)
//                                    VALUES (
//                                        '$uid',
//                                        '${name.replace("'", "''")}',
//                                        '${email.replace("'", "''")}',
//                                        datetime('now'),
//                                        1
//                                    )
//                                """.trimIndent()
//
//                                    dbHelper.runInsert(insertSql, object : InsertCallback {
//                                        override fun onInsertSuccess() {
//                                            Log.d("LoginActivity", "Inserted new user $uid")
//                                        }
//
//                                        override fun onInsertFailure(e: Throwable) {
//                                            Log.e("LoginActivity", "Insert failed", e)
//                                        }
//                                    })
//                                }
//                            }
//                        })
//
//                        // ✅ Navigate to main screen
//                        startActivity(Intent(this, MainActivity::class.java))
//                        finish()
//                    }
//                } else {
//                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
//                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }

    // This version checks whether the selected display name is unique, and adds it to the database.
    // 21 June 2025
    // Further updates made to ensure that the username in Shared Prferences is always the one associated
    // with the current Firebase/Google UID: 22 June 2025
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val auth = FirebaseAuth.getInstance()

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val uid = user.uid
                        val name = user.displayName ?: "Unknown"
                        val email = user.email ?: "Unknown"

                        val dbHelper = RemoteDatabaseHelperHttp(this)

                        // Step 1: Check login count / create new user
                        val checkSql = "SELECT login_count FROM users WHERE uid = '$uid'"
                        dbHelper.runQueryFromJava(checkSql, object :
                            QueryResultCallback<List<Map<String, String>>> {
                            override fun onResult(result: QueryResult<List<Map<String, String>>>) {
                                if (result.success && !result.data.isNullOrEmpty()) {
                                    val currentCount = result.data[0]["login_count"]?.toIntOrNull() ?: 1
                                    val updateSql = "UPDATE users SET login_count = ${currentCount + 1} WHERE uid = '$uid'"
                                    dbHelper.runInsert(updateSql, object : InsertCallback {
                                        override fun onInsertSuccess() {
                                            Log.d("LoginActivity", "Updated login count for user $uid")
                                        }

                                        override fun onInsertFailure(e: Throwable) {
                                            Log.e("LoginActivity", "Failed to update login count", e)
                                        }
                                    })
                                } else {
                                    val insertSql = """
                                    INSERT INTO users (uid, display_name, email, created_at, login_count)
                                    VALUES (
                                        '$uid',
                                        '${name.replace("'", "''")}',
                                        '${email.replace("'", "''")}',
                                        datetime('now'),
                                        1
                                    )
                                """.trimIndent()
                                    dbHelper.runInsert(insertSql, object : InsertCallback {
                                        override fun onInsertSuccess() {
                                            Log.d("LoginActivity", "Inserted new user $uid")
                                        }

                                        override fun onInsertFailure(e: Throwable) {
                                            Log.e("LoginActivity", "Insert failed", e)
                                        }
                                    })
                                }

                                // ✅ Step 2: After user table handled → Check for display name
                                val displayNameSql = "SELECT username FROM user_displaynames WHERE uid = '$uid'"
                                dbHelper.runQueryFromJava(displayNameSql, object :
                                    QueryResultCallback<List<Map<String, String>>> {
                                    override fun onResult(result: QueryResult<List<Map<String, String>>>) {
                                        if (result.success && !result.data.isNullOrEmpty()) {
                                            val displayName = result.data[0]["username"] ?: "Unknown"
                                            Log.d("check",displayName)
                                            getSharedPreferences("prefs", MODE_PRIVATE)
                                                .edit()
                                                .putString("username", displayName)
                                                .apply()
                                            Toast.makeText(this@LoginActivity, "Your display name is: $displayName", Toast.LENGTH_LONG).show()
                                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                            finish()
                                        } else {
                                            // No display name yet → launch display name picker
                                            Log.d("check","no display name yet")
                                            startActivity(Intent(this@LoginActivity, ChooseDisplayNameActivity::class.java))
                                            finish()
                                        }
                                    }
                                })
                            }
                        })
                    }
                } else {
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }


}
