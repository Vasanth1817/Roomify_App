package com.example.ar_app

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowInsetsCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

class Home : AppCompatActivity() {

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                // Copy the selected image to a temporary file so Unity can read it via File.ReadAllBytes
                val inputStream = contentResolver.openInputStream(uri)
                val tempFile = File(cacheDir, "snapshot_bg.jpg")
                val outputStream = FileOutputStream(tempFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                val prefs = getSharedPreferences("VirtualRoomPrefs", MODE_PRIVATE)
                prefs.edit()
                    .putString("UNITY_SCENE", "SnapshotScene")
                    .putString("SNAPSHOT_PHOTO_PATH", tempFile.absolutePath)
                    .apply()

                val intent = Intent(this, FurnitureCatalogActivity::class.java)
                intent.putExtra("MODE", "SNAPSHOT")
                startActivity(intent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(this, "Error loading image", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var photoUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val prefs = getSharedPreferences("VirtualRoomPrefs", MODE_PRIVATE)
            val tempFile = File(cacheDir, "snapshot_bg.jpg")
            prefs.edit()
                .putString("UNITY_SCENE", "SnapshotScene")
                .putString("SNAPSHOT_PHOTO_PATH", tempFile.absolutePath)
                .apply()

            val intent = Intent(this, FurnitureCatalogActivity::class.java)
            intent.putExtra("MODE", "SNAPSHOT")
            startActivity(intent)
        } else {
            android.widget.Toast.makeText(this, "Failed to capture photo", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // ===== CLICK EVENTS =====
        findViewById<View>(R.id.cardTutorial).setOnClickListener {
            startActivity(Intent(this, TutorialActivity::class.java))
        }

        findViewById<View>(R.id.btnLaunchAR).setOnClickListener {
            val prefs = getSharedPreferences("VirtualRoomPrefs", MODE_PRIVATE)
            prefs.edit().putString("UNITY_SCENE", "TestAR").apply()
            startActivity(Intent(this, FurnitureCatalogActivity::class.java))
        }

        findViewById<View>(R.id.btnEdit).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }

        findViewById<View>(R.id.cardBudget).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }

        // Setup global bottom navigation
        BottomNavHelper.setup(this)

        findViewById<android.widget.ImageView>(R.id.navProfile).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // ===== STATUS BAR PADDING =====
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // ===== FADE IN SCREEN =====
        val main = findViewById<View>(R.id.main)

        main.alpha = 0f

        main.animate()
            .alpha(1f)
            .setDuration(800)
            .start()

        // ===== FLOATING BUDGET CARD =====
        val budgetCard = findViewById<CardView>(R.id.budgetCard)

        val floating = ObjectAnimator.ofFloat(
            budgetCard,
            "translationY",
            0f,
            -15f
        )

        floating.duration = 4000
        floating.repeatCount = ObjectAnimator.INFINITE
        floating.repeatMode = ObjectAnimator.REVERSE

        floating.start()

        findViewById<View>(R.id.cardVirtualRoom).setOnClickListener {
            startActivity(Intent(this, VirtualRoomInputActivity::class.java))
        }

        findViewById<View>(R.id.cardSnapshot).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        val arCard = findViewById<androidx.cardview.widget.CardView>(R.id.cardTutorial)
        val snapshotCard = findViewById<androidx.cardview.widget.CardView>(R.id.cardSnapshot)
        val virtualRoomCard = findViewById<androidx.cardview.widget.CardView>(R.id.cardVirtualRoom)
        val budgetQuickCard = findViewById<androidx.cardview.widget.CardView>(R.id.cardBudget)

        apply3DEffect(arCard)
        apply3DEffect(snapshotCard)
        apply3DEffect(virtualRoomCard)
        apply3DEffect(budgetQuickCard)

        // ===== BUTTON PRESS EFFECT =====
        val launchBtn = findViewById<View>(R.id.btnLaunchAR)

        launchBtn.setOnTouchListener { v, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .start()
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {

                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
            }

            false
        }
    }

    override fun onResume() {
        super.onResume()
        // Update Budget info from SharedPreferences
        val prefs = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val maxBudget = prefs.getFloat("MAX_BUDGET", 450000f)
        val userId = prefs.getString("USER_ID", null)

        val tvRemainingBudgetHome = findViewById<android.widget.TextView>(R.id.tvRemainingBudgetHome)
        val tvTotalBudgetHome = findViewById<android.widget.TextView>(R.id.tvTotalBudgetHome)
        val tvTotalSpentHome = findViewById<android.widget.TextView>(R.id.tvTotalSpentHome)

        if (tvRemainingBudgetHome != null) {
            tvRemainingBudgetHome.text = String.format(java.util.Locale("en", "IN"), "₹%,.2f", maxBudget)
        }
        if (tvTotalBudgetHome != null) {
            tvTotalBudgetHome.text = "₹0.00" // Temporary until fetched
        }
        if (tvTotalSpentHome != null) {
            tvTotalSpentHome.text = "₹0.00" // Temporary until fetched
        }
        
        if (userId != null) {
            fetchActiveProjectBudget(userId, maxBudget, tvRemainingBudgetHome, tvTotalBudgetHome, tvTotalSpentHome)
        }
    }

    private fun fetchActiveProjectBudget(
        userId: String,
        fallbackMaxBudget: Float,
        tvRemainingBudgetHome: android.widget.TextView?,
        tvTotalBudgetHome: android.widget.TextView?,
        tvTotalSpentHome: android.widget.TextView?
    ) {
        // 1. Fetch Latest Budget
        RetrofitClient.instance.getBudget(userId).enqueue(object : retrofit2.Callback<BudgetResponse> {
            override fun onResponse(call: retrofit2.Call<BudgetResponse>, response: retrofit2.Response<BudgetResponse>) {
                val maxBudget = if (response.isSuccessful && response.body() != null) {
                    val apiBudget = response.body()!!.max_budget
                    val prefs = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putFloat("MAX_BUDGET", apiBudget).apply()
                    apiBudget
                } else {
                    fallbackMaxBudget
                }

                // 2. Fetch Furniture Catalog
                RetrofitClient.instance.getFurniture().enqueue(object : retrofit2.Callback<List<FurnitureItem>> {
                    override fun onResponse(call: retrofit2.Call<List<FurnitureItem>>, response: retrofit2.Response<List<FurnitureItem>>) {
                        if (response.isSuccessful && response.body() != null) {
                            val catalog = response.body()!!
                            val priceMap = catalog.associate { it.gltfUrl to it.parsedPrice.toFloat() }
                            
                            // 3. Fetch layout
                            RetrofitClient.instance.getLayouts(userId).enqueue(object : retrofit2.Callback<List<SavedLayout>> {
                                override fun onResponse(call: retrofit2.Call<List<SavedLayout>>, response: retrofit2.Response<List<SavedLayout>>) {
                                    var totalSpent = 0f
                                    if (response.isSuccessful && response.body() != null) {
                                        val layouts = response.body()!!
                                        if (layouts.isNotEmpty()) {
                                            val latestLayout = layouts.last()
                                            try {
                                                val jsonObject = org.json.JSONObject(latestLayout.json_data)
                                                val itemsArray = jsonObject.optJSONArray("items")
                                                if (itemsArray != null) {
                                                    for (i in 0 until itemsArray.length()) {
                                                        val itemObj = itemsArray.getJSONObject(i)
                                                        val url = itemObj.optString("model_url", "")
                                                        if (url.isNotEmpty() && priceMap.containsKey(url)) {
                                                            totalSpent += priceMap[url] ?: 0f
                                                        }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }

                                    val remaining = maxBudget - totalSpent
                                    
                                    // User requested swapped layout:
                                    // Big Text (tvRemainingBudgetHome) -> Total Budget
                                    // Small Left (tvTotalBudgetHome) -> Remaining Budget
                                    // Small Right (tvTotalSpentHome) -> Total Spent
                                    runOnUiThread {
                                        tvRemainingBudgetHome?.text = String.format(java.util.Locale("en", "IN"), "₹%,.2f", maxBudget)
                                        tvTotalBudgetHome?.text = String.format(java.util.Locale("en", "IN"), "₹%,.2f", remaining)
                                        tvTotalSpentHome?.text = String.format(java.util.Locale("en", "IN"), "₹%,.2f", totalSpent)
                                    }
                                }
                                override fun onFailure(call: retrofit2.Call<List<SavedLayout>>, t: Throwable) {}
                            })
                        }
                    }
                    override fun onFailure(call: retrofit2.Call<List<FurnitureItem>>, t: Throwable) {}
                })
            }
            override fun onFailure(call: retrofit2.Call<BudgetResponse>, t: Throwable) {}
        })
    }

    // ===== 3D CARD EFFECT =====
    private fun apply3DEffect(card: CardView) {

        card.setOnTouchListener { v, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    v.animate()
                        .rotationX(8f)
                        .rotationY(-8f)
                        .scaleX(0.96f)
                        .scaleY(0.96f)
                        .setDuration(120)
                        .start()
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {

                    v.animate()
                        .rotationX(0f)
                        .rotationY(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120)
                        .start()
                }
            }

            false
        }
    }
}