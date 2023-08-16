package com.example.chatapp.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.ContentFrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.adapter.ChatRecyclerAdapter
import com.example.chatapp.swipteToReply.MessageSwipeController
import com.example.chatapp.swipteToReply.SwipeControllerActions
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.ChatRoom
import com.example.chatapp.models.User
import com.example.chatapp.utils.AndroidUtil
import com.example.chatapp.utils.EncryptionUtil
import com.example.chatapp.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.github.chrisbanes.photoview.PhotoView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.messaging.FirebaseMessaging
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.EmojiTheming
import com.vanniktech.ui.smoothScrollTo
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar

class TalkWithHomieActivity : BaseActivity() {
    lateinit var secondUser: User
    lateinit var usernameTextView: TextView
    lateinit var messageEditText: EditText
    lateinit var sendmessageButton: ImageButton
    lateinit var recyclerviewChat: RecyclerView
    lateinit var txtQuotedMsg : TextView
    lateinit var backButton: ImageButton
    lateinit var emojiButton: ImageButton
    lateinit var deletechatroomButton : ImageButton
   lateinit var emojiPopup: EmojiPopup
    private var isEmojiPopupShown = false
    lateinit var chatAdapter: ChatRecyclerAdapter
    lateinit var profilepicture : ImageView
    lateinit var reply_layout : ConstraintLayout
    lateinit var cancel_reply_layout : ImageButton
    lateinit var deleteChatroomProgressBar : ProgressBar
    var chatroom: ChatRoom? = null
    var chatroomId: String? = ""
    var imageUri : Uri =Uri.parse("")
    lateinit var firebaseMessagingServerKey : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_talk_with_homie)
        backButton = findViewById(R.id.back_button)
        usernameTextView = findViewById(R.id.username_textView)
        messageEditText = findViewById(R.id.message_edittext)
        sendmessageButton = findViewById(R.id.send_message_button)
        recyclerviewChat = findViewById(R.id.chat_recycler_view)
        emojiButton = findViewById(R.id.emojiButton)
        deletechatroomButton = findViewById(R.id.delete_chatroom_button)
        profilepicture = findViewById(R.id.profile_picture_image_view)
        txtQuotedMsg = findViewById(R.id.txtQuotedMsg)
        reply_layout = findViewById(R.id.reply_layout)
        cancel_reply_layout = findViewById(R.id.cancelButton)
        deleteChatroomProgressBar = findViewById(R.id.delete_chatroom_progressbar)

        // Retrieve info for the secondUser
        secondUser = AndroidUtil.getUserModelFromIntent(intent)
        chatroomId =
            FirebaseUtil.currentUserId()
                ?.let { FirebaseUtil.getChatRoomId(it, secondUser.getUid().trim()) }
            try{
            FirebaseUtil.getOtherUserProfilePicStorageReference(secondUser.getUid()).downloadUrl
                .addOnSuccessListener {
                    imageUri = it
                    AndroidUtil.setProfilePic(this,imageUri,profilepicture)
             }}
            catch(_: java.lang.IllegalArgumentException){

            }
        setupUi()

        //create or getTheActualChatRoom
        getOrCreateChatRoom()

        //sendMessage to the secondUser
        sendmessageButton.setOnClickListener {
            val message: String = messageEditText.text.toString()
            if (!message.isEmpty()) {
                sendMessageToUser(message)
                hideReplyLayout()
            }
        }
        //Press this button to hide reply layout
        cancel_reply_layout.setOnClickListener {
            hideReplyLayout()
        }

        //To display the messages
        setupChatRecyclerView()

        //Firebase messaging server key
        val applicationInfo: ApplicationInfo = this.packageManager.getApplicationInfo(
            application.packageName,
            PackageManager.GET_META_DATA
        )
        firebaseMessagingServerKey = applicationInfo.metaData["FIREBASE_MESSAGING_SERVER_KEY"].toString()
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

        //To handle the logic of the swipetoreply
        /*val messageSwipeController =
            MessageSwipeController.MessageSwipeController(this, object : SwipeControllerActions {
                override fun showReplyUI(position: Int) {
                    showQuotedMessage(chatAdapter.chatMessageList[position])
                }
            })

        val itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper.attachToRecyclerView(recyclerviewChat)*/

    }

    //hide reply layout
    private fun hideReplyLayout() {
        reply_layout.visibility = View.GONE
    }

    //Put the concerned message in the reply layout
    private fun showQuotedMessage(message: ChatMessage) {
        messageEditText.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT)
        txtQuotedMsg.text = EncryptionUtil.decrypt(message.getMessage(),this)
        reply_layout.visibility = View.VISIBLE

    }


    //Do the name of the function
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

        //When the current user want to delete that specific chatroom
        deletechatroomButton.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
                .setTitle("Kill all of this messages ?")
                .setPositiveButton("OK") { dialog, _ ->
                    setInProgres(true)
                    deleteDocumentAndSubcollection("chatrooms",
                        FirebaseUtil.getChatRoomId(FirebaseUtil.currentUserId()!!,secondUser.getUid()),
                        "chats")
                }
                .setNegativeButton("Cancel") { dialog, _ ->

                }
                .create()
            dialog.show()

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

        messageEditText.setOnClickListener{
            if (isEmojiPopupShown) {
                emojiPopup.dismiss()
                messageEditText.requestFocus()
            }
        }

        //Set the image in the fullScreen
        fun setImageInFullScreenImage(fullScreenImageView:ImageView){
            Glide.with(this).load(imageUri).error(R.drawable.icon_user_white).into(fullScreenImageView)
        }

        profilepicture.setOnClickListener{
            val dialog = Dialog(this, R.style.AppTheme_NoActionBar)
            dialog.setContentView(R.layout.dialog_fullscreen_image)

            // Initialize photoview and buttons
            val fullScreenImageView = dialog.findViewById<PhotoView>(R.id.fullscreen_content)
            val usernameTextView = dialog.findViewById<TextView>(R.id.dialog_toolbar_picture_textview)
            val buttonBack = dialog.findViewById<ImageButton>(R.id.back_button)
            val buttonEdit = dialog.findViewById<ImageButton>(R.id.edit_profile_picture)
            val buttonDelete = dialog.findViewById<ImageButton>(R.id.delete_profile_picture)
            usernameTextView.setText("Your homie's face...")
            buttonBack.setOnClickListener {
                dialog.dismiss()
            }
            buttonEdit.visibility = View.GONE
            buttonDelete.visibility = View.GONE
            setImageInFullScreenImage(fullScreenImageView)
            dialog.show()
        }
    }

    fun setInProgres(boolean: Boolean){
        if(boolean){
            deleteChatroomProgressBar.visibility = View.VISIBLE
            recyclerviewChat.visibility = View.GONE
        }else {
            deleteChatroomProgressBar.visibility = View.GONE
            recyclerviewChat.visibility = View.VISIBLE
        }
    }
    fun deleteDocumentAndSubcollection(collectionPath: String, documentId: String, subcollectionPath: String) {
        val db = FirebaseFirestore.getInstance()
        val documentReference = db.collection(collectionPath).document(documentId)

        // Suppressing principal document
        documentReference.delete()
            .addOnSuccessListener {
                // Suppressig docs in the subcollection
                val subcollectionReference = documentReference.collection(subcollectionPath)
                deleteCollection(subcollectionReference)
            }
            .addOnFailureListener {
                setInProgres(false)
            }
    }

    fun deleteCollection(collection: CollectionReference) {
        collection.get()
            .addOnSuccessListener { querySnapshot ->
                val batch = collection.firestore.batch()

                for (documentSnapshot in querySnapshot) {
                    batch.delete(documentSnapshot.reference)
                }

                // Suppreesing in mass of the subcollection
                batch.commit()
                    .addOnSuccessListener {
                        setInProgres(false)
                    }
                    .addOnFailureListener {
                        setInProgres(false)
                    }
            }
            .addOnFailureListener {
                setInProgres(false)
            }}


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
            FirebaseUtil.getChatroomMessageference(chatroomId).add(chatMessage).addOnSuccessListener {
                sendNotification(message)
            }
        }
    }

    //To send notification
    fun sendNotification(message : String){
              FirebaseUtil.currentUserDetails().get().addOnCompleteListener{it ->
                  if(it.isSuccessful){
                      val currentUser = it.result.toObject(User::class.java)
                      try{
                          val jsonObject = JSONObject()

                          val notificationObject = JSONObject()
                          notificationObject.put("title",currentUser!!.getUsername())
                          notificationObject.put("body",message)

                          val dataObject = JSONObject()
                          dataObject.put("uid",currentUser.getUid())

                          jsonObject.put("notification",notificationObject)
                          jsonObject.put("data",dataObject)
                          jsonObject.put("to",secondUser.getFcmToken())
                          callApi(jsonObject)
                      }catch (e : Exception){
                        println(e.message)
                      }
                  }
              }
    }

    //call the api firebase messaging
    fun callApi(jsonObject: JSONObject){
        val JSON = "application/json".toMediaType()
        val client = OkHttpClient()
        val url ="https://fcm.googleapis.com/fcm/send"
        val body = RequestBody.create(JSON,jsonObject.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization","Bearer $firebaseMessagingServerKey")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // Handle successful response here
                val responseBody = response.body?.string()
                println("Response: $responseBody")
            }

            override fun onFailure(call: Call, e: IOException) {
                // Handle error here
                e.printStackTrace()
            }
        })

    }


}
