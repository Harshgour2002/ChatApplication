package com.example.visionconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.visionconnect.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class signUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.RbackBtn.setOnClickListener {
            finish()
        }
        binding.textlogin.setOnClickListener {
            startActivity(Intent(this, loginAtivity::class.java))
            finish()
        }
        binding.Register.setOnClickListener {

            val email = binding.registerEmail.text.toString().trim()
            val password = binding.registerPassword.text.toString().trim()
            val phone = binding.registerPhone.text.toString().trim()

            val isValid = check(email, password, phone)
            if (isValid.first) {
                val user = db.collection("USERS")
                user.whereEqualTo("email", email).get()
                    .addOnSuccessListener { documents->
                        if(documents.isEmpty){
                            val intent = Intent(this, userDetails::class.java)
                            intent.putExtra("userEmail", email)
                            intent.putExtra("userPassword", password)
                            intent.putExtra("userPhone", phone)
                            startActivity(intent)
                        }
                        else{
                            Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }

            } else {
                Toast.makeText(this, isValid.second, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun check(email: String, password: String, phone: String): Pair<Boolean, String> {
        if (email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            return Pair(false, "All fields must be filled.")
        }

        val emailResult = validateEmail(email)
        if (!emailResult.first) {
            return emailResult
        }

        val phoneResult = validatePhoneNumber(phone)
        if (!phoneResult.first) {
            return phoneResult
        }

        val passwordResult = validatePassword(password)
        if (!passwordResult.first) {
            return passwordResult
        }

        return Pair(true, "")
    }

    private fun validateEmail(email: String): Pair<Boolean, String> {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return if (email.matches(emailPattern.toRegex())) {
            Pair(true, "Valid email format.")
        } else {
            Pair(false, "Invalid email format. Please enter a valid email, e.g., person@example.com.")
        }
    }

    private fun validatePhoneNumber(phoneNumber: String): Pair<Boolean, String> {
        val phonePattern = "^[0-9]{10}$"
        return if (phoneNumber.matches(phonePattern.toRegex())) {
            Pair(true, "Valid phone number format.")
        } else {
            Pair(false, "Invalid phone number format. Please enter a 10-digit number without any spaces or symbols.")
        }
    }

    private fun validatePassword(password: String): Pair<Boolean, String> {
        val passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$"
        return if (password.matches(passwordPattern.toRegex())) {
            Pair(true, "Valid password format.")
        } else {
            Pair(
                false,
                "Invalid password format."
            )

        }
    }
}
