package org.codebase.slideo

import android.Manifest.permission.*
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.model.MediaFile
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import com.simform.videooperations.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.drawer_header.*
import org.codebase.slideo.ui.AudioActivity
import org.codebase.slideo.ui.LoginActivity
import org.codebase.slideo.ui.ProfileActivity
import org.codebase.slideo.ui.VideosActivity
import org.codebase.slideo.utils.App
import org.codebase.slideo.videoProcessActivity.CombineImages
import org.codebase.slideo.viewmodel.SplashScreenViewModel
import java.io.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var toolbar: androidx.appcompat.widget.Toolbar? = null
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    var mediaFiles: List<MiMedia>? = null
    private var isImageSelected: Boolean = false
    private val ffmpegQueryExtension = FFmpegQueryExtension()

    private lateinit var auth: FirebaseAuth
    lateinit var textView: TextView
    lateinit var hImageView: CircleImageView

    var count = 0
    private val splashViewModel = SplashScreenViewModel()

    companion object {
        lateinit var context: Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //installSplashScreen function must be before the 'super' method
        installSplashScreen().apply {
            setKeepOnScreenCondition{
                splashViewModel.isLoading.value
            }
        }
        setContentView(R.layout.activity_main)

        context = this
        auth = Firebase.auth

        setSupportActionBar(my_awesome_toolbar)

        checkPermissions()

        actionBarDrawerToggle = ActionBarDrawerToggle(this, myDrawerLayoutId,
            R.string.nav_open, R.string.nav_close)

        myDrawerLayoutId.addDrawerListener(actionBarDrawerToggle)

        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navMenuId.setNavigationItemSelectedListener(this)

        val hView = navMenuId.getHeaderView(0)



        hImageView = hView.findViewById(R.id.headerImage)
        textView = hView.findViewById(R.id.headerProfileNameId)

        createVideoCardId.setOnClickListener {
            Log.e("in permissions yes", checkPermissions().toString())

            Common.selectFile(this, maxSelection = 6, isImageSelection = true, isAudioSelection = true)
            if (checkPermissions()) {
                Log.e("in permissions true", checkPermissions().toString())
                //Select images from gallery to make video
            } else {
                Log.e("in permissions", checkPermissions().toString())
                checkPermissions()
            }
        }

        myVideosCardId.setOnClickListener {
            Log.e("clicking ob button", "$it")
            val intent = Intent(this, VideosActivity::class.java)
            startActivity(intent)
        }

//        buttonAudioId.visibility = View.VISIBLE
//        buttonAudioId.setOnClickListener {
//            startActivity(Intent(this, CombineImages::class.java))
//        }

        // get user info
        val rootRef = FirebaseDatabase.getInstance().reference
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val uidRef = rootRef.child("slideo").child(uid).child("videos")
            com.google.android.exoplayer2.util.Log.e("time child", uidRef.toString())
            uidRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.e(
                        "videos of firebase",
                        snapshot.child("videos").value.toString()
                    )
                    for (d in snapshot.children) {
                        count++
                        Log.e("videos of furebase ${count}", d.value.toString() + d.key)

                        Log.e(
                            "videos of firebase",
                            d.child("videos").value.toString()
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
                    Log.e("cancelled", "error")
                }

            })

            uidRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.e("childListener", "${snapshot.child("videoUri").value}")
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            mediaFiles = data.parcelableArrayList(KeyUtils.SELECTED_MEDIA)
            Log.e("mdeia files ", "$data ${mediaFiles.toString()}")
            selectedFiles(mediaFiles, requestCode)
        }
    }

//    val activityResultLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val intent = result.data
//            if (intent != null) {
//                mediaFiles = intent.parcelableArrayList(FilePickerActivity.MEDIA_FILES)
//                (this as FileSelection).selectedFiles(mediaFiles, result.resultCode)
//            }
//        }
//    }

    private inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
        SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
    }

    fun selectedFiles(mediaFiles: List<MiMedia>?, requestCode: Int) {
        when (requestCode) {
            Common.IMAGE_FILE_REQUEST_CODE -> {
                if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                    val size: Int = mediaFiles.size
                    tvInputPathImage.text = "$size" + (if (size == 1) " Image " else " Images ") + "selected"
                    isImageSelected = true
                    processStart()
                    combineImagesProcess()
                } else {
                    Toast.makeText(this, "getString(R.string.image_not_selected_toast_message)", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.createVideoId) {
            val intent = Intent(this, CombineImages::class.java)
            startActivity(intent)
        } else if (item.itemId == R.id.myVideosId) {
            val intent = Intent(this, VideosActivity::class.java)
            startActivity(intent)
        }
        else if (item.itemId == R.id.profileId) {
//            val currentUser = auth.currentUser
            if (App.isLoggedIn()) {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        return true
    }

    private fun processStop() {
//        btnImagePath.isEnabled = true
//        btnCombine.isEnabled = true
        mProgressView.visibility = View.GONE
    }

    private fun processStart() {
//        btnImagePath.isEnabled = false
//        btnCombine.isEnabled = false
        mProgressView.visibility = View.VISIBLE
    }

    private fun combineImagesProcess() {
        val outputPath = Common.getInternalPath(this, Common.VIDEO)
        val pathsList = ArrayList<Paths>()
        mediaFiles?.let {
            for (element in it) {
                val paths = Paths()
                paths.filePath = element.path.toString()
                paths.isImageFile = true
                pathsList.add(paths)
            }

            val query = ffmpegQueryExtension.combineImagesAndVideos(pathsList, 720, 1080,
                3.toString(), outputPath)

            CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
                override fun process(logMessage: LogMessage) {
//                    tvInputPathImage.text = logMessage.text
                }

                override fun success() {
//                    tvInputPathImage.text = String.format(getString(R.string.output_path), outputPath)
                    Log.e("success", "video created successfully on $outputPath")
//                    saveVideoToInternalStorage("outputPath")
//                    Common.writeFileOnInternalStorage(context, Common.VIDEO, outputPath)
                    val intent = Intent(this@MainActivity, CombineImages::class.java)
                    intent.putExtra("video_path", outputPath)
                    App.saveString("video_output_path", outputPath)
                    startActivity(intent)
                    processStop()
                }

                override fun cancel() {
                    processStop()
                }

                override fun failed() {
                    processStop()
                }
            })
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        myDrawerLayoutId.openDrawer(navMenuId)
        return true
    }

    fun saveVideoToInternalStorage(filePath: String) {
        val newfile: File
        try {
            val currentFile = File(filePath)
            val fileName = currentFile.name
            val cw = ContextWrapper(this)
            val directory: File = cw.getDir("videoDir", Context.MODE_PRIVATE)
            newfile = File(directory, fileName)
            if (currentFile.exists()) {
                val `in`: InputStream = FileInputStream(currentFile)
                val out: OutputStream = FileOutputStream(newfile)

                // Copy the bits from instream to outstream
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                Log.e("", "Video file saved on $out.")

                `in`.close()
                out.close()
                Log.e("", "Video file saved successfully.")
            } else {
                Log.e("", "Video saving failed. Source file missing.")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //     override the onBackPressed() function to close the Drawer when the back button is clicked
    override fun onBackPressed() {
        if (this.myDrawerLayoutId.isDrawerOpen(GravityCompat.START)) {
            this.myDrawerLayoutId.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /*private fun checkPermissions(): Boolean {
        val permissionList = mutableListOf<String>()

        if (SDK_INT >= Build.VERSION_CODES.R) {
            // On Android 11 and higher, use MediaStore API to access media files
            if (ContextCompat.checkSelfPermission(
                    applicationContext, MANAGE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.add(MANAGE_EXTERNAL_STORAGE)
            }
        } else {
            // On Android 10 and lower, use legacy storage access framework
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.add(READ_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.add(WRITE_EXTERNAL_STORAGE)
            }
        }

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionList.add(CAMERA)
        }

        if (permissionList.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionList.toTypedArray(),
                1
            )
            return false
        }

        return true
    }*/


    /*@RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            val permissionMap: MutableMap<String, Int> = mutableMapOf()

            if (SDK_INT >= Build.VERSION_CODES.R) {
                permissionMap[MANAGE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
            } else {
                permissionMap[READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                permissionMap[WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
            }

            permissionMap[CAMERA] = PackageManager.PERMISSION_GRANTED

            if (grantResults.isNotEmpty()) {
                for (i in permissions.indices) {
                    permissionMap[permissions[i]] = grantResults[i]
                }

                if (permissionMap.all { it.value == PackageManager.PERMISSION_GRANTED }) {
                    // All permissions are granted
                    Log.d("TAG", "All permissions granted")
                } else {
                    // Some permissions are not granted
                    Log.d("TAG", "Some permissions not granted ask again")

                    val deniedPermissions = permissionMap.filter { it.value != PackageManager.PERMISSION_GRANTED }.keys
                    val rationalePermissions = mutableListOf<String>()

                    for (permission in deniedPermissions) {
                        if (shouldShowRequestPermissionRationale(permission)) {
                            rationalePermissions.add(permission)
                        }
                    }

                    if (rationalePermissions.isNotEmpty()) {
                        showDialogOK("Storage and camera permissions are required") { _, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> checkPermissions()
                                DialogInterface.BUTTON_NEGATIVE -> {
                                    // User cancelled the dialog
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }*/


    private fun checkPermissions() : Boolean{
        //Ask for permissions
        var externalStorageReadPermission: Int = ContextCompat.checkSelfPermission(
            applicationContext, READ_EXTERNAL_STORAGE)
        var videoPerm: Int = 0

        if (SDK_INT > 32) {
            externalStorageReadPermission = ContextCompat.checkSelfPermission(
                applicationContext, MANAGE_EXTERNAL_STORAGE
            )
            videoPerm = ContextCompat.checkSelfPermission(
                applicationContext, READ_MEDIA_VIDEO)
        }

        val externalStorageWritePermission: Int = ContextCompat.checkSelfPermission(
            applicationContext, WRITE_EXTERNAL_STORAGE)
        val cameraPermission: Int = ContextCompat.checkSelfPermission(
            applicationContext, CAMERA)
        val listPermissionNeeded: ArrayList<String> = ArrayList()

        if (externalStorageReadPermission != PackageManager.PERMISSION_GRANTED) {
            if (SDK_INT > 32) {
                listPermissionNeeded.add(READ_MEDIA_IMAGES)
            } else
                listPermissionNeeded.add(READ_EXTERNAL_STORAGE)
        }
        if (videoPerm != PackageManager.PERMISSION_GRANTED) {
            if (SDK_INT > 32) {
                listPermissionNeeded.add(READ_MEDIA_VIDEO)
            }

        }
        if (externalStorageWritePermission != PackageManager.PERMISSION_GRANTED) {
            if (SDK_INT < 31)
                listPermissionNeeded.add(WRITE_EXTERNAL_STORAGE)
        }
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionNeeded.add(CAMERA)
        }

        if (listPermissionNeeded.isNotEmpty()) {
            Log.e("shut the fuck up", listPermissionNeeded.toString())
            ActivityCompat.requestPermissions(this, listPermissionNeeded.toTypedArray(), 1)
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1 -> {
                val permissionMap : HashMap<String, Int> = HashMap()

                // Initialize the map with permissions
                if (SDK_INT > 32) {
                    permissionMap[READ_MEDIA_IMAGES] = PackageManager.PERMISSION_GRANTED
                    permissionMap[READ_MEDIA_VIDEO] = PackageManager.PERMISSION_GRANTED
                } else
                    permissionMap[READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED

                permissionMap[WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                permissionMap[CAMERA] = PackageManager.PERMISSION_GRANTED

                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices) {
                        permissionMap[permissions[i]] = grantResults[i]
                    }
                    if (permissionMap[READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED ||
                        (permissionMap[READ_MEDIA_IMAGES] == PackageManager.PERMISSION_GRANTED &&
                        permissionMap[READ_MEDIA_VIDEO] == PackageManager.PERMISSION_GRANTED) &&
                        permissionMap[WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED &&
                            permissionMap[CAMERA] == PackageManager.PERMISSION_GRANTED) {
//                        isGranted = true

//                        pickVideo()
                        Log.d("Permissions", "All permissions granted")
                    } else {
//                        isGranted = false
                        Log.d("Permissions", "Some permissions not granted ask again")

                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this, READ_MEDIA_IMAGES) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this, READ_MEDIA_VIDEO) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA)) {
                            showDialogOK("Storage Permission required for this app") { dialog, which ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE -> checkPermissions()
                                    DialogInterface.BUTTON_NEGATIVE -> {
                                        dialog.dismiss()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this, "Go to settings and enable permissions",
                                Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    //Dialog for permissions if permission not granted
    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        MaterialAlertDialogBuilder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", okListener)
            .create()
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (App.isLoggedIn()) {
            textView.text = App.getString("user_name")

            Glide.with(applicationContext)
                .load(App.getString("profile_image"))
                .placeholder(R.drawable.ic_baseline_person_24)
                .error(R.drawable.ic_baseline_person_24)
                .into(hImageView)
        } else {
            textView.text = getString(R.string.app_name)

            Glide.with(applicationContext)
                .load(R.drawable.slideo_logo)
                .placeholder(R.drawable.ic_baseline_person_24)
                .error(R.drawable.ic_baseline_person_24)
                .into(hImageView)
        }
    }
}