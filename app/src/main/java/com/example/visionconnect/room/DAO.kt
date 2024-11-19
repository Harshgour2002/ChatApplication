package com.example.visionconnect.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DAO {
    @Insert
    suspend fun insertChat(myChatsEntity: myChatsEntity)

    @Update
    suspend fun updateChat(myChatsEntity: myChatsEntity)

    @Query("DELETE FROM my_ChatList_Table WHERE Id = :Id")
    suspend fun deleteById(Id:Int)

    @Query("SELECT * FROM  my_chatlist_table WHERE  currentUserEmail = :currentUserEmail")
    fun getAllChats(currentUserEmail:String): Flow<List<myChatsEntity>>


}