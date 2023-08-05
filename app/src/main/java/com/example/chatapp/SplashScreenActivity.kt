package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.utils.FirebaseUtil

class SplashScreenActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT: Long = 1000 // 2 secondes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler().postDelayed({

            if (FirebaseUtil.isLoggedIn()) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this,SignupActivity::class.java)
                startActivity(intent)
                finish()
            }

        }, SPLASH_TIME_OUT)
    }
}