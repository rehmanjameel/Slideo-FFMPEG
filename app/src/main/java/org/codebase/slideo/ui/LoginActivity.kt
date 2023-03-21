package org.codebase.slideo.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.view.*
import org.codebase.slideo.MainActivity
import org.codebase.slideo.R
import org.codebase.slideo.utils.App
import org.codebase.slideo.viewmodel.SplashScreenViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        window.statusBarColor = resources.getColor(R.color.top_bar)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        loginButtonId.setOnClickListener {
//            login()
        }

        openSignUpPage.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        backArrow.setOnClickListener {
            onBackPressed()
        }
    }

    fun validateLoginFields() {
        val loginEmail = loginEmailId.text.toString()
        val loginPassword = loginPasswordId.text.toString().trim()

        if (loginEmail.isEmpty() && loginPassword.isEmpty()) {
            loginEmailId.error = "Email is required"
            loginPasswordId.error = "Email is required"
        } else if (loginEmail.isEmpty()) {
            loginEmailId.error = "Email is required"
        } else if (loginPassword.isEmpty()) {
            loginPasswordId.error = "Password is required"
        } else {
            login(email = loginEmail, password = loginPassword)
        }

    }
    private fun login(email: String, password: String) {
        App.saveLogin(true)

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser

                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)

            } else {
                Toast.makeText(baseContext, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {

        }
    }
}