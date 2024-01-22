package org.codebase.slideo.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.codebase.slideo.R
import org.codebase.slideo.databinding.ActivityLoginBinding
import org.codebase.slideo.utils.App

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        window.statusBarColor = resources.getColor(R.color.top_bar)
        setContentView(view)

        auth = Firebase.auth

        binding.loginButtonId.setOnClickListener {
            validateLoginFields()
        }

        binding.openSignUpPage.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.backArrow.setOnClickListener {
            onBackPressed()
        }
    }

    private fun validateLoginFields() {
        val loginEmail = binding.loginEmailId.text.toString()
        val loginPassword = binding.loginPasswordId.text.toString().trim()

        if (loginEmail.isEmpty() && loginPassword.isEmpty()) {
            binding.loginEmailId.error = "Email is required"
            binding.loginPasswordId.error = "Email is required"
        } else if (loginEmail.isEmpty()) {
            binding.loginEmailId.error = "Email is required"
        } else if (loginPassword.isEmpty()) {
            binding.loginPasswordId.error = "Password is required"
        } else {
            binding.mProgressView.mProgress.visibility = View.VISIBLE
            login(email = loginEmail, password = loginPassword)
        }

    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                binding.mProgressView.mProgress.visibility = View.GONE

                Log.e("user login", "${user!!.uid} ${user.reload()}" )
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                finish()
                App.saveLogin(true)

            } else {
                binding.mProgressView.mProgress.visibility = View.GONE

                Toast.makeText(baseContext, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onStart() {
        super.onStart()
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            currentUser.reload()
//            val intent = Intent(this, ProfileActivity::class.java)
//            startActivity(intent)
//        }
    }
}