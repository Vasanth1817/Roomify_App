package com.example.ar_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Toast
import android.view.LayoutInflater
import org.json.JSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BudgetActivity : AppCompatActivity() {

    private lateinit var etTargetBudget: EditText
    private lateinit var btnSaveBudget: Button
    private lateinit var seekBudget: SeekBar

    private var totalSpentAllProjects: Float = 0f
    private var lastProjectCost: Float = 0f
    private var initialMaxBudget: Float = 0f
    private val chips = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etTargetBudget = findViewById(R.id.etTargetBudget)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)
        seekBudget = findViewById(R.id.seekBudget)

        val prefs = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", null)

        if (userId != null) {
            fetchBudget(userId)
            fetchBreakdown(userId)
        }
        
        setupChips()
        
        // Link SeekBar to EditText
        seekBudget.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    etTargetBudget.setText(progress.toString())
                    etTargetBudget.setSelection(etTargetBudget.text.length)
                }
                updateDynamicViews(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Link EditText to SeekBar
        etTargetBudget.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (etTargetBudget.hasFocus()) {
                    val value = s.toString().toIntOrNull() ?: 0
                    if (value > seekBudget.max) {
                        seekBudget.max = value
                    }
                    seekBudget.progress = value
                }
            }
        })

        btnSaveBudget.setOnClickListener {
            if (userId != null) {
                val budgetStr = etTargetBudget.text.toString()
                val budgetFloat = budgetStr.toFloatOrNull()
                if (budgetFloat != null) {
                    saveBudget(userId, budgetFloat)
                } else {
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "You must be logged in to save a budget.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchBudget(userId: String) {
        RetrofitClient.instance.getBudget(userId).enqueue(object : Callback<BudgetResponse> {
            override fun onResponse(call: Call<BudgetResponse>, response: Response<BudgetResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val maxBudget = response.body()!!.max_budget
                    initialMaxBudget = maxBudget
                    if (maxBudget > seekBudget.max) {
                        seekBudget.max = maxBudget.toInt()
                    }
                    etTargetBudget.setText(maxBudget.toString())
                    seekBudget.progress = maxBudget.toInt()
                    btnSaveBudget.isEnabled = false
                    
                    // Save to local prefs for ARViewerActivity
                    val prefs = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putFloat("MAX_BUDGET", maxBudget).apply()
                }
            }
            override fun onFailure(call: Call<BudgetResponse>, t: Throwable) {
                Toast.makeText(this@BudgetActivity, "Failed to load budget", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveBudget(userId: String, budget: Float) {
        btnSaveBudget.isEnabled = false
        btnSaveBudget.text = "Saving..."
        
        val request = BudgetRequest(userId, budget)
        RetrofitClient.instance.updateBudget(request).enqueue(object : Callback<BudgetResponse> {
            override fun onResponse(call: Call<BudgetResponse>, response: Response<BudgetResponse>) {
                btnSaveBudget.isEnabled = true
                btnSaveBudget.text = "Save Budget"
                if (response.isSuccessful) {
                    Toast.makeText(this@BudgetActivity, "Budget Saved!", Toast.LENGTH_SHORT).show()
                    initialMaxBudget = budget
                    updateDynamicViews(budget)
                    
                    AlertManager.addAlert(this@BudgetActivity, "Budget Updated", "Your new maximum budget is ₹$budget")
                    
                    // Save to local prefs for ARViewerActivity
                    val prefs = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putFloat("MAX_BUDGET", budget).apply()
                } else {
                    Toast.makeText(this@BudgetActivity, "Failed to save budget", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<BudgetResponse>, t: Throwable) {
                btnSaveBudget.isEnabled = true
                btnSaveBudget.text = "Save Budget"
                Toast.makeText(this@BudgetActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchBreakdown(userId: String) {
        val llContainer = findViewById<LinearLayout>(R.id.llProjectBreakdownContainer)
        val tvTotalBudget = findViewById<TextView>(R.id.tvTotalBudget)
        val tvSpent = findViewById<TextView>(R.id.tvSpent)
        val tvRemaining = findViewById<TextView>(R.id.tvRemaining)
        val pbBudget = findViewById<android.widget.ProgressBar>(R.id.progressBudget)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Fetch Prices
                val priceMap = mutableMapOf<String, Float>()
                val furnitureResponse = RetrofitClient.instance.getFurniture().execute()
                if (furnitureResponse.isSuccessful && furnitureResponse.body() != null) {
                    furnitureResponse.body()!!.forEach { item ->
                        priceMap[item.gltfUrl] = item.parsedPrice.toFloat()
                    }
                }

                // 2. Fetch Layouts
                val layoutsResponse = RetrofitClient.instance.getLayouts(userId).execute()
                if (layoutsResponse.isSuccessful && layoutsResponse.body() != null) {
                    val layouts = layoutsResponse.body()!!
                    totalSpentAllProjects = 0f
                    
                    val projectCosts = mutableListOf<Pair<String, Float>>()

                    for (layout in layouts) {
                        var cost = 0f
                        try {
                            val jsonObject = JSONObject(layout.json_data)
                            val itemsArray = jsonObject.optJSONArray("items")
                            if (itemsArray != null) {
                                for (i in 0 until itemsArray.length()) {
                                    val itemObj = itemsArray.getJSONObject(i)
                                    val url = itemObj.optString("model_url")
                                    cost += priceMap[url] ?: 0f
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        totalSpentAllProjects += cost
                        lastProjectCost = cost
                        projectCosts.add(Pair("${layout.name} #${layout.id}", cost))
                    }

                    withContext(Dispatchers.Main) {
                        llContainer.removeAllViews()
                        for (project in projectCosts) {
                            val view = LayoutInflater.from(this@BudgetActivity).inflate(R.layout.item_budget_breakdown, llContainer, false)
                            val tvName = view.findViewById<TextView>(R.id.tvProjectName)
                            val tvCost = view.findViewById<TextView>(R.id.tvProjectCost)
                            tvName.text = project.first
                            tvCost.text = String.format(java.util.Locale("en", "IN"), "₹%,.2f", project.second)
                            llContainer.addView(view)
                        }

                        val tvCurrentSpending = findViewById<TextView>(R.id.tvCurrentSpending)
                        tvCurrentSpending?.text = String.format(java.util.Locale("en", "IN"), "₹%,.2f", totalSpentAllProjects)

                        if (projectCosts.isNotEmpty()) {
                            val tvPreviewCaption = findViewById<TextView>(R.id.tvPreviewCaption)
                            tvPreviewCaption?.text = "Previewing: ${projectCosts.last().first}"
                        } else {
                            findViewById<View>(R.id.cvRoomPreview)?.visibility = View.GONE
                        }

                        updateDynamicViews(seekBudget.progress.toFloat())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupChips() {
        val chipIds = listOf(R.id.chip5k, R.id.chip25k, R.id.chip50k, R.id.chip75k, R.id.chip100k, R.id.chip150k)
        val chipValues = listOf(5000, 25000, 50000, 75000, 100000, 150000)
        
        for (i in chipIds.indices) {
            val chip = findViewById<TextView>(chipIds[i])
            if (chip != null) {
                chips.add(chip)
                chip.setOnClickListener {
                    seekBudget.progress = chipValues[i]
                    etTargetBudget.setText(chipValues[i].toString())
                    updateChipUI(chip)
                }
            }
        }
    }

    private fun updateChipUI(selectedChip: TextView?) {
        for (chip in chips) {
            if (chip == selectedChip) {
                chip.setBackgroundResource(R.drawable.bg_chip_solid)
                chip.setTextColor(android.graphics.Color.WHITE)
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_outline)
                chip.setTextColor(android.graphics.Color.parseColor("#6C2BD9"))
            }
        }
    }

    private fun updateDynamicViews(maxBudget: Float) {
        val tvTotalBudget = findViewById<TextView>(R.id.tvTotalBudget)
        val tvSpent = findViewById<TextView>(R.id.tvSpent)
        val tvRemaining = findViewById<TextView>(R.id.tvRemaining)
        val pbBudget = findViewById<android.widget.ProgressBar>(R.id.progressBudget)
        val tvWarningText = findViewById<TextView>(R.id.tvWarningText)
        val tvWarningRatio = findViewById<TextView>(R.id.tvWarningRatio)

        tvTotalBudget?.text = String.format(java.util.Locale("en", "IN"), "₹%,.2f", maxBudget)
        tvSpent?.text = String.format(java.util.Locale("en", "IN"), "₹%,.2f", lastProjectCost)
        
        val remaining = maxBudget - lastProjectCost
        tvRemaining?.text = String.format(java.util.Locale("en", "IN"), "₹%,.2f", remaining)
        if (remaining < 0) {
            tvRemaining?.setTextColor(android.graphics.Color.parseColor("#EF4444")) // Red
        } else {
            tvRemaining?.setTextColor(android.graphics.Color.parseColor("#22C55E")) // Green
        }

        val spentForBar = maxBudget - remaining
        val progress = if (maxBudget > 0) ((spentForBar / maxBudget) * 100).toInt() else 0
        val clampedProgress = Math.min(Math.max(progress, 0), 100)
        pbBudget?.progress = clampedProgress
        
        tvWarningText?.text = "⚠️ Warning: $clampedProgress% of budget reached"
        tvWarningRatio?.text = "$clampedProgress/100"
        
        if (maxBudget != initialMaxBudget) {
            btnSaveBudget.isEnabled = true
        } else {
            btnSaveBudget.isEnabled = false
        }
    }

    fun back(view: View) {
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
    }

    fun setting(view: View){
        val intent=Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}
