package com.example.ar_app

data class SavedDesign(
    val id: Int,
    val name: String,
    val mode: String?,
    val json_data: String,
    val before_image: String?,
    val after_image: String?
)
