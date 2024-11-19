package com.example.visionconnect

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Visibility
import com.example.visionconnect.databinding.ActivityAcceptRequestBinding
import com.example.visionconnect.room.DAO
import com.example.visionconnect.room.appDatabase
import com.example.visionconnect.room.myChatsEntity
import com.google.firebase.firestore.FieldValue

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class acceptRequest : AppCompatActivity() {
    private lateinit var binding:ActivityAcceptRequestBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var currentUserEmail: String
    private lateinit var adapter:acceptListAdapter
    private lateinit var nameList : ArrayList<user>
    var senderName: String? = ""
    var senderPic: String? = ""
    var senderEmail: String? = ""
    var LASTmessage: String? = ""
    var time: String? = ""
    private var dao: DAO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAcceptRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.back.setOnClickListener {
            finish()
        }

        dao = appDatabase.getDatabase(this).chatDao()
        db = FirebaseFirestore.getInstance()
        currentUserEmail = intent.getStringExtra("currentUser")!!
        nameList = arrayListOf()
        setUpRecyclerView()
        loadRequests(currentUserEmail)
    }


    private fun loadRequests(currentUserEmail: String) {
        db.collection("CHAT_REQUESTS")
            .whereEqualTo("senderEmail", currentUserEmail)
            .whereEqualTo("status", "accepted")
            .addSnapshotListener{ snapshots, e ->
                if(e!=null){
                    Log.e("FirestoreError", e.message.toString())
                    return@addSnapshotListener
                }
                nameList.clear()
                snapshots?.let {
                    for(doc in it){
                        val request = doc.toObject(user::class.java).apply {
                            requestId = doc.id  // Ensure requestId is properly set
                        }
                        nameList.add(request)
                    }
                    if(nameList.isEmpty()){
                        binding.emptyText.visibility = View.VISIBLE
                        binding.addrecycler.visibility = View.GONE
                    }
                    else{
                        binding.emptyText.visibility = View.GONE
                        binding.addrecycler.visibility = View.VISIBLE
                    }
                    adapter.notifyDataSetChanged()
                }

            }
    }

    private fun setUpRecyclerView() {
        binding.addrecycler.layoutManager = LinearLayoutManager(this)
        adapter = acceptListAdapter(nameList,
            onAddbuttonClick= { user -> handleAdd(user)},
            db = db)
        binding.addrecycler.adapter = adapter
    }

    private fun handleAdd(user: user) {

        // Create or get the chat channel ID
        val chatId = createOrGetChatDocument(currentUserEmail, user.receiverEmail)
        Log.d("userSenderEmail is ", "${chatId}")
        Log.d("userSenderEmail is ", "${user.receiverEmail}")
        Log.d("userSenderEmail is ", "${currentUserEmail}")
        Log.d("userSenderEmail is ", "${user.requestId}")

        // Retrieve sender's details from Firestore
        db.collection("USERS")
            .document(user.receiverEmail)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    senderName = document.getString("name")
                    senderPic = document.getString("pic")
                    senderEmail = document.getString("email")

                    Log.d("got the sender email", "$senderEmail")

                    // Move Intent creation and startActivity here, after data retrieval
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("chatId", chatId)
                        putExtra("senderName", senderName)
                        putExtra("senderPic", senderPic)
                        putExtra("senderEmail", senderEmail)
                        putExtra("lastMessage", LASTmessage ?: "")
                        putExtra("time", time ?: "")
                    }

                    val userDetail = myChatsEntity(
                        Id = 0,  // 0 or null if using auto-generation
                        name = senderName ?: "Unknown",
                        currentUserEmail = currentUserEmail,
                        senderEmail = senderEmail ?: "Unknown",
                        image = senderPic ?: "",
                        chatId = chatId,
                        lastMessage = LASTmessage ?: "",  // Default empty string if null
                        time = time ?: ""  // Default empty string if null
                    )

                    Log.d("userDetail", "$userDetail")

                    // Insert data into the database
                    lifecycleScope.launch {
                        try {
                            dao?.insertChat(userDetail)
                            Log.d("Database Insert", "Chat inserted successfully")
                        } catch (e: Exception) {
                            Log.e(
                                "Database Error",
                                "Error inserting chat: ${e.message}"
                            )
                        }
                    }

                    Log.d("Chat Fragment ID", chatId)
                    Log.d("Sender Name", "$senderName")
                    Log.d("Sender Pic", "$senderPic")
                    startActivity(intent)
                    db.collection("CHAT_REQUESTS").document(user.requestId).delete()
                    finish() // Finish current activity after starting MainActivity
                }
            }
    }



    private fun createOrGetChatDocument(
        currentUserEmail: String,
        selectedUserEmail: String
    ): String {
        val db = FirebaseFirestore.getInstance()
        val chatId = if (currentUserEmail < selectedUserEmail) {
            "${currentUserEmail}_$selectedUserEmail"
        } else {
            "${selectedUserEmail}_$currentUserEmail"
        }

        val chatRef = db.collection("CHATS").document(chatId)

        chatRef.get().addOnSuccessListener { document ->
            if (document.exists()) { // Chat document already exists
                Log.d("Firestore", "Chat already exists")
            } else {
                // Chat document does not exist, create it
                val chatData = hashMapOf(
                    "users" to listOf(currentUserEmail, selectedUserEmail),
                    "createdAt" to FieldValue.serverTimestamp()
                )

                chatRef.set(chatData)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Chat created successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error creating chat", e)
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error fetching chat document", e)
        }
        return chatId
    }

}