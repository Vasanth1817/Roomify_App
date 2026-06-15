package com.example.ar_app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class SavedDesignsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_designs)
        BottomNavHelper.setup(this)

        val emptyText = findViewById<TextView>(R.id.tvEmptyState)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewDesigns)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        findViewById<android.widget.ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // Fetch from python backend
        val prefs = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", null)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch Prices first to pass to adapter
                val priceMap = mutableMapOf<String, Float>()
                val furnitureResponse = RetrofitClient.instance.getFurniture().execute()
                if (furnitureResponse.isSuccessful && furnitureResponse.body() != null) {
                    furnitureResponse.body()!!.forEach { item ->
                        priceMap[item.gltfUrl] = item.parsedPrice.toFloat()
                    }
                }
                
                var urlString = "https://roomifybackend.onrender.com/get_layouts"
                if (userId != null) {
                    urlString += "?user_id=$userId"
                }
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    
                    // Parse the JSON array
                    val gson = com.google.gson.Gson()
                    val type = object : com.google.gson.reflect.TypeToken<MutableList<SavedDesign>>() {}.type
                    val designsList: MutableList<SavedDesign> = gson.fromJson(response, type)
                    
                    withContext(Dispatchers.Main) {
                        if (designsList.isEmpty()) {
                            emptyText.text = "No saved designs found."
                        } else {
                            emptyText.visibility = android.view.View.GONE
                            val adapter = SavedDesignsAdapter(designsList, priceMap) { design, action ->
                                if (action == "VIEW_AR") {
                                    // Clicked View In AR! Pass the entire JSON data to Unity to restore exact positions
                                    try {
                                        val intent = android.content.Intent(this@SavedDesignsActivity, ARViewerActivity::class.java)
                                        intent.putExtra("ROOM_LAYOUT_JSON", design.json_data)
                                        
                                        // WE ALSO NEED TO LAUNCH THE RIGHT MODE!
                                        val prefsVirtual = getSharedPreferences("VirtualRoomPrefs", MODE_PRIVATE)
                                        if (design.mode == "Virtual") {
                                            prefsVirtual.edit().putString("UNITY_SCENE", "VirtualRoomScene").apply()
                                        } else if (design.mode == "Snapshot") {
                                            prefsVirtual.edit().putString("UNITY_SCENE", "SnapshotScene").apply()
                                        } else {
                                            prefsVirtual.edit().putString("UNITY_SCENE", "TestAR").apply()
                                        }

                                        startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(this@SavedDesignsActivity, "Error parsing design: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } else if (action == "COMPARE") {
                                    if (design.before_image == null || design.after_image == null) {
                                        android.widget.Toast.makeText(this@SavedDesignsActivity, "No comparison images saved for this design.", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        val intent = android.content.Intent(this@SavedDesignsActivity, BeforeAfterActivity::class.java)
                                        intent.putExtra("BEFORE_IMAGE_B64", design.before_image)
                                        intent.putExtra("AFTER_IMAGE_B64", design.after_image)
                                        
                                        intent.putExtra("DESIGN_NAME", design.name)
                                        
                                        try {
                                            val jsonObject = org.json.JSONObject(design.json_data)
                                            val itemsArray = jsonObject.getJSONArray("items")
                                            intent.putExtra("ITEM_COUNT", itemsArray.length())
                                            
                                            var cost = 0f
                                            for (i in 0 until itemsArray.length()) {
                                                val itemObj = itemsArray.getJSONObject(i)
                                                val url = itemObj.optString("model_url")
                                                cost += priceMap[url] ?: 0f
                                            }
                                            intent.putExtra("BUDGET", cost)
                                        } catch (e: Exception) {
                                            intent.putExtra("ITEM_COUNT", 0)
                                            intent.putExtra("BUDGET", 0f)
                                        }
                                        
                                        startActivity(intent)
                                    }
                                }
                            }
                            recyclerView.adapter = adapter

                            val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
                                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false
                                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                                    val position = viewHolder.adapterPosition
                                    val designToDelete = designsList[position]

                                    android.app.AlertDialog.Builder(this@SavedDesignsActivity)
                                        .setTitle("Delete Design")
                                        .setMessage("Are you sure you want to delete this design?")
                                        .setPositiveButton("Delete") { _, _ ->
                                            adapter.removeAt(position)
                                            RetrofitClient.instance.deleteLayout(designToDelete.id).enqueue(object : retrofit2.Callback<Void> {
                                                override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                                                    if (!response.isSuccessful) {
                                                        android.widget.Toast.makeText(this@SavedDesignsActivity, "Failed to delete from server: HTTP ${response.code()}", android.widget.Toast.LENGTH_LONG).show()
                                                    } else {
                                                        android.widget.Toast.makeText(this@SavedDesignsActivity, "Design deleted permanently", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                                                    android.widget.Toast.makeText(this@SavedDesignsActivity, "Network Error: ${t.message}", android.widget.Toast.LENGTH_LONG).show()
                                                }
                                            })
                                            if (designsList.isEmpty()) {
                                                emptyText.visibility = android.view.View.VISIBLE
                                                emptyText.text = "No saved designs found."
                                            }
                                        }
                                        .setNegativeButton("Cancel") { dialog, _ ->
                                            adapter.notifyItemChanged(position)
                                            dialog.dismiss()
                                        }
                                        .setCancelable(false)
                                        .show()
                                }
                            })
                            itemTouchHelper.attachToRecyclerView(recyclerView)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        emptyText.text = "Error connecting to server. (HTTP ${connection.responseCode})"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    emptyText.text = "Failed to connect to Python backend.\n\nError: ${e.message}"
                }
            }
        }
    }
}
