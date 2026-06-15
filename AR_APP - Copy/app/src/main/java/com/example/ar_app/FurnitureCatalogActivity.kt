package com.example.ar_app

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FurnitureCatalogActivity : AppCompatActivity() {

    private lateinit var rvFurniture: RecyclerView
    private lateinit var adapter: FurnitureAdapter
    private lateinit var tvRemainingBudget: TextView
    private lateinit var tvTotalSpent: TextView
    private lateinit var tvItemCount: TextView
    private lateinit var tvSelectedPieces: TextView
    private lateinit var btnViewInAR: Button
    private lateinit var llCategories: android.widget.LinearLayout
    private lateinit var etSearch: android.widget.EditText

    private var totalBudget = 0.0
    private var spent = 0.0

    // All items from the backend
    private var furnitureList = mutableListOf<FurnitureItem>()
    
    // Items currently shown in the RecyclerView (filtered)
    private var displayedFurnitureList = mutableListOf<FurnitureItem>()

    // Track exact chronological selection order
    private var orderedSelectedUrls = mutableListOf<String>()

    // Currently selected category
    private var selectedCategory = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_furniture_catalog)

        val prefs = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        totalBudget = prefs.getFloat("MAX_BUDGET", 450000f).toDouble()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Init views
        rvFurniture = findViewById(R.id.rvFurniture)
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget)
        tvTotalSpent = findViewById(R.id.tvTotalSpent)
        tvItemCount = findViewById(R.id.tvItemCount)
        tvSelectedPieces = findViewById(R.id.tvSelectedPieces)
        btnViewInAR = findViewById(R.id.btnViewInAR)
        llCategories = findViewById(R.id.llCategories)
        etSearch = findViewById(R.id.etSearch)

        // Setup Search Bar
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Setup RecyclerView
        rvFurniture.layoutManager = GridLayoutManager(this, 2)
        adapter = FurnitureAdapter(displayedFurnitureList) { item, position ->
            toggleItemSelection(item, position)
        }
        rvFurniture.adapter = adapter

        btnViewInAR.setOnClickListener {
            val validUrls = orderedSelectedUrls.filter { it.isNotEmpty() }
            
            if (validUrls.isNotEmpty()) {
                Toast.makeText(this, "Loading " + validUrls.size + " items in AR...", Toast.LENGTH_LONG).show()
                val urlsString = validUrls.joinToString(",")
                val intent = Intent(this, ARViewerActivity::class.java)
                intent.putExtra("MODEL_URL", urlsString) // Pass the full comma-separated list!
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select at least one item with a valid 3D model", Toast.LENGTH_SHORT).show()
            }
        }

        updateBudgetUI()
        fetchFurnitureFromBackend()
    }

    private fun fetchFurnitureFromBackend() {
        Toast.makeText(this, "Loading from backend...", Toast.LENGTH_SHORT).show()
        RetrofitClient.instance.getFurniture().enqueue(object : retrofit2.Callback<List<FurnitureItem>> {
            override fun onResponse(
                call: retrofit2.Call<List<FurnitureItem>>,
                response: retrofit2.Response<List<FurnitureItem>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    furnitureList.clear()
                    furnitureList.addAll(response.body()!!)
                    
                    // Extract unique categories and build UI
                    buildCategoryChips()
                    
                    // Apply current filter
                    filterDataByCategory(selectedCategory)
                    
                    updateBudgetUI()
                } else {
                    Toast.makeText(this@FurnitureCatalogActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<List<FurnitureItem>>, t: Throwable) {
                Toast.makeText(this@FurnitureCatalogActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun buildCategoryChips() {
        llCategories.removeAllViews()
        
        // Get unique categories from the data, ignoring empty ones
        val categories = mutableListOf("All")
        categories.addAll(furnitureList.map { it.category }.filter { it.isNotEmpty() }.distinct())
        
        for (category in categories) {
            val tv = TextView(this)
            tv.text = category
            tv.textSize = 12f
            
            val params = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // Add some margin between chips
            params.setMargins(0, 0, 48, 0) // 16dp roughly
            tv.layoutParams = params
            
            // Set initial style
            updateChipStyle(tv, category == selectedCategory)
            
            tv.setOnClickListener {
                selectedCategory = category
                // Update all styles
                for (i in 0 until llCategories.childCount) {
                    val child = llCategories.getChildAt(i) as TextView
                    updateChipStyle(child, child.text == selectedCategory)
                }
                filterDataByCategory(selectedCategory)
            }
            
            llCategories.addView(tv)
        }
    }
    
    private fun updateChipStyle(tv: TextView, isSelected: Boolean) {
        val px60 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, resources.displayMetrics).toInt()
        val px24 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt()

        if (isSelected) {
            tv.setBackgroundResource(R.drawable.bg_circle_purple)
            tv.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            tv.setTypeface(null, android.graphics.Typeface.BOLD)
            tv.setPadding(px60, px24, px60, px24) // 20dp, 8dp
        } else {
            tv.background = null
            tv.setTextColor(android.graphics.Color.parseColor("#666666"))
            tv.setTypeface(null, android.graphics.Typeface.NORMAL)
            tv.setPadding(0, px24, 0, px24)
        }
    }

    private fun filterDataByCategory(category: String) {
        selectedCategory = category
        filterData()
    }

    private fun filterData() {
        val query = etSearch.text.toString().trim().lowercase()
        
        displayedFurnitureList.clear()
        
        val filteredList = furnitureList.filter { item ->
            val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
            val matchesQuery = query.isEmpty() || item.name.lowercase().contains(query) || item.category.lowercase().contains(query)
            matchesCategory && matchesQuery
        }
        
        displayedFurnitureList.addAll(filteredList)
        adapter.notifyDataSetChanged()
    }

    private fun toggleItemSelection(item: FurnitureItem, position: Int) {
        if (item.isSelected) {
            item.isSelected = false
            spent -= item.parsedPrice
            if (item.gltfUrl != null) {
                orderedSelectedUrls.remove(item.gltfUrl)
            }
        } else {
            if (spent + item.parsedPrice > totalBudget) {
                Toast.makeText(this, "Cannot add! Exceeds budget.", Toast.LENGTH_SHORT).show()
                return
            }
            item.isSelected = true
            spent += item.parsedPrice
            if (item.gltfUrl != null && item.gltfUrl.isNotEmpty() && !orderedSelectedUrls.contains(item.gltfUrl)) {
                orderedSelectedUrls.add(item.gltfUrl)
            }
        }

        // Update Over Budget states for all items based on new remaining budget
        val remaining = totalBudget - spent
        furnitureList.forEach {
            it.isOverBudget = !it.isSelected && it.parsedPrice > remaining
        }

        // The adapter already references displayedFurnitureList, so we just notify it
        adapter.notifyDataSetChanged()
        updateBudgetUI()
    }

    private fun updateBudgetUI() {
        val remaining = totalBudget - spent
        val selectedCount = furnitureList.count { it.isSelected }

        tvRemainingBudget.text = String.format(java.util.Locale("en", "IN"), "₹%,.2f", remaining)
        tvTotalSpent.text = String.format(java.util.Locale("en", "IN"), "₹%,.2f", spent)
        tvItemCount.text = selectedCount.toString()
        
        tvSelectedPieces.text = "$selectedCount Pieces"
    }
}
