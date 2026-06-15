package com.example.ar_app

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity

object BottomNavHelper {
    fun setup(activity: AppCompatActivity) {
        val navHome = activity.findViewById<View>(R.id.navHome)
        val navExplore = activity.findViewById<View>(R.id.navExplore)
        val navSaved = activity.findViewById<View>(R.id.navSaved)
        val navAlerts = activity.findViewById<View>(R.id.navAlerts)
        // Check navProfile2 first, fallback to navProfile
        val navProfile = activity.findViewById<View>(R.id.navProfile2) ?: activity.findViewById<View>(R.id.navProfile)

        navHome?.setOnClickListener {
            if (activity !is Home) {
                activity.startActivity(Intent(activity, Home::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                activity.overridePendingTransition(0, 0)
            }
        }
        navExplore?.setOnClickListener {
            if (activity !is ThemeActivity) {
                activity.startActivity(Intent(activity, ThemeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                activity.overridePendingTransition(0, 0)
            }
        }
        navSaved?.setOnClickListener {
            if (activity !is SavedDesignsActivity) {
                activity.startActivity(Intent(activity, SavedDesignsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                activity.overridePendingTransition(0, 0)
            }
        }
        navAlerts?.setOnClickListener {
            if (activity !is AlertsActivity) {
                activity.startActivity(Intent(activity, AlertsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                activity.overridePendingTransition(0, 0)
            }
        }
        navProfile?.setOnClickListener {
            if (activity !is ProfileActivity) {
                activity.startActivity(Intent(activity, ProfileActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                activity.overridePendingTransition(0, 0)
            }
        }
    }
}
