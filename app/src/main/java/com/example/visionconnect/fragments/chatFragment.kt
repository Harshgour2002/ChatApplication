package com.example.visionconnect.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.visionconnect.ChatActivity
import com.example.visionconnect.databinding.FragmentChatBinding
import com.example.visionconnect.room.DAO
import com.example.visionconnect.room.appDatabase
import com.example.visionconnect.room.myChatsEntity
import com.example.visionconnect.user
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class chatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private var chatId: String? = null
    private var senderName: String? = null
    private var senderPic: String? = null
    private lateinit var currentUserEmail :String
    private lateinit var adapter: userAdapter
    private var senderEmail: String? = ""
    private var lastMessage: String? = ""
    private var time: String? = ""
    private lateinit var requestList : ArrayList<user>
    private lateinit var db: FirebaseFirestore
    private var dao: DAO? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        // Initialize RecyclerView

        requestList = arrayListOf()
        db = FirebaseFirestore.getInstance()
        binding.recyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        adapter = userAdapter(this.requireContext())
        binding.recyclerView.adapter = adapter

        adapter.onItemClicked = { selectedUser ->
            Log.d("AllUsersActivity", "currentUserEmail: ${selectedUser.currentUserEmail}")
            val intent = Intent(this.requireContext(), ChatActivity::class.java)
            intent.putExtra("selectedUserEmail", selectedUser.senderEmail)
            intent.putExtra("currentUserEmail", selectedUser.currentUserEmail)
            intent.putExtra("selectedUserName", selectedUser.name)
            intent.putExtra("selectedUserImage", selectedUser.image)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatId = arguments?.getString("chatId")
        senderName = arguments?.getString("senderName")
        senderPic = arguments?.getString("senderPic")
        currentUserEmail = arguments?.getString("currentUserEmail")!!
        senderEmail = arguments?.getString("senderEmail")
        lastMessage = arguments?.getString("lastMessage")
        time = arguments?.getString("time")

        dao = appDatabase.getDatabase(requireContext().applicationContext).chatDao()

        binding.recyclerView.visibility = View.VISIBLE
        binding.nochats.visibility = View.GONE


        lifecycleScope.launch {
            dao?.getAllChats(currentUserEmail)?.collect{ mlist->
                if(mlist.isEmpty()){
                    binding.recyclerView.visibility = View.GONE
                    binding.nochats.visibility = View.VISIBLE
                }
                else{
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.nochats.visibility = View.GONE
                    adapter.submitList(mlist)
                }
            }
        }
    }


}

