package com.example.ar_app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AlertManager {
    private const val PREFS_NAME = "AlertsPrefs"
    private const val ALERTS_KEY = "alerts_json_array"

    fun addAlert(context: Context, title: String, message: String) {
        val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("NOTIFICATIONS_ENABLED", true)) {
            return // Notifications disabled
        }

        val alertsPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = alertsPrefs.getString(ALERTS_KEY, "[]")
        
        try {
            val jsonArray = JSONArray(jsonString)
            
            val newAlert = JSONObject()
            newAlert.put("title", title)
            newAlert.put("message", message)
            
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            newAlert.put("time", sdf.format(Date()))
            
            val updatedArray = JSONArray()
            updatedArray.put(newAlert)
            for (i in 0 until jsonArray.length()) {
                updatedArray.put(jsonArray.getJSONObject(i))
            }
            
            alertsPrefs.edit().putString(ALERTS_KEY, updatedArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getAlerts(context: Context): List<AlertItem> {
        val alertsPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = alertsPrefs.getString(ALERTS_KEY, "[]")
        val list = mutableListOf<AlertItem>()
        
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(AlertItem(
                    obj.getString("title"),
                    obj.getString("message"),
                    obj.getString("time")
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
