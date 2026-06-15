package com.example.ar_app

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val logo = findViewById<ImageView>(R.id.logo)
        val room = findViewById<ImageView>(R.id.roomImage)
        val title = findViewById<TextView>(R.id.title)
        val subtitle = findViewById<TextView>(R.id.subtitle)

        // LOGO FLOAT ANIMATION
        val floatAnim = ObjectAnimator.ofFloat(
            logo,
            "translationY",
            0f,
            -25f,
            0f
        )

        floatAnim.duration = 2500
        floatAnim.repeatCount = ObjectAnimator.INFINITE
        floatAnim.interpolator = AccelerateDecelerateInterpolator()
        floatAnim.start()

        // ROOM IMAGE 3D SCALE EFFECT
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.9f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.9f, 1f)

        val roomAnim = ObjectAnimator.ofPropertyValuesHolder(
            room,
            scaleX,
            scaleY
        )

        roomAnim.duration = 1800
        roomAnim.start()

        // ROOM IMAGE SLIGHT ROTATION
        room.rotationY = 12f
        room.animate()
            .rotationY(0f)
            .setDuration(1800)
            .start()

        // TITLE FADE-IN
        title.alpha = 0f
        title.translationY = 40f

        title.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(1400)
            .start()

        // SUBTITLE FADE-IN
        subtitle.alpha = 0f
        subtitle.translationY = 40f

        subtitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(1700)
            .start()

        // NEXT SCREEN
        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
            val userId = prefs.getString("USER_ID", null)
            
            if (userId != null) {
                startActivity(Intent(this, Home::class.java))
            } else {
                startActivity(Intent(this, Login::class.java))
            }
            finish()
        }, 4000)
    }
}
