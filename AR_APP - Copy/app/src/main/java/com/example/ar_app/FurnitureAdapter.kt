package com.example.ar_app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FurnitureAdapter(
    private val items: List<FurnitureItem>,
    private val onAddClicked: (FurnitureItem, Int) -> Unit
) : RecyclerView.Adapter<FurnitureAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvOverBudget: TextView = view.findViewById(R.id.tvOverBudget)
        val btnAdd: ImageButton = view.findViewById(R.id.btnAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_furniture_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvName.text = item.name
        holder.tvSubtitle.text = item.category
        holder.tvPrice.text = String.format(java.util.Locale("en", "IN"), "₹%,.0f", item.parsedPrice)

        // Load Image
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .centerCrop()
            .into(holder.ivThumbnail)

        // Selection State
        if (item.isSelected) {
            holder.btnAdd.setImageResource(android.R.drawable.ic_menu_delete) // Change to a delete/check icon
            holder.btnAdd.setBackgroundResource(R.drawable.badge_over_budget) // Red background to indicate remove
        } else {
            holder.btnAdd.setImageResource(android.R.drawable.ic_input_add)
            holder.btnAdd.setBackgroundResource(R.drawable.bg_circle_purple)
        }

        // Budget State
        if (item.isOverBudget) {
            holder.tvOverBudget.visibility = View.VISIBLE
        } else {
            holder.tvOverBudget.visibility = View.GONE
        }

        holder.btnAdd.setOnClickListener {
            onAddClicked(item, position)
        }
    }

    override fun getItemCount() = items.size
}
