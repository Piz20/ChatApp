package com.example.chatapp.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseUtil {

    fun currentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun isLoggedIn(): Boolean = currentUserId() != null

    fun signIn(phoneAuthCredential: PhoneAuthCredential) =
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)

    fun reauthenticate(phoneAuthCredential: PhoneAuthCredential) =
        FirebaseAuth.getInstance().currentUser!!.reauthenticate(phoneAuthCredential)
    fun signOut() = FirebaseAuth.getInstance().signOut()

    fun currentUserDetails(): DocumentReference {
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId()!!)
    }

    fun usersCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("users")
    }

    fun getChatroomReference(chatroomId: String?): DocumentReference {
        return chatroomId?.let {
            FirebaseFirestore.getInstance().collection("chatrooms").document(it)
        }!!
    }

    fun getChatroomMessageference(chatroomId: String?): CollectionReference =
        getChatroomReference(chatroomId).collection("chats")

    fun getChatRoomId(userId1: String, userId2: String?): String =
        if (userId1.hashCode() > userId2.hashCode())
            userId1 + "_" + userId2?.trim()
        else
            userId2?.trim() + "_" + userId1

    fun allChatRoomCollectionReference(): CollectionReference =
        FirebaseFirestore.getInstance().collection("chatrooms")

    fun getOtherUserFromChatRoom(userIds: List<String>): DocumentReference {
        return if (userIds[0] == currentUserId()) {
            usersCollection().document(userIds[1])
        } else {
            usersCollection().document(userIds[0])
        }
    }

    fun simpleDateFormat(date : Date, format : String) : String{
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(date)
    }

    fun getCurrentUserProfilePicStorageReference() : StorageReference?  =
         currentUserId()?.let {
            FirebaseStorage.getInstance().getReference().child("profile_picture")
                .child(it)
    }
    fun getOtherUserProfilePicStorageReference(uid : String) : StorageReference =
        FirebaseStorage.getInstance().getReference().child("profile_picture")
            .child(uid)

}