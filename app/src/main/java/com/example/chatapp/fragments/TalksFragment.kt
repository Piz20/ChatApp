package com.example.chatapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.adapter.RecentChatRecyclerAdapter
import com.example.chatapp.interfaces.UiListener
import com.example.chatapp.models.ChatRoom
import com.example.chatapp.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TalksFragment : Fragment(), UiListener {
    lateinit var recentchatRecyclerView: RecyclerView
    lateinit var recentchatAdapter: RecentChatRecyclerAdapter
    lateinit var progressBar: ProgressBar
    lateinit var deleteallchatroomsButton: ImageButton
    lateinit var notalksTextView: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_talks, container, false)
        recentchatRecyclerView = view.findViewById(R.id.recent_chat_recycler_view)
        progressBar = view.findViewById(R.id.recent_chat_progressbar)
        deleteallchatroomsButton = requireActivity().findViewById(R.id.delete_all_chatrooms_button)
        notalksTextView = view.findViewById(R.id.no_talks_textView)
        setupRecentChatRecyclerView()
        setupUi()
        return view
    }

    //Basic ui setup
    fun setupUi() {
        deleteallchatroomsButton.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Delete all the chats ?")
                .setPositiveButton("OK") { dialog, _ ->
                    setInProgress(true)
                    deleteAllChatRoomsForTheCurrentUser()
                }
                .setNegativeButton("Cancel") { dialog, _ ->

                }
                .create()
            dialog.show()
        }
    }

    private fun setupRecentChatRecyclerView() {
        val query: Query = FirebaseUtil.allChatRoomCollectionReference()
            .whereArrayContains("userIds", FirebaseUtil.currentUserId()!!)
            .whereNotEqualTo("lastMessageDate",null)
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

    //When we want to delete all chats
    fun setInProgress(boolean: Boolean) {
        if (boolean) {
            recentchatRecyclerView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
            recentchatRecyclerView.visibility = View.VISIBLE
        }
    }

    //hide progressbar
    override fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    //hide notalkstextview
    override fun hideNoTalksTextView(boolean: Boolean) {
        if (boolean)
            notalksTextView.visibility = View.GONE
        else
            notalksTextView.visibility = View.VISIBLE
    }

    //hide deleteallchatroomsbutton
    override fun hideDeleteAllChatRoomsButton(boolean: Boolean) {
        if (boolean)
            deleteallchatroomsButton.visibility = View.GONE
        else
            deleteallchatroomsButton.visibility = View.VISIBLE

    }

    //deleteAllChatRooms for the current user
    fun deleteAllChatRoomsForTheCurrentUser() {
        FirebaseUtil.allChatRoomCollectionReference()
            .whereArrayContains("userIds", FirebaseUtil.currentUserId()!!).get()
            .addOnSuccessListener {
                for (documentSnapshot: DocumentSnapshot in it) {
                    deleteDocumentAndSubcollection("chatrooms", documentSnapshot.id, "chats")
                }
            }
    }

    fun deleteDocumentAndSubcollection(
        collectionPath: String,
        documentId: String,
        subcollectionPath: String
    ) {
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
                setInProgress(false)
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
                        setInProgress(false)
                    }
                    .addOnFailureListener {
                        setInProgress(false)
                    }
            }
            .addOnFailureListener {
                setInProgress(false)
            }
    }


}


