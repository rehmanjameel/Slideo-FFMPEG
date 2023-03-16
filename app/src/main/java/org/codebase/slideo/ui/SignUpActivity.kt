package org.codebase.slideo.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.PermissionChecker
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.codebase.slideo.R

class SignUpActivity : AppCompatActivity() {

    var firebaseAuth = FirebaseAuth.getInstance()
    var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.hide()

        // initialize the firebaseAuth variable
        firebaseAuth = FirebaseAuth.getInstance()

    }

    fun checkValidations() {
        val userName = signUpUserName.text.toString()
        val userEmail = signUpEmail.text.toString()
        val userPassword = signUpPassword.text.toString()
        val confirmPassword = signUpConfirmPassword.text.toString()

        if (userName.isEmpty() && userEmail.isEmpty() && userPassword.isEmpty() && confirmPassword.isEmpty()) {
            signUpUserName.error = "Field required"
            signUpEmail.error = "Field required"
            signUpPassword.error = "Field required"
            signUpConfirmPassword.error = "Field required"
        } else if (userName.isEmpty()) {
            signUpUserName.error = "Field required"
        } else if (userEmail.isEmpty()) {
            signUpEmail.error = "Field required"
        } else if (userPassword.isEmpty()) {
            signUpPassword.error = "Field required"
        } else if (confirmPassword.isEmpty()) {
            signUpConfirmPassword.error = "Field required"
        }
    }

    private var imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { imagePickerResult ->
        if (imagePickerResult.resultCode == Activity.RESULT_OK) {
            val intent= imagePickerResult.data

            if (intent != null) {
                imageUri = intent.data!!

                Glide.with(applicationContext)
                    .load(imageUri)
                    .error(R.drawable.ic_baseline_person_24)
                    .into(userSignUpImage)
            } else {
                Log.e("Error", "Image not set")
            }
        }
    }

    private fun pickImage() {
        Log.e("pick", "Image not set")

        //Check Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkSelfPermission(
                    applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PermissionChecker.PERMISSION_DENIED) {
                Log.e("if", "Image not set")

                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permission, 1001)
            } else {
                val  intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                imagePickerLauncher.launch(intent)
            }
        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }
    }
}