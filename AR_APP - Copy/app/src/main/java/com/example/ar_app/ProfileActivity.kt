package com.example.ar_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        BottomNavHelper.setup(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val fullName = prefs.getString("FULL_NAME", "Guest User")
        val email = prefs.getString("EMAIL", "guest@example.com")
        
        findViewById<android.widget.TextView>(R.id.tvName).text = fullName
        findViewById<android.widget.TextView>(R.id.tvEmail).text = email
        
        findViewById<android.widget.ImageView>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        
    }

    override fun onResume() {
        super.onResume()
        fetchStats()
    }

    private fun fetchStats() {
        val prefs = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", "")
        if (!userId.isNullOrEmpty()) {
            val request = okhttp3.Request.Builder()
                .url("https://roomifybackend.onrender.com/get_layouts?user_id=$userId")
                .build()

            val client = okhttp3.OkHttpClient()
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) { }
                
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (response.isSuccessful) {
                        val body = response.body()?.string()
                        if (body != null) {
                            try {
                                val array = org.json.JSONArray(body)
                                val savedCount = array.length()
                                var activeCount = 0
                                
                                for (i in 0 until savedCount) {
                                    val obj = array.getJSONObject(i)
                                    val mode = obj.optString("mode", "")
                                    if (mode == "Virtual") {
                                        activeCount++
                                    }
                                }
                                
                                runOnUiThread {
                                    findViewById<android.widget.TextView>(R.id.tvSavedDesignsCount).text = savedCount.toString()
                                    findViewById<android.widget.TextView>(R.id.tvActiveRoomsCount).text = activeCount.toString()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.cardSignOut).setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    fun back(view: View) {
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
    }
}