package com.example.visionconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.visionconnect.databinding.ActivityUserDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class userDetails : AppCompatActivity() {
    private lateinit var binding: ActivityUserDetailsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    var fileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference.child("images")

        binding.back.setOnClickListener {
            finish()
        }

        var imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
               Glide.with(this)
                   .load(uri)
                   .into(binding.imageView)
                fileUri = uri
            } else {
                Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
            }
        }
        binding.cameraBtn.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.Register.setOnClickListener {
            val name = binding.registerName.text.toString()
            val nameResult = validateName(name)

            if (nameResult.first) {
                val email = intent.getStringExtra("userEmail")
                val password = intent.getStringExtra("userPassword")
                val phone = intent.getStringExtra("userPhone")
                val userMap = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "password" to password,
                    "phone" to phone
                )
                val user = db.collection("USERS")
                user.whereEqualTo("email", email).get()
                    .addOnSuccessListener {
                        if (it.isEmpty) {
                            auth.createUserWithEmailAndPassword(email!!, password!!)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        user.document(email).set(userMap)
                                        uploadImage(email)
                                        Toast.makeText(this, "User Registration Complete, please Login", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, loginAtivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                    .addOnFailureListener { task ->
                        Toast.makeText(this, task.localizedMessage, Toast.LENGTH_SHORT).show()
                    }

            } else {
                // Show error message and set error line color
                binding.registerName.error = nameResult.second
                binding.registerName.backgroundTintList = ContextCompat.getColorStateList(this, R.color.red) // Use your error color
            }
        }
    }

    private fun uploadImage(email: String) {
        if (fileUri != null) {
            // Use a unique identifier for each user to organize their images
            val userImageRef = storageReference.child("$email/${UUID.randomUUID()}.jpg")
            fileUri?.let { it ->
                userImageRef.putFile(it)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            userImageRef.downloadUrl.addOnSuccessListener { uri ->
                                val map = HashMap<String, Any>()
                                map["pic"] = uri.toString()
                                db.collection("USERS").document(email).update(map)
                                    .addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            Toast.makeText(this, "Image Uploaded", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(this, dbTask.exception?.message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        } else {
                            Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        
    }

    private fun validateName(name: String): Pair<Boolean, String> {
        val namePattern = "^[A-Za-z]+([ '-][A-Za-z]+)*$"
        return if (name.matches(namePattern.toRegex())) {
            Pair(true, "Valid name format.")
        } else {
            Pair(false, "Invalid name format. Please use only alphabetic characters, spaces, hyphens, or apostrophes.")
        }
    }
}
