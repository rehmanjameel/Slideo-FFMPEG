package org.codebase.slideo.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_profile.*
import org.codebase.slideo.MainActivity
import org.codebase.slideo.R
import org.codebase.slideo.utils.App

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        actionBar?.hide()

        // get user info
        val rootRef = FirebaseDatabase.getInstance().reference
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val uidRef = rootRef.child("slideo").child(uid)

        uidRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapShot = task.result
                val imageUrl = snapShot.child("profileImageUri").value
                val name = snapShot.child("userName").value.toString()
                val email = snapShot.child("email").value.toString()
                val userGender = snapShot.child("gender").value.toString()
                Log.e("uri", imageUrl.toString())

                App.saveString("profile_image", imageUrl.toString())
                App.saveString("user_name", name)
                App.saveString("user_email", email)
                App.saveString("gender", userGender)

                Log.e("name of user", App.getString("user_name"))
                Log.e("name of user", App.getString("profile_image"))

            }
        }

        backArrow.setOnClickListener {
            onBackPressed()
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
        }

        logout.setOnClickListener {

//            Firebase.auth.signOut()
            App.saveLogin(false)
            App.removeKey("user_name")
            App.removeKey("user_email")
            App.removeKey("gender")
            onBackPressed()

//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        Glide.with(applicationContext)
            .load(App.getString("profile_image"))
            .placeholder(R.drawable.ic_baseline_person_24)
            .error(R.drawable.ic_baseline_person_24)
            .into(profileImage)

        userProfileName.text = App.getString("user_name")
        userEmail.text = App.getString("user_email")
        gender.text = App.getString("gender")
    }
}