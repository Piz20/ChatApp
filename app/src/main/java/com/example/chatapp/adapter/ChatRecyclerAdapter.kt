package com.example.chatapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.ChatRoom
import com.example.chatapp.utils.EncryptionUtil
import com.example.chatapp.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import java.text.SimpleDateFormat
import java.util.Locale

class ChatRecyclerAdapter(options: FirestoreRecyclerOptions<ChatMessage>, context: Context) :
    FirestoreRecyclerAdapter<ChatMessage, ChatRecyclerAdapter.ChatMessageViewHolder>(options) {

    var chatMessageList : MutableList<ChatMessage> = mutableListOf()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatRecyclerAdapter.ChatMessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_message_recycler_row, parent, false)
        return ChatMessageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ChatRecyclerAdapter.ChatMessageViewHolder,
        position: Int,
        model: ChatMessage
    ) {
        val context = holder.itemView.context
        if(model.getSenderId() == FirebaseUtil.currentUserId()){
            holder.leftChatLayout.visibility =View.GONE
            holder.rightChatLayout.visibility = View.VISIBLE
            holder.rightChatTextView.text = EncryptionUtil.decrypt(model.getMessage(),context)
            holder.rightdateTextView.text = FirebaseUtil.simpleDateFormat(model.getDate(),"HH:mm")
        } else {
            holder.rightChatLayout.visibility =View.GONE
            holder.leftChatLayout.visibility = View.VISIBLE
            holder.leftChatTextView.text = EncryptionUtil.decrypt(model.getMessage(),context)
            holder.lefttdateTextView.text = FirebaseUtil.simpleDateFormat(model.getDate(),"HH:mm")
        }
        chatMessageList.add(model)
    }


    inner class ChatMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val leftChatLayout:RelativeLayout = itemView.findViewById(R.id.left_chat_relativelayout)
        val rightChatLayout: RelativeLayout = itemView.findViewById(R.id.right_chat_relativelayout)
        val leftChatTextView: TextView = itemView.findViewById(R.id.left_chat_textview)
        val rightChatTextView: TextView = itemView.findViewById(R.id.right_chat_textview)
        val lefttdateTextView : TextView = itemView.findViewById(R.id.date_left_textview)
        val rightdateTextView : TextView = itemView.findViewById(R.id.date_right_textview)

    }
}


