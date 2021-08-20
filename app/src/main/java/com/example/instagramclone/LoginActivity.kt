package com.example.instagramclone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


private const val TAG = "LoginActivity"

class LoginActivity : AppCompatActivity() {

    private lateinit var loginButton: Button
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText

    // private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButton = findViewById(R.id.loginButton)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)

        //Checking if user is already signed in then we can continue that session
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            goPostsActivity()
        }

        //binding = ActivityLoginBinding.inflate(layoutInflater)
        //val view = binding.root
        //setContentView(view)

        /* If login button has been clicked then we will check for valid email/password entries by user,
           and then do firebase authentication check */
        loginButton.setOnClickListener {
            loginButton.isEnabled = false
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {

                Toast.makeText(this, "Email/password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase authentication check
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                loginButton.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                    goPostsActivity()
                } else {
                    Log.e(TAG, "signInWithEmail Failed", task.exception)
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun goPostsActivity() {
        Log.i(TAG, "goPostsActivity")

        val intent = Intent(this, PostsActivity::class.java)
        startActivity(intent)
        finish()
    }

}