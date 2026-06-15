package com.example.ar_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        BottomNavHelper.setup(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        val switchDarkMode = findViewById<SwitchCompat>(R.id.switchDarkMode)
        val rowClearFurniture = findViewById<LinearLayout>(R.id.rowClearFurniture)
        val rowResetDesigns = findViewById<LinearLayout>(R.id.rowResetDesigns)

        // Load saved preferences
        switchDarkMode.isChecked = prefs.getBoolean("DARK_MODE", false)

        // Save preferences on toggle
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("DARK_MODE", isChecked).apply()
            val modeText = if (isChecked) "Dark Mode Enabled" else "Light Mode Enabled"
            Toast.makeText(this, modeText, Toast.LENGTH_SHORT).show()
            if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Maintenance actions
        rowClearFurniture.setOnClickListener {
            // Delete AR cached models directory if exists
            val cacheDir = java.io.File(cacheDir, "models")
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
            }
            Toast.makeText(this, "Cleared locally cached furniture models.", Toast.LENGTH_SHORT).show()
        }

        rowResetDesigns.setOnClickListener {
            Toast.makeText(this, "To delete saved designs, go to 'Saved' and swipe them away.", Toast.LENGTH_LONG).show()
        }
    }
    fun back(view: View) {
        finish()
    }
}