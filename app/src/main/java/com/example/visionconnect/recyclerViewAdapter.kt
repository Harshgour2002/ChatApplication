package com.example.visionconnect

import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class recyclerViewAdapter(var userList: ArrayList<userInfo>): RecyclerView.Adapter<recyclerViewAdapter.ViewHolder>() {

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val Name: TextView = itemView.findViewById(R.id.chatName)
        val image: ShapeableImageView = itemView.findViewById(R.id.DP)
    }

    var onItemClicked : ((userInfo) -> Unit)? = null

    fun setNewList(newList : ArrayList<userInfo>){
        this.userList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): recyclerViewAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.all_user_list,
                                parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: recyclerViewAdapter.ViewHolder, position: Int) {
        val user : userInfo = userList[position]
        holder.Name.text = user.name
        Glide.with(holder.itemView.context)
            .load(user.pic)
            .error(R.drawable.user_profile_4255__1_)
            .into(holder.image)


        holder.itemView.setOnClickListener {
            onItemClicked?.invoke(user)
        }
    }

    override fun getItemCount(): Int {
       return userList.size
    }
}