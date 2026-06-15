package com.example.ar_app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.graphics.Outline
import android.widget.TextView
import android.widget.RelativeLayout
import android.widget.FrameLayout

class BeforeAfterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_before_after)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<android.widget.ImageView>(R.id.btnBack)
        btnBack?.setOnClickListener { finish() }

        val b64Before = intent.getStringExtra("BEFORE_IMAGE_B64")
        val b64After = intent.getStringExtra("AFTER_IMAGE_B64")

        val imgBefore = findViewById<android.widget.ImageView>(R.id.imgBefore)
        val imgAfter = findViewById<android.widget.ImageView>(R.id.imgAfter)

        if (b64Before != null) {
            val decodedBytes = android.util.Base64.decode(b64Before.replace("data:image/jpeg;base64,", ""), android.util.Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            imgBefore.setImageBitmap(bitmap)
        }

        if (b64After != null) {
            val decodedBytes = android.util.Base64.decode(b64After.replace("data:image/jpeg;base64,", ""), android.util.Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            imgAfter.setImageBitmap(bitmap)
        }

        // Dynamic Stats Update
        val designName = intent.getStringExtra("DESIGN_NAME") ?: "My Design"
        val itemCount = intent.getIntExtra("ITEM_COUNT", 0)
        val budget = intent.getFloatExtra("BUDGET", 0f)

        findViewById<TextView>(R.id.tvCollectionName).text = "\"$designName\""
        findViewById<TextView>(R.id.tvFurnitureCount).text = "$itemCount Items"
        findViewById<TextView>(R.id.tvEstBudget).text = String.format(java.util.Locale("en", "IN"), "₹%,.0f", budget)
        
        // Hide collection time if not provided, or format it
        findViewById<TextView>(R.id.tvCollectionTime).visibility = View.GONE

        // Slider Interaction
        val sliderContainer = findViewById<RelativeLayout>(R.id.sliderContainer)
        val sliderHandle = findViewById<FrameLayout>(R.id.sliderHandle)
        val dividerLine = findViewById<View>(R.id.dividerLine)

        var currentSliderX = 0f

        imgAfter.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                // Clip imgAfter to only show the right side (from slider X to view width)
                val clipLeft = currentSliderX.toInt().coerceIn(0, view.width)
                outline.setRect(clipLeft, 0, view.width, view.height)
            }
        }
        imgAfter.clipToOutline = true

        sliderContainer.post {
            // Initial position (center)
            currentSliderX = sliderContainer.width / 2f
            imgAfter.invalidateOutline()
        }

        sliderContainer.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    // Restrict dragging within container bounds
                    currentSliderX = event.x.coerceIn(0f, v.width.toFloat())

                    // Move handle and divider
                    sliderHandle.translationX = currentSliderX - (v.width / 2f)
                    dividerLine.translationX = currentSliderX - (v.width / 2f)

                    // Re-calculate clip outline for imgAfter
                    imgAfter.invalidateOutline()
                    true
                }
                else -> false
            }
        }
    }
}