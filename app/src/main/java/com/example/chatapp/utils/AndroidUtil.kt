package com.example.chatapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.R

import com.example.chatapp.models.User

class AndroidUtil {
    companion object {
        private var density = 1f

        fun passUserModelAsIntent(intent: Intent, user: User) {
            intent.putExtra("uid", user.getUid())
            intent.putExtra("username", user.getUsername())
            intent.putExtra("phonenumber", user.getPhonenumber())
            intent.putExtra("fcmToken",user.getFcmToken())
        }

        fun getUserModelFromIntent(intent: Intent): User {
            val secondUser = User()
            secondUser.setUid(intent.getStringExtra("uid").toString())
            secondUser.setUsername(intent.getStringExtra("username").toString())
            secondUser.setPhonenumber(intent.getStringExtra("phonenumber").toString())
            secondUser.setFcmToken(intent.getStringExtra("fcmToken").toString())
            return secondUser
        }

        fun setProfilePic(context : Context , imageUri: Uri, imageView: ImageView){
            try {
                Glide.with(context).load(imageUri).error(R.drawable.icon_user)
                    .apply(RequestOptions.circleCropTransform()).into(imageView)
            }catch (_:java.lang.IllegalArgumentException){

            }
        }
        fun dp(value: Float, context: Context): Int {
            if (density == 1f) {
                checkDisplaySize(context)
            }
            return if (value == 0f) {
                0
            } else Math.ceil((density * value).toDouble()).toInt()
        }


        private fun checkDisplaySize(context: Context) {
            try {
                density = context.resources.displayMetrics.density
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

}