package com.example.visionconnect

import android.content.Context
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class messageAdaptor(val context : Context,
                     val messageList : ArrayList<Message>,
                     val currentUserEmail : String, )
                    : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    class SendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sendText: TextView = itemView.findViewById(R.id.send_message)
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveText: TextView = itemView.findViewById(R.id.receive_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            // Sending message
            val view = LayoutInflater.from(context).inflate(R.layout.send_message, parent, false)
            SendViewHolder(view)
        } else {
            // Receiving message
            val view = LayoutInflater.from(context).inflate(R.layout.receive_message, parent, false)
            ReceiveViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        if (holder is SendViewHolder) {
            holder.sendText.text = currentMessage.message
        } else if (holder is ReceiveViewHolder) {
            holder.receiveText.text = currentMessage.message
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].senderEmail == currentUserEmail) {
            1 // Send message
        } else {
            2 // Receive message
        }
    }
}