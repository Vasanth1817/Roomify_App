package com.example.ar_app

import android.content.Intent
import android.text.TextPaint
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class registration : AppCompatActivity() {

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Sign In Clickable Text

        val signInText = findViewById<TextView>(R.id.signInText)

        val text = "Already have an account? Sign In"
        val spannable = SpannableString(text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {

                val intent = Intent(this@registration, Login::class.java)
                startActivity(intent)
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = Color.parseColor("#6C2BD9")
            }
        }

        spannable.setSpan(
            clickableSpan,
            25,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        signInText.text = spannable
        signInText.movementMethod = LinkMovementMethod.getInstance()

        // Password Toggle

        val password = findViewById<EditText>(R.id.password)

        password.setOnTouchListener { _, event ->

            if (event.action == MotionEvent.ACTION_UP) {

                if (event.rawX >= (password.right -
                            password.compoundDrawables[2].bounds.width())
                ) {

                    if (isPasswordVisible) {
                        password.transformationMethod = PasswordTransformationMethod.getInstance()
                        password.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_lock_lock, 0, R.drawable.ic_eye_off, 0)
                        isPasswordVisible = false
                    } else {
                        password.transformationMethod = HideReturnsTransformationMethod.getInstance()
                        password.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_lock_lock, 0, R.drawable.ic_eye, 0)
                        isPasswordVisible = true
                    }

                    password.setSelection(password.text.length)
                    return@setOnTouchListener true
                }
            }

            false
        }

        // Confirm Password Toggle

        val confirmPassword = findViewById<EditText>(R.id.confirmPassword)

        confirmPassword.setOnTouchListener { _, event ->

            if (event.action == MotionEvent.ACTION_UP) {

                if (event.rawX >= (confirmPassword.right -
                            confirmPassword.compoundDrawables[2].bounds.width())
                ) {

                    if (isConfirmPasswordVisible) {
                        confirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                        confirmPassword.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_lock_idle_lock, 0, R.drawable.ic_eye_off, 0)
                        isConfirmPasswordVisible = false
                    } else {
                        confirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                        confirmPassword.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_lock_idle_lock, 0, R.drawable.ic_eye, 0)
                        isConfirmPasswordVisible = true
                    }

                    confirmPassword.setSelection(confirmPassword.text.length)
                    return@setOnTouchListener true
                }
            }

            false
        }
    }

    fun regist(view: View) {
        val fullName = findViewById<EditText>(R.id.fullName).text.toString()
        val phone = findViewById<EditText>(R.id.phoneNumber).text.toString()
        val email = findViewById<EditText>(R.id.email).text.toString()
        val password = findViewById<EditText>(R.id.password).text.toString()
        val confirm = findViewById<EditText>(R.id.confirmPassword).text.toString()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            android.widget.Toast.makeText(this, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirm) {
            android.widget.Toast.makeText(this, "Passwords do not match", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val regBtn = view
        regBtn.isEnabled = false

        val request = RegisterRequest(fullName, phone, email, password)
        
        RetrofitClient.instance.registerUser(request).enqueue(object : retrofit2.Callback<RegisterResponse> {
            override fun onResponse(
                call: retrofit2.Call<RegisterResponse>,
                response: retrofit2.Response<RegisterResponse>
            ) {
                regBtn.isEnabled = true
                if (response.isSuccessful) {
                    val userId = response.body()?.user_id
                    val prefs = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("USER_ID", userId)
                        .putString("FULL_NAME", fullName)
                        .putString("EMAIL", email)
                        .apply()
                    
                    android.widget.Toast.makeText(this@registration, "Registration Successful!", android.widget.Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@registration, Home::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    android.widget.Toast.makeText(this@registration, "Error: Email might already be registered", android.widget.Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<RegisterResponse>, t: Throwable) {
                regBtn.isEnabled = true
                android.widget.Toast.makeText(this@registration, "Network Error: ${t.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        })
    }
}