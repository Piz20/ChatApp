package com.example.chatapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.TalkWithHomieActivity
import com.example.chatapp.models.User
import com.example.chatapp.utils.AndroidUtil
import com.example.chatapp.utils.FirebaseUtil

//Adapter to handle the recyclerview
class SearchUserRecyclerAdapter(
    private var users: List<User>,
    private val context: Context
) :
    RecyclerView.Adapter<SearchUserRecyclerAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameText = itemView.findViewById<TextView>(R.id.username_text)
        val phoneText: TextView = itemView.findViewById(R.id.phone_text)
        val profilePic = itemView.findViewById<ImageView>(R.id.profile_picture_image_view)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_user_recycler_row, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.usernameText.text = users[position].getUsername()
        holder.phoneText.text = users[position].getPhonenumber()
        if (users[position].getUid().trim('"').equals(FirebaseUtil.currentUserId())) {
            holder.usernameText.text = users[position].getUsername() + " (Me)"
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, TalkWithHomieActivity::class.java)
            AndroidUtil.passUserModelAsIntent(intent, users[position])
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun setUsers(users: List<User>) {
        this.users = users
        notifyDataSetChanged()
    }
}


