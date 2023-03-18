package org.codebase.slideo.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.android.synthetic.main.activity_login.*
import org.codebase.slideo.MainActivity
import org.codebase.slideo.R
import org.codebase.slideo.viewmodel.SplashScreenViewModel

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        window.statusBarColor = resources.getColor(R.color.top_bar)
        setContentView(R.layout.activity_login)

        loginButtonId.setOnClickListener {
        }

        openSignUpPage.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        backArrow.setOnClickListener {
            onBackPressed()
        }
    }
}