package com.example.ar_app

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

data class RegisterRequest(
    val full_name: String,
    val phone_number: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val message: String,
    val user_id: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val user_id: String,
    val full_name: String,
    val email: String
)

data class LayoutItem(
    val name: String,
    val px: Float, val py: Float, val pz: Float,
    val rx: Float, val ry: Float, val rz: Float, val rw: Float,
    val sx: Float, val sy: Float, val sz: Float
)

data class LayoutData(
    val items: List<LayoutItem>,
    val user_id: String? = null
)

data class SaveLayoutResponse(
    val message: String,
    val id: Int
)

data class SavedLayout(
    val id: Int,
    val name: String,
    val json_data: String,
    val user_id: String?
)

data class BudgetRequest(
    val user_id: String,
    val max_budget: Float
)

data class BudgetResponse(
    val max_budget: Float,
    val message: String? = null
)

interface RoomifyApiService {
    @GET("furniture")
    fun getFurniture(): Call<List<FurnitureItem>>

    @retrofit2.http.POST("api/register")
    fun registerUser(@retrofit2.http.Body request: RegisterRequest): Call<RegisterResponse>

    @retrofit2.http.POST("api/login")
    fun loginUser(@retrofit2.http.Body request: LoginRequest): Call<LoginResponse>

    @retrofit2.http.POST("save_layout")
    fun saveLayout(@retrofit2.http.Body layout: LayoutData): Call<SaveLayoutResponse>

    @GET("get_layouts")
    fun getLayouts(@retrofit2.http.Query("user_id") userId: String?): Call<List<SavedLayout>>

    @retrofit2.http.DELETE("delete_layout/{id}")
    fun deleteLayout(@retrofit2.http.Path("id") layoutId: Int): Call<Void>

    @GET("api/budget")
    fun getBudget(@retrofit2.http.Query("user_id") userId: String): Call<BudgetResponse>

    @retrofit2.http.POST("api/budget")
    fun updateBudget(@retrofit2.http.Body request: BudgetRequest): Call<BudgetResponse>
}

object RetrofitClient {
    // 192.168.0.101 is your computer's Wi-Fi IP address so your physical phone can reach it!
    private const val BASE_URL = "https://roomifybackend.onrender.com/"

    // Free cloud servers (like Render.com) go to sleep to save money. 
    // We need to give the server up to 90 seconds to wake up on the first launch!
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    val instance: RoomifyApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Attach our custom timeout client!
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RoomifyApiService::class.java)
    }
}
