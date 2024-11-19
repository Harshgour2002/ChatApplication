package com.example.visionconnect

import android.os.Bundle
import android.print.PrintDocumentAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.visionconnect.fragments.callsFragment
import com.example.visionconnect.fragments.chatFragment

class pagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val chatId: String?,// Accept chatId as a parameter
    private val senderName: String?,
    private val senderPic: String?,
    private val currentUserEmail: String
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2 // Number of fragments
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            val chatFragment = chatFragment()
            chatFragment.arguments = Bundle().apply {
                putString("chatId", chatId)
                putString("senderName", senderName)
                putString("senderPic", senderPic)
                putString("currentUserEmail", currentUserEmail)
            }
            chatFragment
        } else {
            callsFragment()
        }
    }

    override fun getItemId(position: Int): Long {
        // Assign a unique ID for each fragment
        return position.toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return itemId in 0..1
    }

}
