package com.example.chatapp

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider

class MyApplication : Application() {


   override fun onCreate() {
        super.onCreate();
        EmojiManager.install(IosEmojiProvider())
    }
}