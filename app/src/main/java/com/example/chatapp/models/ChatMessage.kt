package com.example.chatapp.models

import java.util.Calendar
import java.util.Date

class ChatMessage(private var message : String , private var senderId : String , private var date : Date) {
    constructor() : this("","",Calendar.getInstance().time)

    fun getMessage(): String {
        return message
    }

    fun setMessage(newMessage: String) {
        message = newMessage
    }

    fun getSenderId(): String {
        return senderId
    }

    fun setSenderId(newSenderId: String) {
        senderId = newSenderId
    }

    fun getDate(): Date {
        return date
    }

    fun setDate(newDate: Date) {
        date = newDate
    }
}