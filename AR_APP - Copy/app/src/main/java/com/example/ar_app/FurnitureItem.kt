package com.example.ar_app

import com.google.gson.annotations.SerializedName

data class FurnitureItem(
    val id: Int,
    val name: String,
    val category: String, // Replaces subtitle
    val price: String, // String from backend
    @SerializedName("thumbnail_url") val imageUrl: String,
    @SerializedName("model_url") val gltfUrl: String,
    var isSelected: Boolean = false,
    var isOverBudget: Boolean = false
) {
    val parsedPrice: Double
        get() = price.replace("Rs.", "").replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
}
