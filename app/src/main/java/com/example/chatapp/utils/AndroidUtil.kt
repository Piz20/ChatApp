package com.example.chatapp.utils

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.example.chatapp.models.User
import com.google.android.material.snackbar.Snackbar

class AndroidUtil {
    companion object {
        fun showToast(context: Context, message: String) =
            Toast.makeText(context, message, LENGTH_LONG).show()


        fun showSnackBar(view: View, message: String) =
            Snackbar.make(view, message, Snackbar.LENGTH_LONG)

        fun passUserModelAsIntent(intent: Intent, user: User) {
            intent.putExtra("uid", user.getUid())
            intent.putExtra("username", user.getUsername())
            intent.putExtra("phonenumber", user.getPhonenumber())
        }

        fun getUserModelFromIntent(intent: Intent): User {
            val secondUser = User()
            secondUser.setUid(intent.getStringExtra("uid").toString())
            secondUser.setUsername(intent.getStringExtra("username").toString())
            secondUser.setPhonenumber(intent.getStringExtra("phonenumber").toString())

            return secondUser
        }
    }


}