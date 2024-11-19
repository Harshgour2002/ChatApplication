package com.example.visionconnect.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_ChatList_Table")
data class myChatsEntity(
    @PrimaryKey (autoGenerate = true) val Id : Int,
    @ColumnInfo(name = "name") val name : String,
    @ColumnInfo(name = "currentUserEmail") val currentUserEmail : String,
    @ColumnInfo(name = "senderEmail") val senderEmail : String?=null,
    @ColumnInfo(name = "imageURI") val image : String?=null,
    @ColumnInfo(name = "chatID") val chatId : String,
    @ColumnInfo(name = "lastMessage") val lastMessage : String,
    @ColumnInfo(name = "time") val time : String
)
