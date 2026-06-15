package com.example.ar_app

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AlertsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alerts)
        BottomNavHelper.setup(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnSettings = findViewById<android.widget.ImageView>(R.id.btnSettings)
        btnSettings.setOnClickListener {
            val intent = android.content.Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val alertsContainer = findViewById<LinearLayout>(R.id.alertsContainer)
        val tvEmptyState = findViewById<TextView>(R.id.tvEmptyState)

        // Dynamic Alerts Based on User Prefs
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("NOTIFICATIONS_ENABLED", true)

        if (!notificationsEnabled) {
            tvEmptyState.visibility = View.VISIBLE
            tvEmptyState.text = "Notifications are disabled in Settings."
        } else {
            val alerts = AlertManager.getAlerts(this)
            
            if (alerts.isEmpty()) {
                tvEmptyState.visibility = View.VISIBLE
                tvEmptyState.text = "No new alerts."
            } else {
                tvEmptyState.visibility = View.GONE
                
                for (alert in alerts) {
                    val card = layoutInflater.inflate(R.layout.item_alert, alertsContainer, false) as androidx.cardview.widget.CardView
                    card.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 0, 0, 30) }

                    val tvTitle = card.findViewById<TextView>(R.id.tvAlertTitle)
                    val tvDesc = card.findViewById<TextView>(R.id.tvAlertDesc)
                    val tvTime = card.findViewById<TextView>(R.id.tvAlertTime)
                    val imgIcon = card.findViewById<android.widget.ImageView>(R.id.imgAlertIcon)

                    tvTitle.text = alert.title
                    tvDesc.text = alert.message
                    tvTime.text = alert.time
                    
                    if (alert.title.contains("Budget")) {
                        tvTitle.setTextColor(android.graphics.Color.parseColor("#F97316"))
                        imgIcon.setImageResource(R.drawable.ic_budget)
                    } else if (alert.title.contains("Theme")) {
                        imgIcon.setImageResource(R.drawable.ic_explore)
                    } else {
                        imgIcon.setImageResource(R.drawable.ic_notifications)
                    }

                    alertsContainer.addView(card)
                }
            }
        }
    }
}
