package com.example.chatapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.ContentFrameLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.adapter.ChatRecyclerAdapter
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.ChatRoom
import com.example.chatapp.models.User
import com.example.chatapp.utils.AndroidUtil
import com.example.chatapp.utils.EncryptionUtil
import com.example.chatapp.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.EmojiTheming
import com.vanniktech.ui.smoothScrollTo
import java.util.Calendar

class TalkWithHomieActivity : BaseActivity() {
    lateinit var secondUser: User
    lateinit var usernameTextView: TextView
    lateinit var messageEditText: EditText
    lateinit var sendmessageButton: ImageButton
    lateinit var recyclerviewChat: RecyclerView
    lateinit var backButton: ImageButton
    private lateinit var emojiButton: ImageButton
    private lateinit var emojiPopup: EmojiPopup
    private var isEmojiPopupShown = false
    lateinit var chatAdapter: ChatRecyclerAdapter
    var chatroom: ChatRoom? = null
    var chatroomId: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_talk_with_homie)
        backButton = findViewById(R.id.back_button)
        usernameTextView = findViewById(R.id.username_textView)
        messageEditText = findViewById(R.id.message_edittext)
        sendmessageButton = findViewById(R.id.send_message_button)
        recyclerviewChat = findViewById(R.id.chat_recycler_view)
        emojiButton = findViewById(R.id.emojiButton)


        // Retrieve info for the secondUser
        secondUser = AndroidUtil.getUserModelFromIntent(intent)
        chatroomId =
            FirebaseUtil.currentUserId()
                ?.let { FirebaseUtil.getChatRoomId(it, secondUser.getUid().trim()) }
        setupUi()
        //create or getTheActualChatRoom
        getOrCreateChatRoom()

        //sendMessage to the secondUser
        sendmessageButton.setOnClickListener {
            val message: String = messageEditText.text.toString()
            if (!message.isEmpty()) {
                sendMessageToUser(message)
            }
        }

        //To display the messages
        setupChatRecyclerView()
    }

    fun setupChatRecyclerView(){
        val query : Query = FirebaseUtil.getChatroomMessageference(chatroomId)
            .orderBy("date",Query.Direction.DESCENDING)
        val options : FirestoreRecyclerOptions<ChatMessage> = FirestoreRecyclerOptions.Builder<ChatMessage>()
            .setQuery(query,ChatMessage::class.java).build()

        chatAdapter = ChatRecyclerAdapter(options,applicationContext)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerviewChat.layoutManager = linearLayoutManager
        recyclerviewChat.adapter = chatAdapter
        chatAdapter.startListening()
        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerviewChat.smoothScrollTo(0)
            }
        })
    }

    fun getOrCreateChatRoom() {

        //There is already a chatroom for those users

        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                chatroom = task.result.toObject(ChatRoom::class.java)
                //There was not already an chatroom for those users
                if (chatroom == null) {
                    chatroom = chatroomId?.let {
                        ChatRoom(
                            it,
                            listOf(FirebaseUtil.currentUserId().toString(), secondUser.getUid()),
                            null, "" ,""
                        )
                    }!!

                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroom!!)
                }
            }
        }

    }

    fun setupUi() {
        usernameTextView.text = secondUser.getUsername()
        emojiPopup = EmojiPopup(
            rootView = findViewById<ContentFrameLayout>(android.R.id.content),
            messageEditText,
            theming = EmojiTheming(
                resources.getColor(R.color.black),
                resources.getColor(R.color.white),
                resources.getColor(R.color.black),
                resources.getColor(R.color.black),
                resources.getColor(R.color.white),
                resources.getColor(R.color.white)
            ),
            onEmojiPopupShownListener = {
                emojiButton.setImageResource(R.drawable.ic_keyboard); isEmojiPopupShown = true
            },
            onEmojiPopupDismissListener = {
                emojiButton.setImageResource(R.drawable.ic_emogi); isEmojiPopupShown = false
            },
            keyboardAnimationStyle = com.vanniktech.emoji.ios.R.style.emoji_fade_animation_style
        )

        emojiButton.setOnClickListener {

            if (isEmojiPopupShown) {
                emojiPopup.dismiss()
            } else {
                emojiPopup.toggle()
            }
        }
        backButton.setOnClickListener {
            onBackPressed()
        }

        //Watch if the user can send a message or not
        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (messageEditText.text.toString().isEmpty()) {
                    sendmessageButton.isEnabled = false
                    sendmessageButton.setImageResource(R.drawable.ic_send_impossible)
                } else {
                    sendmessageButton.isEnabled = true
                    sendmessageButton.setImageResource(R.drawable.icon_send)
                }

            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    fun sendMessageToUser(message: String) {
        //update the chatrommDocument
        chatroom?.setLastMessageDate(Calendar.getInstance().time)
        FirebaseUtil.currentUserId()?.let { chatroom?.setLastMessageSenderId(it) }
        chatroom?.setLastMessage(EncryptionUtil.encrypt(message,this))
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroom!!)

        //create chat message object
        val encryptedMessage: String = EncryptionUtil.encrypt(message,this)
        val chatMessage = FirebaseUtil.currentUserId()
            ?.let { ChatMessage(encryptedMessage, it, Calendar.getInstance().time) }
        //send the message to Firestore
        if (chatMessage != null) {
            messageEditText.setText("")
            FirebaseUtil.getChatroomMessageference(chatroomId).add(chatMessage)
        }
    }
}
