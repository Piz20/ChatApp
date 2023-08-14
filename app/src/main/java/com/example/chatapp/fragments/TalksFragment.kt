package com.example.chatapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.firebase.firestore.FirebaseFirestore
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
            .whereNotEqualTo("lastMessage","")
            .orderBy("lastMessage")
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
    fun deleteDocumentAndSubcollection(collectionPath: String, documentId: String, subcollectionPath: String) {
        val db = FirebaseFirestore.getInstance()
        val documentReference = db.collection(collectionPath).document(documentId)

        // Suppression du document principal
        documentReference.delete()
            .addOnSuccessListener {
                // Suppression des documents dans la sous-collection
                val subcollectionReference = documentReference.collection(subcollectionPath)
                deleteCollection(subcollectionReference)
            }
            .addOnFailureListener { exception ->
                // Gérer les erreurs
                println("Erreur lors de la suppression du document principal : $exception")
            }
    }

    fun deleteCollection(collection: CollectionReference) {
        collection.get()
            .addOnSuccessListener { querySnapshot ->
                val batch = collection.firestore.batch()

                for (documentSnapshot in querySnapshot) {
                    batch.delete(documentSnapshot.reference)
                }

                // Exécution de la suppression en lot pour la sous-collection
                batch.commit()
                    .addOnSuccessListener {
                        println("Sous-collection supprimée avec succès")
                    }
                    .addOnFailureListener { exception ->
                        println("Erreur lors de la suppression de la sous-collection : $exception")
                    }
            }
            .addOnFailureListener { exception ->
                println("Erreur lors de la récupération des documents de la sous-collection : $exception")
            }}

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
