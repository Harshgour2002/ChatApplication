package com.example.visionconnect

import com.google.firebase.Timestamp

data class Message(
    val senderEmail: String = "",
    val receiverEmail: String = "",
    val message: String = "",
    val timestamp: Timestamp? = null


)
