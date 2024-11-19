package com.example.visionconnect.fragments

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

data class userDetailModel(
    val name : String? = null,
    val image : String? = null,
    val lastMessage :String? = null,
    val email : String? = null,
    val time: Timestamp? = null
)
