package com.example.visionconnect

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.api.LaunchStage
import com.google.firebase.firestore.FirebaseFirestore

class requestListAdapter(
    private val requestList: ArrayList<user>,
    private val onAcceptClicked: (user) -> Unit,
    private val onRejectClicked: (user) -> Unit,
    private val db: FirebaseFirestore// Pass Firestorm instance through constructor
) : RecyclerView.Adapter<requestListAdapter.RequestViewHolder>() {

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.name)
        val profileImageView: ShapeableImageView = itemView.findViewById(R.id.dp)
        val acceptButton: ImageButton = itemView.findViewById(R.id.accept)
        val rejectButton: ImageButton = itemView.findViewById(R.id.reject)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): requestListAdapter.RequestViewHolder {

            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.request_layout, parent, false)
            return RequestViewHolder(view)
    }



    override fun onBindViewHolder(holder: requestListAdapter.RequestViewHolder, position: Int) {


            val currentRequest = requestList[position]
            // Set user name
            val senderName = currentRequest.senderEmail

            db.collection("USERS").document(senderName).get()
                .addOnSuccessListener { task ->
                    val name = task.getString("name")
                    holder.nameTextView.text = name
                }
                .addOnFailureListener { task ->
                    Toast.makeText(
                        holder.itemView.context,
                        task.localizedMessage?.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            val senderEmail = currentRequest.senderEmail

            // Fetch profile picture from Firestore for the sender
            db.collection("USERS").document(senderEmail).get()
                .addOnSuccessListener { task ->
                    val picUrl = task.getString("pic") // Fetch the pic URL from Firestore
                    if (!picUrl.isNullOrEmpty()) {
                        // Load the profile picture if the URL is not null or empty
                        Glide.with(holder.profileImageView.context)
                            .load(picUrl)
                            .placeholder(R.drawable.person) // Default image
                            .into(holder.profileImageView)
                    } else {
                        // Set default image if pic URL is empty or null
                        holder.profileImageView.setImageResource(R.drawable.person)
                    }
                }
                .addOnFailureListener {
                    // Handle the error, possibly log or show default image if fetching fails
                    holder.profileImageView.setImageResource(R.drawable.person)
                }


            // Accept button click listener
            holder.acceptButton.setOnClickListener {
                onAcceptClicked(currentRequest)
            }

            // Reject button click listener
            holder.rejectButton.setOnClickListener {
                onRejectClicked(currentRequest)
            }

    }

    override fun getItemCount(): Int {
        return requestList.size
    }






    // Update the list data and notify the adapter
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: ArrayList<user>) {
        requestList.clear()
        requestList.addAll(newList)
        notifyDataSetChanged()
    }
}
