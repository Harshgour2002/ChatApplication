package com.example.visionconnect

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.visionconnect.databinding.ActivityNotificationBinding
import com.example.visionconnect.room.DAO
import com.example.visionconnect.room.appDatabase
import com.example.visionconnect.room.myChatsEntity
import com.example.visionconnect.room.roomModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale
import kotlin.math.log

class notification : AppCompatActivity() { // Naming convention improved
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var requestList: ArrayList<user>
    private lateinit var adapter: requestListAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUserEmail: String
    var senderName: String? = ""
    var senderPic: String? = ""
    var senderEmail: String? = ""
    var LASTmessage: String? = ""
    var time: String? = ""
    private var dao: DAO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // Set up edge-to-edge display and insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dao = appDatabase.getDatabase(this).chatDao()
        currentUserEmail = intent.getStringExtra("currentUser")!!

        // Back button setup
        binding.back.setOnClickListener {
            finish()
        }

        // Initialize Firestore, RecyclerView, and adapter
        requestList = arrayListOf()
        db = FirebaseFirestore.getInstance()
        setupRecyclerView()
        loadRequests(currentUserEmail!!)


    }


    private fun setupRecyclerView() {
        binding.requestRecycler.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with click listeners
        adapter = requestListAdapter(
            requestList,
            onAcceptClicked = { user -> handleAccept(user) },
            onRejectClicked = { user -> handleReject(user) },
            db = db // Pass Firestore instance to adapter for efficiency

        )
        binding.requestRecycler.adapter = adapter
    }


    private fun loadRequests(currentUser: String) {

        db.collection("CHAT_REQUESTS")
            .whereEqualTo("receiverEmail", currentUser)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("FirestoreError", e.message.toString())
                    return@addSnapshotListener
                }

                // Populate requestList and notify adapter
                requestList.clear()
                snapshots?.let {
                    for (doc in it) {
                        val request = doc.toObject(user::class.java).apply {
                            requestId = doc.id  // Ensure requestId is properly set
                        }
                        requestList.add(request)
                    }
                    if (requestList.isEmpty()) {
                        binding.emptyText.visibility = View.VISIBLE
                        binding.requestRecycler.visibility = View.GONE
                    } else {
                        binding.emptyText.visibility = View.GONE
                        binding.requestRecycler.visibility = View.VISIBLE

                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    // Handle accept click action
        private fun handleAccept(user: user) {
        // Update status to accepted
        db.collection("CHAT_REQUESTS").document(user.requestId).update("status", "accepted")

        // Create or get the chat channel ID
        val chatId = createOrGetChatDocument(currentUserEmail, user.senderEmail)

        // Retrieve sender's details from Firestore
        db.collection("USERS")
            .document(user.senderEmail)
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
                        lastMessage = LASTmessage
                            ?: "",  // Default empty string if null
                        time = time ?: ""  // Default empty string if null
                    )

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

                    Log.d("Chat Fragment ID", "$chatId")
                    Log.d("Sender Name", "$senderName")
                    Log.d("Sender Pic", "$senderPic")

                    startActivity(intent)
                    finish() // Finish current activity after starting MainActivity
                }
            }
    }


        // Handle reject click action
        private fun handleReject(user: user) {
            // Add logic to reject chat request, update Firestore, etc.
            Log.d("NotificationActivity", "Rejected clicked for ${user.senderEmail}")

            db.collection("CHAT_REQUESTS").document(user.requestId).delete()
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
