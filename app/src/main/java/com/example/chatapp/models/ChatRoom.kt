package com.example.chatapp.models

import java.util.Calendar
import java.util.Date

class ChatRoom(
    private var chatroomId: String,
    private var userIds: List<String>?,
    private var lastMessageDate: Date?,
    private var lastMessageSenderId: String ,
    private var lastMessage : String
) {
    constructor() : this("", null, null, "","")

    fun getChatroomId(): String {
        return chatroomId
    }

    fun setChatroomId(value: String) {
        chatroomId = value
    }

    // Getter et Setter pour la propriété userIds
    fun getUserIds(): List<String>? {
        return userIds
    }

    fun setUserIds(value: List<String>) {
        userIds = value
    }

    // Getter et Setter pour la propriété lastMessageDate
    fun getLastMessageDate(): Date? {
        return lastMessageDate
    }

    fun setLastMessageDate(value: Date) {
        lastMessageDate = value
    }

    // Getter et Setter pour la propriété lastMessageSenderId
    fun getLastMessageSenderId(): String {
        return lastMessageSenderId
    }

    fun setLastMessageSenderId(value: String) {
        lastMessageSenderId = value
    }

    fun getLastMessage() : String{
        return lastMessage
    }

    fun setLastMessage(lastMessage : String?) {
        if (lastMessage != null) {
            this.lastMessage = lastMessage
        }
    }
}