package com.example.chatapp.models

import java.util.Calendar
import java.util.Date

class User(
    private var uid: String,
    private var username: String,
    private var phonenumber: String,
    private var createdAt: Date = Calendar.getInstance().time,
    private var fcmToken : String = ""
) {
    constructor() : this("", "", "", Calendar.getInstance().time,"")

    constructor(uid: String, username: String, phonenumber: String) : this(
        uid,
        username,
        phonenumber,
        Calendar.getInstance().time,
        ""
    )


    fun getUid(): String {
        return uid
    }

    fun setUid(value: String) {
        uid = value
    }

    // Getter et Setter pour la propriété username
    fun getUsername(): String {
        return username
    }

    fun setUsername(value: String) {
        username = value
    }

    // Getter et Setter pour la propriété phonenumber
    fun getPhonenumber(): String {
        return phonenumber
    }

    fun setPhonenumber(value: String) {
        phonenumber = value
    }

    // Getter et Setter pour la propriété createdAt
    fun getCreatedAt(): Date {
        return createdAt
    }

    fun setCreatedAt(value: Date) {
        createdAt = value
    }

    fun getfcmToken(){
        return getfcmToken()
    }

    fun setfcmToken(fcmToken: String){
       this.fcmToken = fcmToken
    }
}


