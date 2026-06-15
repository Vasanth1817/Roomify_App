package com.example.ar_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import com.example.ar_app.R

class SavedDesignsAdapter(
    private val designs: MutableList<SavedDesign>,
    private val priceMap: Map<String, Float>,
    private val onActionClicked: (SavedDesign, String) -> Unit
) : RecyclerView.Adapter<SavedDesignsAdapter.DesignViewHolder>() {

    fun removeAt(position: Int) {
        designs.removeAt(position)
        notifyItemRemoved(position)
    }

    class DesignViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvDesignName)
        val tvItemCount: TextView = view.findViewById(R.id.tvItemCount)
        val tvProjectCost: TextView? = view.findViewById(R.id.tvProjectCost)
        val btnViewAR: Button = view.findViewById(R.id.btnViewAR)
        val btnCompare: Button = view.findViewById(R.id.btnCompare)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DesignViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_design, parent, false)
        return DesignViewHolder(view)
    }

    override fun onBindViewHolder(holder: DesignViewHolder, position: Int) {
        val design = designs[position]
        // Set Title based on design name
        holder.tvName.text = design.name

        // Set mode on the button
        val mode = design.mode ?: "AR"
        holder.btnViewAR.text = "VIEW IN ${mode.uppercase()}"
        
        try {
            val jsonObject = JSONObject(design.json_data)
            val itemsArray = jsonObject.getJSONArray("items")
            holder.tvItemCount.text = "${itemsArray.length()} Furniture Items"
            
            var cost = 0f
            for (i in 0 until itemsArray.length()) {
                val itemObj = itemsArray.getJSONObject(i)
                val url = itemObj.optString("model_url")
                cost += priceMap[url] ?: 0f
            }
            holder.tvProjectCost?.text = String.format(java.util.Locale("en", "IN"), "₹%,.2f", cost)
            
        } catch (e: Exception) {
            holder.tvItemCount.text = "Unknown items"
            holder.tvProjectCost?.text = "₹0.00"
        }

        holder.btnViewAR.setOnClickListener {
            onActionClicked(design, "VIEW_AR")
        }
        
        holder.btnCompare.setOnClickListener {
            onActionClicked(design, "COMPARE")
        }
    }

    override fun getItemCount() = designs.size
}
