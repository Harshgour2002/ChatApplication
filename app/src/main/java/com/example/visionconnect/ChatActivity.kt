package com.example.visionconnect

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.visionconnect.databinding.ActivityChatBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ChatActivity : AppCompatActivity() {

    private lateinit var binding : ActivityChatBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var messageAdapter: messageAdaptor
    private val messages = ArrayList<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        val currentUserEmail = intent.getStringExtra("currentUserEmail")
        val selectedUserName = intent.getStringExtra("selectedUserName")
        val selectedUserEmail = intent.getStringExtra("selectedUserEmail")
        val selectedUserPic = intent.getStringExtra("selectedUserImage")


        binding.back.setOnClickListener{
            finish()
        }


        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        messageAdapter = messageAdaptor(this, messages, currentUserEmail!!)
        binding.recyclerView.adapter = messageAdapter


        binding.ReceiverName.text = selectedUserName
        Glide.with(this)
            .load(selectedUserPic)
            .error(R.drawable.user_profile_4255__1_)
            .into(binding.receiverDP)


            GetChatDocument(currentUserEmail, selectedUserEmail!!)

            binding.sendButton.setOnClickListener {
                val message = binding.messageInput.text.toString()
                if(message.isNotEmpty()) {
                    sendMessage(message, currentUserEmail, selectedUserEmail)
                    binding.messageInput.text!!.clear()
                }
            }

    }

    private fun GetChatDocument(currentUserEmail: String, selectedUserEmail: String) {
        val chatId = if (currentUserEmail < selectedUserEmail) {
            "${currentUserEmail}_$selectedUserEmail"
        } else {
            "${selectedUserEmail}_$currentUserEmail"
        }

        loadMessages(chatId, currentUserEmail)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMessages(chatId: String, currentUserEmail: String) {
        val chatRef = db.collection("CHATS").document(chatId).collection("messages")
            .orderBy("timestamp")

        chatRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.e("Firestore", "Listen failed", e)
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                messages.clear()
                for (document in snapshots.documents) {
                    val message = document.toObject(Message::class.java)
                    if (message != null) {
                        messages.add(message)
                    }
                }

                messageAdapter.notifyDataSetChanged()
                binding.recyclerView.scrollToPosition(messages.size - 1)
            } else {
                Log.d("Firestore", "No messages found")
            }
        }
    }


    private fun sendMessage(messageText: String, currentUserEmail: String, selectedUserEmail: String) {
        val db = FirebaseFirestore.getInstance()
        val chatId = if (currentUserEmail < selectedUserEmail) {
            "${currentUserEmail}_$selectedUserEmail"
        } else {
            "${selectedUserEmail}_$currentUserEmail"
        }

        val messageData = hashMapOf(
            "senderEmail" to currentUserEmail,
            "receiverEmail" to selectedUserEmail,
            "message" to messageText,
            "timestamp" to FieldValue.serverTimestamp()
        )

        val chatRef = db.collection("CHATS").document(chatId).collection("messages")

        chatRef.add(messageData)
            .addOnSuccessListener {
                Log.d("Firestore", "Message sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error sending message", e)
            }
    }
}