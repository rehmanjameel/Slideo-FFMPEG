package org.codebase.slideo.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.android.synthetic.main.activity_login.*
import org.codebase.slideo.MainActivity
import org.codebase.slideo.R
import org.codebase.slideo.viewmodel.SplashScreenViewModel

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButtonId.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}