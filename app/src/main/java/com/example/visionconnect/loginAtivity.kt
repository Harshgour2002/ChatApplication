package com.example.visionconnect

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.visionconnect.databinding.ActivityLoginAtivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class loginAtivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginAtivityBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginAtivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()

        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.textRegister.setOnClickListener {
            startActivity(Intent(this, signUpActivity::class.java))
            finish()
        }

        binding.forgotPassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.forgot_password, null)
            val email = view.findViewById<EditText>(R.id.emailET)
            builder.setView(view)
            val dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

            val cancelBtn = view.findViewById<ImageButton>(R.id.cancelbtn)

            val sendBtn = view.findViewById<Button>(R.id.sendBtn)
            sendBtn.setOnClickListener {
                val Email = email.text.toString()
                if(Email.isNotEmpty()) {
                    auth.sendPasswordResetEmail(Email)
                        .addOnSuccessListener { task ->
                            Toast.makeText(this, "Email sent", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .addOnFailureListener { task ->
                            Toast.makeText(this, task.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                }
            }

            cancelBtn.setOnClickListener{
                dialog.dismiss()
            }
            dialog.show()
        }

        binding.loginBtn.setOnClickListener {
            if(check()) {
                val email = binding.loginEmail.text.toString()
                val password = binding.loginPassword.text.toString()
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Login successful
                            Toast.makeText(this, "Login success", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.putExtra("Email", email)
                            startActivity(intent)
                            finish()
                        }
                    }
                    .addOnFailureListener { task ->
                        Toast.makeText(this, task.localizedMessage, Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun check(): Boolean {
        return (binding.loginEmail.text.toString().trim().isNotEmpty() &&
                binding.loginPassword.text.toString().trim().isNotEmpty())
    }
}