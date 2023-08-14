package com.example.chatapp.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity : AppCompatActivity()  {


    fun Activity.showSnackBar(msg: String) =
        Snackbar.make(this.findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT)

}

