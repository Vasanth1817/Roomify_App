package com.example.ar_app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unity3d.player.UnityPlayer
import com.unity3d.player.UnityPlayerGameActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ARViewerActivity : UnityPlayerGameActivity() {

    private val downloadedUrls = mutableSetOf<String>()
    
    // Native UI Overlay variables (Replacing BottomSheetDialog to prevent ARCore crash!)
    private lateinit var overlayContainer: FrameLayout
    private lateinit var bottomSheetView: View
    private val catalogFurnitureList = mutableListOf<FurnitureItem>()
    private lateinit var catalogAdapter: FurnitureAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if we are loading a full saved room layout!
        val roomLayoutJson = intent.getStringExtra("ROOM_LAYOUT_JSON")
        if (roomLayoutJson != null) {
            Toast.makeText(this, "Initializing AR... Room will load in 6 seconds!", Toast.LENGTH_LONG).show()
            Handler(Looper.getMainLooper()).postDelayed({
                Toast.makeText(this, "Restoring your room layout now...", Toast.LENGTH_LONG).show()
                UnityPlayer.UnitySendMessage("AndroidBridge", "LoadRoom", roomLayoutJson)
            }, 6000L) // Wait 6 seconds for Unity to fully load the scene and ARCore
        }

        // Send User ID and Budget to Unity for saving layouts
        val prefs = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", null)
        val maxBudget = prefs.getFloat("MAX_BUDGET", 0f)
        
        if (userId != null) {
            // Wait slightly for Unity to initialize its scripts
            Handler(Looper.getMainLooper()).postDelayed({
                UnityPlayer.UnitySendMessage("AndroidBridge", "SetUserId", userId)
                UnityPlayer.UnitySendMessage("AndroidBridge", "SetMaxBudget", maxBudget.toString())
            }, 2000L)
        }

        // 1. Get initial URL
        val modelUrl = intent.getStringExtra("MODEL_URL")

        if (modelUrl != null) {
            val urls = modelUrl.split(",")
            for ((index, url) in urls.withIndex()) {
                val cleanUrl = url.trim()
                if (cleanUrl.isNotBlank() && !downloadedUrls.contains(cleanUrl)) {
                    downloadedUrls.add(cleanUrl) 
                    Handler(Looper.getMainLooper()).postDelayed({
                        val isLast = index == urls.size - 1
                        val msg = if (isLast) "Downloading Item ${index + 1}... Please wait 5 seconds then tap screen!" else "Downloading Item ${index + 1}..."
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        UnityPlayer.UnitySendMessage("AndroidBridge", "ReceiveModelUrl", cleanUrl)
                    }, 3000L + (index * 2000L)) 
                }
            }
        }

        val backButton = Button(this).apply {
            text = "⬅ Back"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#80000000")) 
            setPadding(40, 20, 40, 20)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            
            val background = android.graphics.drawable.GradientDrawable()
            background.setColor(Color.parseColor("#99000000"))
            background.cornerRadius = 50f
            this.background = background

            setOnClickListener {
                finish()
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }

        val backParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            setMargins(50, 120, 0, 0)
        }
        addContentView(backButton, backParams)

        val addButton = Button(this).apply {
            text = "➕ Add Furniture"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#FF5722")) 
            setPadding(60, 30, 60, 30)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            
            val background = android.graphics.drawable.GradientDrawable()
            background.setColor(Color.parseColor("#FF5722"))
            background.cornerRadius = 50f
            this.background = background

            setOnClickListener {
                showCatalogOverlay()
            }
        }

        val addParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            setMargins(0, 0, 0, 100)
        }
        addContentView(addButton, addParams)
        
        setupNativeOverlay()
        fetchFurnitureCatalog()
    }
    
    private fun setupNativeOverlay() {
        // Create a full-screen semi-transparent overlay
        overlayContainer = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            // CRITICAL FIX: Use View.INVISIBLE instead of GONE so we don't recreate SurfaceView,
            // but we must use INVISIBLE so it doesn't intercept AR touches!
            visibility = View.INVISIBLE
            alpha = 0f
            translationY = 2000f
            setBackgroundColor(Color.parseColor("#80000000"))
            
            // Clicking the dark background closes the menu
            setOnClickListener {
                closeCatalogOverlay()
            }
        }
        
        // Inflate the actual catalog layout
        bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_catalog, null)
        
        // Prevent clicks on the white menu from passing through and closing the overlay or hitting ARCore
        bottomSheetView.setOnClickListener { }
        
        val rvBottomSheetFurniture = bottomSheetView.findViewById<RecyclerView>(R.id.rvBottomSheetFurniture)
        rvBottomSheetFurniture.layoutManager = GridLayoutManager(this, 2)
        
        catalogAdapter = FurnitureAdapter(catalogFurnitureList) { selectedItem, position ->
            if (!downloadedUrls.contains(selectedItem.gltfUrl)) {
                downloadedUrls.add(selectedItem.gltfUrl)
                
                Toast.makeText(this, "Downloading ${selectedItem.name}...", Toast.LENGTH_LONG).show()
                UnityPlayer.UnitySendMessage("AndroidBridge", "ReceiveModelUrl", selectedItem.gltfUrl)
                
                closeCatalogOverlay()
            } else {
                Toast.makeText(this, "${selectedItem.name} is already in the room!", Toast.LENGTH_SHORT).show()
            }
        }
        rvBottomSheetFurniture.adapter = catalogAdapter
        
        // Position it at the bottom
        val sheetParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM
        }
        
        overlayContainer.addView(bottomSheetView, sheetParams)
        
        // Add the entire overlay natively to the Unity Activity
        addContentView(overlayContainer, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
    }

    private fun showCatalogOverlay() {
        if (catalogFurnitureList.isNotEmpty()) {
            // Bring container on-screen and fade in the dark background
            overlayContainer.visibility = View.VISIBLE
            overlayContainer.translationY = 0f
            overlayContainer.animate().alpha(1f).setDuration(300).start()
            
            // Slide up animation for the white menu
            bottomSheetView.translationY = 2000f
            bottomSheetView.animate().translationY(0f).setDuration(300).start()
        } else {
            Toast.makeText(this, "Loading catalog, please wait...", Toast.LENGTH_SHORT).show()
            fetchFurnitureCatalog()
        }
    }

    private fun closeCatalogOverlay() {
        // Slide down the white menu
        bottomSheetView.animate().translationY(2000f).setDuration(300).start()
        
        // Fade out the dark background, then move the entire container off-screen
        overlayContainer.animate().alpha(0f).setDuration(300).withEndAction {
            overlayContainer.translationY = 2000f
            overlayContainer.visibility = View.INVISIBLE
        }.start()
    }

    private fun fetchFurnitureCatalog() {
        RetrofitClient.instance.getFurniture().enqueue(object : Callback<List<FurnitureItem>> {
            override fun onResponse(
                call: Call<List<FurnitureItem>>,
                response: Response<List<FurnitureItem>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    catalogFurnitureList.clear()
                    catalogFurnitureList.addAll(response.body()!!)
                    catalogAdapter.notifyDataSetChanged()
                    
                    // Convert catalog to JSON and send to Unity for budget calculation!
                    val gson = com.google.gson.Gson()
                    val unityList = catalogFurnitureList.map { 
                        mapOf(
                            "id" to it.id,
                            "name" to it.name,
                            "category" to it.category,
                            "price" to it.parsedPrice.toString(), 
                            "thumbnail_url" to it.imageUrl,
                            "model_url" to it.gltfUrl
                        )
                    }
                    val jsonStr = gson.toJson(unityList)
                    UnityPlayer.UnitySendMessage("AndroidBridge", "SetCatalogJson", jsonStr)
                }
            }

            override fun onFailure(call: Call<List<FurnitureItem>>, t: Throwable) {
                Toast.makeText(this@ARViewerActivity, "Failed to load catalog", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun promptForRoomName() {
        runOnUiThread {
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Save Design")
            builder.setMessage("Enter a name for your design:")

            val input = android.widget.EditText(this)
            input.inputType = android.text.InputType.TYPE_CLASS_TEXT
            
            // Set some margins for better looks
            val lp = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(50, 0, 50, 0)
            input.layoutParams = lp
            
            val container = android.widget.LinearLayout(this)
            container.orientation = android.widget.LinearLayout.VERTICAL
            container.addView(input)
            builder.setView(container)

            builder.setPositiveButton("Save") { dialog, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    UnityPlayer.UnitySendMessage("UIManager", "OnRoomNameReceived", name)
                } else {
                    UnityPlayer.UnitySendMessage("UIManager", "OnRoomNameReceived", "My Room Design")
                }
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }
    }
}
