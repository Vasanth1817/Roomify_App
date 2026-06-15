package com.example.ar_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class VirtualRoomInputActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_virtual_room_input)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val etLength = findViewById<EditText>(R.id.etLength)
        val etWidth = findViewById<EditText>(R.id.etWidth)
        val btnGenerate = findViewById<Button>(R.id.btnGenerate)

        btnGenerate.setOnClickListener {
            val lengthStr = etLength.text.toString()
            val widthStr = etWidth.text.toString()

            if (lengthStr.isEmpty() || widthStr.isEmpty()) {
                Toast.makeText(this, "Please enter both dimensions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val length = lengthStr.toFloatOrNull()
            val width = widthStr.toFloatOrNull()

            if (length == null || width == null || length <= 0 || width <= 0) {
                Toast.makeText(this, "Please enter valid positive numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save dimensions to SharedPreferences for Unity to read
            val prefs = getSharedPreferences("VirtualRoomPrefs", MODE_PRIVATE)
            prefs.edit()
                .putFloat("ROOM_LENGTH", length)
                .putFloat("ROOM_WIDTH", width)
                .putString("UNITY_SCENE", "VirtualRoomScene")
                .apply()

            Toast.makeText(this, "Unity Scene 'VirtualRoomScene' must be exported to test this!", Toast.LENGTH_LONG).show()
            
            // TODO: Launch Unity Activity (FurnitureCatalog first to pick furniture, then Unity)
            // For now we go to catalog and set a flag
            val intent = Intent(this, FurnitureCatalogActivity::class.java)
            intent.putExtra("MODE", "VIRTUAL_ROOM")
            startActivity(intent)
        }
    }
}
