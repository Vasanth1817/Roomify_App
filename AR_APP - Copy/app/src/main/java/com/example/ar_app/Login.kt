package com.example.ar_app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
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

class Login : AppCompatActivity() {

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Sign Up Clickable Text

        val signupText = findViewById<TextView>(R.id.signup)

        val text = "Don't have an account? Sign Up"
        val spannable = SpannableString(text)

        val startIndex = text.indexOf("Sign Up")

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {

                val intent = Intent(this@Login, registration::class.java)
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
            startIndex,
            startIndex + "Sign Up".length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        signupText.text = spannable
        signupText.movementMethod = LinkMovementMethod.getInstance()

        // Password Eye Toggle

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
    }

    fun login(view: View) {
        val email = findViewById<EditText>(R.id.email).text.toString()
        val password = findViewById<EditText>(R.id.password).text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            android.widget.Toast.makeText(this, "Please enter email and password", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val loginBtn = view
        loginBtn.isEnabled = false

        val request = LoginRequest(email, password)
        
        RetrofitClient.instance.loginUser(request).enqueue(object : retrofit2.Callback<LoginResponse> {
            override fun onResponse(
                call: retrofit2.Call<LoginResponse>,
                response: retrofit2.Response<LoginResponse>
            ) {
                loginBtn.isEnabled = true
                if (response.isSuccessful) {
                    val userId = response.body()?.user_id
                    val fullName = response.body()?.full_name
                    
                    val prefs = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("USER_ID", userId)
                        .putString("FULL_NAME", fullName)
                        .putString("EMAIL", email)
                        .apply()
                    
                    android.widget.Toast.makeText(this@Login, "Welcome Back, $fullName!", android.widget.Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Login, Home::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    android.widget.Toast.makeText(this@Login, "Invalid email or password", android.widget.Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
                loginBtn.isEnabled = true
                android.widget.Toast.makeText(this@Login, "Network Error: ${t.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        })
    }
}