package com.example.visionconnect

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.visionconnect.databinding.ActivityAllUserListBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class allUserList : AppCompatActivity() {
    private lateinit var binding : ActivityAllUserListBinding
    private lateinit var adapter : recyclerViewAdapter
    private lateinit var userList : ArrayList<userInfo>
    private lateinit var db : FirebaseFirestore
    private lateinit var addedUserList : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAllUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        db = FirebaseFirestore.getInstance()
        binding.RVList.layoutManager=LinearLayoutManager(this)
        binding.RVList.setHasFixedSize(true)

        //getting currentUserEmail
        val userEmail = intent.getStringExtra("currentUserEmail")

        val addedUserList = intent.getSerializableExtra("addedUserList") as? ArrayList<*>
        if(addedUserList != null){
            Log.d("list email is " , "$addedUserList")
        }
        else{
            Log.d("list is ", "EMPTY")
        }


        binding.backButton.setOnClickListener {
            finish()
        }

        userList = arrayListOf()

        getinfoDb(userEmail!!, addedUserList)

        adapter = recyclerViewAdapter(userList)

        // Show the ProgressBar and hide the RecyclerView initially
        binding.progressBar.visibility = View.VISIBLE
        binding.RVList.visibility = View.GONE

        binding.RVList.adapter = adapter

        adapter.onItemClicked = { selectedUser ->
            // Check if there's already a pending request
            db.collection("CHAT_REQUESTS")
                .whereEqualTo("senderEmail", userEmail)
                .whereEqualTo("receiverEmail", selectedUser.email)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        // No pending request exists, so create a new one
                        val chatRequest = hashMapOf(
                            "senderEmail" to userEmail,
                            "receiverEmail" to selectedUser.email,
                            "status" to "pending"
                        )

                        db.collection("CHAT_REQUESTS")
                            .add(chatRequest)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Chat request sent to ${selectedUser.name}", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("ChatRequestError", e.message.toString())
                                Toast.makeText(this, "Failed to send request", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "You have already sent a request to ${selectedUser.name}.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("CheckRequestError", e.message.toString())
                    Toast.makeText(this, "Failed to check existing requests", Toast.LENGTH_SHORT).show()
                }
        }



        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
               return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                filterList(p0)
                return true
            }

        })
    }

    private fun filterList(query: String?) {
       if(query != null){
           val filterList = ArrayList<userInfo>()
          for(i in userList){
              if (i.name.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))){
                  filterList.add(i)
              }
          }
          adapter.setNewList(filterList)
       }
    }

    private fun getinfoDb(currentUserEmail: String, addedUserList: ArrayList<*>?) {
        db.collection("USERS")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val email = document.getString("email") ?: ""

                    // Skip if the email matches the current user's email or exists in addedUserList
                    if (email != currentUserEmail && (addedUserList == null || !addedUserList.contains(email))) {
                        val name = document.getString("name") ?: ""
                        val pic = document.getString("pic") ?: ""
                        val user = userInfo(name, pic, email)
                        userList.add(user)
                    }
                }
                adapter.notifyDataSetChanged()

                // Hide the ProgressBar and show the RecyclerView once the data is loaded
                binding.progressBar.visibility = View.GONE
                binding.RVList.visibility = View.VISIBLE
            }
            .addOnFailureListener {
                Log.e("Error", it.message.toString())
                // Hide the ProgressBar in case of an error
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
    }
}