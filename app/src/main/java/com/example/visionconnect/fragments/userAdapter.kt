// Adapter Class
package com.example.visionconnect.fragments

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.visionconnect.R
import com.example.visionconnect.room.myChatsEntity
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class userAdapter(
    private val context: Context,
) : ListAdapter<myChatsEntity, userAdapter.myViewHolder>(UserDiffCallback()) {

    class myViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.chatName)
        val userimage: ShapeableImageView = itemView.findViewById(R.id.DP)
        val recentMessage: TextView = itemView.findViewById(R.id.recentMsgText)
        val time: TextView = itemView.findViewById(R.id.time)
    }

    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var onItemClicked: ((myChatsEntity) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.cardview, parent, false)
        return myViewHolder(view)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val chatEntity: myChatsEntity = getItem(position)
        holder.username.text = chatEntity.name

        Glide.with(holder.itemView.context)
            .load(chatEntity.image)
            .error(R.drawable.person)
            .into(holder.userimage)

        // Display placeholder or loading indicator
        holder.recentMessage.text = "Loading..." // Or a suitable placeholder
        holder.time.text = ""

        // Implement the real logic
        val chatId = chatEntity.chatId

        val chatRef = db.collection("CHATS")
            .document(chatId)
            .collection("messages")

        // Retrieve the last message in the chat
        chatRef.orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
            .addSnapshotListener { querySnapshot, error -> // Use snapshot listener
                if (error != null) {
                    Log.e("FirestoreError", "Error fetching last message: ${error.message}")
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    if (!querySnapshot.isEmpty()) {
                        val lastMessage = querySnapshot.documents[0]
                        val message = lastMessage.getString("message")
                        val timestamp = lastMessage.getTimestamp("timestamp")

                        // Format the timestamp
                        val formattedTime = timestamp?.toDate()?.let {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        }

                        // Update the view holder
                        holder.recentMessage.text = message
                        holder.time.text = formattedTime
                    } else {
                        // Handle the case where there are no messages
                        holder.recentMessage.text = "No messages yet"
                        holder.time.text = ""
                    }
                }
            }


        holder.itemView.setOnClickListener {
            onItemClicked?.invoke(chatEntity)
        }
    }

        class UserDiffCallback : DiffUtil.ItemCallback<myChatsEntity>() {
            override fun areItemsTheSame(oldItem: myChatsEntity, newItem: myChatsEntity): Boolean {
                return oldItem.chatId == newItem.chatId
            }

            override fun areContentsTheSame(
                oldItem: myChatsEntity,
                newItem: myChatsEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
    }


