package com.example.chatapp.interfaces


interface UiListener {
    fun hideProgressBar()
    fun hideNoTalksTextView(boolean: Boolean)
    fun hideDeleteAllChatRoomsButton(boolean: Boolean)
}