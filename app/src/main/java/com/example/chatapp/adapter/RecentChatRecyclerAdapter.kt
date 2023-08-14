package com.example.chatapp.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.activities.TalkWithHomieActivity
import com.example.chatapp.interfaces.UiListener
import com.example.chatapp.models.ChatRoom
import com.example.chatapp.models.User
import com.example.chatapp.utils.AndroidUtil
import com.example.chatapp.utils.EncryptionUtil
import com.example.chatapp.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import java.util.Calendar

class RecentChatRecyclerAdapter(
    options: FirestoreRecyclerOptions<ChatRoom?>, context: Context, private val ui: UiListener
) : FirestoreRecyclerAdapter<ChatRoom?, RecentChatRecyclerAdapter.ChatRoomViewHolder>(options) {

    var chatRoomList : MutableList<ChatRoom> = mutableListOf()


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RecentChatRecyclerAdapter.ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recent_chat_recylcer_row, parent, false)
        return ChatRoomViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(
        holder: RecentChatRecyclerAdapter.ChatRoomViewHolder, position: Int, model: ChatRoom
    ) {
        val context = holder.itemView.context
        model.getUserIds()?.let {
            FirebaseUtil.getOtherUserFromChatRoom(it).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                        val secondUser: User? = task.result.toObject(User::class.java)
                        if (secondUser != null) {
                            chatRoomList.add(model)
                        }
                        if (secondUser != null) {
                            FirebaseUtil.getOtherUserProfilePicStorageReference(secondUser.getUid()).downloadUrl
                                .addOnSuccessListener {
                                    AndroidUtil.setProfilePic(context, it, holder.profilePic)
                                }
                        }
                        val isLastMessageHasBeenSentByMe =
                            model.getLastMessageSenderId() == FirebaseUtil.currentUserId()
                        val isMe = secondUser?.getUid() == FirebaseUtil.currentUserId()
                        if (isMe) {
                            holder.usernameTextView.text = secondUser!!.getUsername() + " (Me)"
                        } else {
                            holder.usernameTextView.text = secondUser!!.getUsername()
                        }
                        try {
                            if (isLastMessageHasBeenSentByMe && !isMe) {
                                holder.lastMessageTextView.text = "You : " + model.getLastMessage()
                                    ?.let { it1 ->
                                        EncryptionUtil.decrypt(
                                            it1, context
                                        )
                                    }
                            } else {
                                holder.lastMessageTextView.text =
                                    model.getLastMessage()
                                        ?.let { it1 -> EncryptionUtil.decrypt(it1, context) }
                            }
                        } catch (e: javax.crypto.AEADBadTagException) {
                            print("javax.crypto.AEADBadTagException")
                        }
                        if (FirebaseUtil.simpleDateFormat(
                                Calendar.getInstance().time, "dd/MM/yy"
                            ) != FirebaseUtil.simpleDateFormat(
                                model.getLastMessageDate()!!, "dd/MM/yy"
                            )
                        ) {
                            holder.lastmessageDate.text = FirebaseUtil.simpleDateFormat(
                                model.getLastMessageDate()!!, "dd/MM/yy"
                            )
                        } else {
                            holder.lastmessageDate.text = FirebaseUtil.simpleDateFormat(
                                model.getLastMessageDate()!!, "HH:mm"
                            )
                        }
                        holder.recentChatLinearLayout.visibility = View.VISIBLE
                        ui.hideProgressBar()
                        holder.itemView.setOnClickListener {
                            val intent = Intent(context, TalkWithHomieActivity::class.java)
                            AndroidUtil.passUserModelAsIntent(intent, secondUser)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }

                }
            }

        }
    }

    inner class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView = itemView.findViewById<TextView>(R.id.username_text_view)
        val lastMessageTextView = itemView.findViewById<TextView>(R.id.last_message_text)
        val lastmessageDate = itemView.findViewById<TextView>(R.id.last_message_date_text)
        val recentChatLinearLayout =
            itemView.findViewById<LinearLayout>(R.id.recent_chat_linearlayout)
        val profilePic = itemView.findViewById<ImageView>(R.id.profile_picture_image_view)

    }

    override fun onDataChanged() {
        super.onDataChanged()
        if (itemCount == 0) {
            ui.hideProgressBar()
            ui.showNoTalksTextView()
        } else {
            ui.hideNoTalksTextView()
        }
    }
}