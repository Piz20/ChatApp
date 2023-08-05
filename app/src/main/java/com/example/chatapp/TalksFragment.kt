package com.example.chatapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.adapter.RecentChatRecyclerAdapter
import com.example.chatapp.adapter.UiListener
import com.example.chatapp.models.ChatRoom
import com.example.chatapp.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query

class TalksFragment : Fragment(), UiListener {
    lateinit var recentchatRecyclerView: RecyclerView
    lateinit var recentchatAdapter: RecentChatRecyclerAdapter
    lateinit var progressBar: ProgressBar
    lateinit var notalksTextView: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_talks, container, false)
        recentchatRecyclerView = view.findViewById(R.id.recent_chat_recycler_view)
        progressBar = view.findViewById(R.id.recent_chat_progressbar)
        notalksTextView = view.findViewById(R.id.no_talks_textView)
        setupRecentChatRecyclerView()
        return view
    }

    private fun setupRecentChatRecyclerView() {
        val query: Query = FirebaseUtil.allChatRoomCollectionReference()
            .whereArrayContains("userIds", FirebaseUtil.currentUserId()!!)
            .orderBy("lastMessageDate", Query.Direction.DESCENDING)

        val options: FirestoreRecyclerOptions<ChatRoom?> =
            FirestoreRecyclerOptions.Builder<ChatRoom>()
                .setQuery(query, ChatRoom::class.java).build()

        recentchatAdapter = context?.let { RecentChatRecyclerAdapter(options, it, this) }!!
        val linearLayoutManager = LinearLayoutManager(context)
        recentchatRecyclerView.layoutManager = linearLayoutManager
        recentchatRecyclerView.adapter = recentchatAdapter
        recentchatAdapter.startListening()
}

override fun hideProgressBar() {
    progressBar.visibility = View.GONE
}

override fun showNoTalksTextView() {
    notalksTextView.visibility = View.VISIBLE
}

    override fun hideNoTalksTextView() {
        notalksTextView.visibility = View.GONE
    }

}
