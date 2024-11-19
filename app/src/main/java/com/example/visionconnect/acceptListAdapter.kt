package com.example.visionconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapePath
import com.google.firebase.firestore.FirebaseFirestore

class acceptListAdapter(val nameList : ArrayList<user>,
                        val onAddbuttonClick: (user) -> Unit,
                        val db:FirebaseFirestore) :
    RecyclerView.Adapter<acceptListAdapter.accepetViewHolder>() {



    class accepetViewHolder(itemView :View) :RecyclerView.ViewHolder(itemView) {
        val username:TextView = itemView.findViewById(R.id.acceptName)
        val image:ShapeableImageView = itemView.findViewById(R.id.acceptImage)
        val addButton: Button = itemView.findViewById(R.id.add)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): accepetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.request_accept,parent,false)
        return accepetViewHolder(view)
    }

    override fun onBindViewHolder(holder: accepetViewHolder, position: Int) {
        val currentName = nameList[position]
        val senderName = currentName.receiverEmail
        db.collection("USERS")
            .document(senderName)
            .get()
            .addOnSuccessListener { doc->
                val name = doc.getString("name")
                val picUrl = doc.getString("pic")
                holder.username.text = name

                if (!picUrl.isNullOrEmpty()) {
                    // Load the profile picture if the URL is not null or empty
                    Glide.with(holder.image.context)
                        .load(picUrl)
                        .placeholder(R.drawable.person) // Default image
                        .into(holder.image)
                } else {
                    // Set default image if pic URL is empty or null
                    holder.image.setImageResource(R.drawable.person)
                }
            }
            .addOnFailureListener {
                Toast.makeText(holder.itemView.context,it.localizedMessage?.toString(),Toast.LENGTH_SHORT).show()
            }
        holder.addButton.setOnClickListener {
            onAddbuttonClick(currentName)
        }

    }

    override fun getItemCount(): Int {
        return nameList.size
    }


}