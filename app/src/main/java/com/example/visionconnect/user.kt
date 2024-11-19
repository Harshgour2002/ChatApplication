package com.example.visionconnect

data class user(
    var requestId:String = "",
    val senderEmail: String = "",
    val receiverEmail:String = "",
    val status:String = "pending"
)
