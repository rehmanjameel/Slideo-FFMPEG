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
import android.view.View
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.PermissionChecker
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.codebase.slideo.R
import org.codebase.slideo.models.UserModel
import org.codebase.slideo.models.VideosModel
import org.codebase.slideo.utils.App
import java.util.*
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {

    var firebaseAuth = FirebaseAuth.getInstance()
    var imageUri: Uri? = null

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButtons: RadioButton
    private lateinit var radioButtonText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.statusBarColor = resources.getColor(R.color.top_bar)

        setContentView(R.layout.activity_sign_up)

        supportActionBar?.hide()

        // initialize the firebaseAuth variable
        firebaseAuth = FirebaseAuth.getInstance()

        signUpButton.setOnClickListener {
            checkValidations()
        }

        selectImage.setOnClickListener { pickImage() }

        backArrow.setOnClickListener {
            onBackPressed()
        }

        openLoginPage.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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

    //On Permission granted this override function will be called
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1001) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Permission was granted
                pickImage()
            }
            else{
                // Permission was denied
                showAlert("Image permission was denied. Unable to pick image.");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showAlert(message: String) {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setPositiveButton("Ok") {_, _ ->
            builder.create().dismiss()
        }
        builder.setTitle("Permission!")
        builder.setMessage(message)
        builder.create().show()
    }

    private fun getRadioButtonText() {
        //Getting radio button text on the base of radio button id
        val radioButtonId: Int = genderRadioGroup.checkedRadioButtonId
        if (radioButtonId != -1) {
            radioButtons = findViewById(radioButtonId)
        }
        radioButtonText = radioButtons.text.toString()
    }

    private fun checkValidations() {
        getRadioButtonText()

        val userName = signUpUserName.text.toString()
        val userEmail = signUpEmail.text.toString()
        val userPassword = signUpPassword.text.toString()
        val confirmPassword = signUpConfirmPassword.text.toString()

        if (userName.isEmpty() && userEmail.isEmpty() && userPassword.isEmpty() && confirmPassword.isEmpty()) {
            signUpUserName.error = "Field required"
            signUpEmail.error = "Field required"
            signUpPassword.error = "Field required"
            signUpConfirmPassword.error = "Field required"
        } else if (userName.isEmpty() || !isValidFirstLastName(userName)) {
            signUpUserName.error = "Field required or invalid data"
        } else if (userEmail.isEmpty() || !isValidMail(userEmail)) {
            signUpEmail.error = "Field required or invalid email"
        } else if (userPassword.isEmpty() || !isValidPasswordFormat(userPassword)) {
            signUpPassword.error = "Field required or password is weak"
        } else if (confirmPassword.isEmpty() || confirmPassword != userPassword) {
            signUpConfirmPassword.error = "Field required or password not matched"
        } else {
            mProgressView.visibility = View.VISIBLE

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(userEmail, confirmPassword)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    //else if successful
                    Log.d("Main", "Successfully created user with: ${it.result.user?.uid}")
//                SavedPreference.setEmail(this, it.result.user?.email.toString())
//                SavedPreference.setUsername(this, it.result.user.toString())
                    uploadImageToFirebaseStorage(userName, userEmail, radioButtonText)
                }
                .addOnFailureListener {e->
                    mProgressView.visibility = View.GONE
                    Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("Failure","${e.message}")
                }
        }
    }

    private fun uploadImageToFirebaseStorage(userName: String, email: String, gender:String) {
        if (imageUri == null) return
        val fileName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("slideo_profile/$fileName")
        ref.putFile(imageUri!!)
            .addOnSuccessListener {
                Log.d("Image Storage", "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {imageUri ->
                    Log.d("ImageUri", "File Location: $imageUri")

                    App.saveString("ProfileImageUrl", imageUri.toString())
                    saveUserToFireBaseDatabase(userName, imageUri.toString(), email, gender)
                    mProgressView.visibility = View.GONE

                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {e ->
                    mProgressView.visibility = View.GONE

                    Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                mProgressView.visibility = View.GONE
                Log.d("ImageUri", "File Location failed")
            }
    }

    private fun saveUserToFireBaseDatabase(userName: String, profileImageUri: String,
    email: String, gender:String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        App.saveString("UID", uid)

        val userData = UserModel(userName = userName, profileImageUri = profileImageUri, email = email,
            gender = gender)

        val ref = FirebaseDatabase.getInstance().getReference("slideo/${FirebaseAuth.getInstance().currentUser!!.uid}")
        ref.setValue(userData).addOnCompleteListener{ valuesSent ->
                if (valuesSent.isSuccessful) {
                    mProgressView.visibility = View.GONE
                    Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show()
                } else {
                    mProgressView.visibility = View.GONE
                    Toast.makeText(this, "Registration failed ${valuesSent.exception!!.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }

        val userVideos = VideosModel(videoUri = "")
        val vidRef = ref.child("videos")
        vidRef.setValue(userVideos)
        Log.e("login success", vidRef.toString())


//        ref.setValue(userMessages)
//            .addOnSuccessListener {
//                Log.d("Data to FBDB", "Successfully added")
//            }
    }

    private fun isValidMail(email: String): Boolean {
        val EMAIL_STRING =
            ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                    "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        return Pattern.compile(EMAIL_STRING).matcher(email).matches()
    }

    fun isValidFirstLastName(userName: String): Boolean {
        val USER_NAME =
            Pattern.compile(
                "^" +
                        //                    "(?=.*[0-9])" +         //at least 1 digit
                        // "(?=.*[a-z])" +         //at least 1 lower case letter
                        // "(?=.*[A-Z])" +         //at least 1 upper case letter
                        "(?=.*[a-zA-Z0-9])" + // any letter
                        // "(?=.*[@#$%^&+=])" +    //at least 1 special character
                        //                    "(?=\\S+$)" +           //no white spaces
                        ".{4,}" + // at least 6 characters
                        "$"
            )
        return USER_NAME.matcher(userName).matches()
    }

    fun isValidPasswordFormat(password: String): Boolean {
        val passwordREGEX =
            Pattern.compile(
                "^" +
                        "(?=.*[0-9])" + // at least 1 digit
                        //                "(?=.*[a-z])" +         //at least 1 lower case letter
                        //                "(?=.*[A-Z])" +         //at least 1 upper case letter
                        "(?=.*[a-zA-Z])" + // any letter
                        "(?=.*[@#$%^&+=])" + // at least 1 special character
                        "(?=\\S+$)" + // no white spaces
                        ".{6,}" + // at least 6 characters
                        "$"
            )
        return passwordREGEX.matcher(password).matches()
    }

}