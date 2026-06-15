package com.example.ar_app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ThemeActivity : AppCompatActivity() {
    private var selectedThemeJson: String? = null
    private var selectedThemeName: String = "Modern Minimalist"
    private var selectedThemeDesc: String = "Sleek lines and monochromatic purity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_theme)
        BottomNavHelper.setup(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<android.widget.ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val card1 = findViewById<androidx.cardview.widget.CardView>(R.id.cardTheme1)
        val card2 = findViewById<androidx.cardview.widget.CardView>(R.id.cardTheme2)
        val card3 = findViewById<androidx.cardview.widget.CardView>(R.id.cardTheme3)
        val card4 = findViewById<androidx.cardview.widget.CardView>(R.id.cardTheme4)
        val card5 = findViewById<androidx.cardview.widget.CardView>(R.id.cardTheme5)

        val radio1 = findViewById<android.widget.RadioButton>(R.id.radioTheme1)
        val radio2 = findViewById<android.widget.RadioButton>(R.id.radioTheme2)
        val radio3 = findViewById<android.widget.RadioButton>(R.id.radioTheme3)
        val radio4 = findViewById<android.widget.RadioButton>(R.id.radioTheme4)
        val radio5 = findViewById<android.widget.RadioButton>(R.id.radioTheme5)
        
        val tvActiveThemeName = findViewById<android.widget.TextView>(R.id.tvActiveThemeName)
        val tvActiveThemeDesc = findViewById<android.widget.TextView>(R.id.tvActiveThemeTagline)

        val radios = listOf(radio1, radio2, radio3, radio4, radio5)

        // Predefined JSON Layouts
        val theme1Json = """{"user_id":"theme","room_name":"Scandinavian","mode":"TestAR","items":[{"name":"Sofa","model_url":"https://raw.githubusercontent.com/Vasanth1817/AR-Furniture-Models/main/Sofas/Sofa01.glb","price":"Rs. 30,000","position":{"x":0.0,"y":0.0,"z":1.5},"rotation":{"x":0.0,"y":0.0,"z":0.0,"w":1.0},"scale":{"x":1.0,"y":1.0,"z":1.0}},{"name":"Table","model_url":"https://raw.githubusercontent.com/Vasanth1817/AR-Furniture-Models/main/Tables/ClassicTable.glb","price":"Rs. 15,000","position":{"x":0.0,"y":0.0,"z":0.5},"rotation":{"x":0.0,"y":0.0,"z":0.0,"w":1.0},"scale":{"x":1.0,"y":1.0,"z":1.0}}]}"""
        val theme2Json = """{"user_id":"theme","room_name":"Industrial","mode":"TestAR","items":[{"name":"Desk","model_url":"https://raw.githubusercontent.com/Vasanth1817/AR-Furniture-Models/main/Tables/OfficeDesk6Mb.glb","price":"Rs. 25,000","position":{"x":0.0,"y":0.0,"z":1.0},"rotation":{"x":0.0,"y":0.0,"z":0.0,"w":1.0},"scale":{"x":1.0,"y":1.0,"z":1.0}},{"name":"Cupboard","model_url":"https://raw.githubusercontent.com/Vasanth1817/AR-Furniture-Models/main/Cupboards/Cupboard_1.glb","price":"Rs. 20,000","position":{"x":-1.5,"y":0.0,"z":1.5},"rotation":{"x":0.0,"y":0.707,"z":0.0,"w":0.707},"scale":{"x":1.0,"y":1.0,"z":1.0}}]}"""
        val theme3Json = """{"user_id":"theme","room_name":"Bohemian","mode":"TestAR","items":[{"name":"Single Sofa","model_url":"https://raw.githubusercontent.com/Vasanth1817/AR-Furniture-Models/main/Sofas/SingleSofa.glb","price":"Rs. 18,000","position":{"x":-0.5,"y":0.0,"z":1.2},"rotation":{"x":0.0,"y":0.382,"z":0.0,"w":0.923},"scale":{"x":1.0,"y":1.0,"z":1.0}},{"name":"Bed","model_url":"https://raw.githubusercontent.com/Vasanth1817/AR-Furniture-Models/main/Beds/Bed_4.glb","price":"Rs. 35,000","position":{"x":1.0,"y":0.0,"z":2.0},"rotation":{"x":0.0,"y":-0.707,"z":0.0,"w":0.707},"scale":{"x":1.0,"y":1.0,"z":1.0}}]}"""
        val theme4Json = """{"user_id":"theme","room_name":"Contemporary","mode":"TestAR","items":[{"name":"Dining Set","model_url":"https://raw.githubusercontent.com/Vasanth1817/AR-Furniture-Models/main/DiningSets/SleekModernDiningTableSet.glb","price":"Rs. 60,000","position":{"x":0.0,"y":0.0,"z":1.5},"rotation":{"x":0.0,"y":0.0,"z":0.0,"w":1.0},"scale":{"x":1.0,"y":1.0,"z":1.0}}]}"""
        val theme5Json = """{"user_id":"theme","room_name":"Mid-Century Modern","mode":"TestAR","items":[{"name":"Side Table","model_url":"https://raw.githubusercontent.com/Vasanth1817/AR-Furniture-Models/main/Tables/VictorianSideTable.glb","price":"Rs. 8,000","position":{"x":1.0,"y":0.0,"z":1.0},"rotation":{"x":0.0,"y":0.0,"z":0.0,"w":1.0},"scale":{"x":1.0,"y":1.0,"z":1.0}},{"name":"Chair","model_url":"https://raw.githubusercontent.com/Vasanth1817/AR-Furniture-Models/main/Chairs/OldWoodenChair05.glb","price":"Rs. 5,000","position":{"x":0.5,"y":0.0,"z":1.2},"rotation":{"x":0.0,"y":-0.382,"z":0.0,"w":0.923},"scale":{"x":1.0,"y":1.0,"z":1.0}}]}"""
        
        fun selectTheme(index: Int, name: String, desc: String, json: String) {
            radios.forEachIndexed { i, r -> r.isChecked = (i == index) }
            selectedThemeName = name
            selectedThemeDesc = desc
            selectedThemeJson = json
            tvActiveThemeName.text = name
            tvActiveThemeDesc.text = desc
        }

        card1.setOnClickListener { selectTheme(0, "Scandinavian", "Warm wood & airy minimalism", theme1Json) }
        card2.setOnClickListener { selectTheme(1, "Industrial", "Raw metals & exposed elements", theme2Json) }
        card3.setOnClickListener { selectTheme(2, "Bohemian", "Eclectic patterns & plants", theme3Json) }
        card4.setOnClickListener { selectTheme(3, "Contemporary", "Bold curves & marble textures", theme4Json) }
        card5.setOnClickListener { selectTheme(4, "Mid-Century Modern", "Iconic wood & retro vibes", theme5Json) }

        // Default to Theme 1
        selectTheme(0, "Scandinavian", "Warm wood & airy minimalism", theme1Json)

        val btnApplyTheme = findViewById<android.widget.Button>(R.id.btnApplyTheme)
        btnApplyTheme.setOnClickListener {
            val json = selectedThemeJson ?: return@setOnClickListener
            
            // Navigate to Unity AR Viewer with the JSON payload
            val intent = android.content.Intent(this, ARViewerActivity::class.java)
            intent.putExtra("ROOM_LAYOUT_JSON", json)
            
            // We want it to open in real AR!
            val prefsVirtual = getSharedPreferences("VirtualRoomPrefs", MODE_PRIVATE)
            prefsVirtual.edit().putString("UNITY_SCENE", "TestAR").apply()
            
            startActivity(intent)
        }
    }
}