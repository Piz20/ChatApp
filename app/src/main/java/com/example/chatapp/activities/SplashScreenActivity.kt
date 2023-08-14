package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.models.User
import com.example.chatapp.utils.AndroidUtil
import com.example.chatapp.utils.FirebaseUtil

class SplashScreenActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT: Long = 300

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if( FirebaseUtil.isLoggedIn() && intent.extras!= null){
        //from a notification
           val  userId : String? = intent.extras!!.getString("userId")
            if (userId != null) {
                FirebaseUtil.usersCollection().document(userId).get()
                    .addOnCompleteListener{
                        if(it.isSuccessful){
                            val model = it.result.toObject(User::class.java)

                            val mainIntent = Intent(this,MainActivity::class.java)
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                            startActivity(mainIntent)
                            val intent = Intent(this,TalkWithHomieActivity::class.java)
                            AndroidUtil.passUserModelAsIntent(intent, model!!)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)

                            finish()
                        }
                    }
            }
        } else {
            Handler().postDelayed({

                if (FirebaseUtil.isLoggedIn()) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this, SignupActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }, SPLASH_TIME_OUT)
        }
        }

}