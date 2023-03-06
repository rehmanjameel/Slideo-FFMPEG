package org.codebase.slideo

import android.Manifest.permission.*
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.model.MediaFile
import com.simform.videooperations.*
import kotlinx.android.synthetic.main.activity_main.*
import org.codebase.slideo.ui.AudioActivity
import org.codebase.slideo.ui.VideosActivity
import org.codebase.slideo.utils.App
import org.codebase.slideo.videoProcessActivity.CombineImages
import org.codebase.slideo.viewmodel.SplashScreenViewModel
import java.io.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, FileSelection {

    var toolbar: androidx.appcompat.widget.Toolbar? = null
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    var mediaFiles: List<MediaFile>? = null
    private var isImageSelected: Boolean = false
    private val ffmpegQueryExtension = FFmpegQueryExtension()

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
        setSupportActionBar(my_awesome_toolbar)

        checkPermissions()

        actionBarDrawerToggle = ActionBarDrawerToggle(this, myDrawerLayoutId,
            R.string.nav_open, R.string.nav_close)

        myDrawerLayoutId.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navMenuId.setNavigationItemSelectedListener(this)

        createVideoCardId.setOnClickListener {
            if (checkPermissions()) {
                //Select images from gallery to make video
                Common.selectFile(this, maxSelection = 6, isImageSelection = true, isAudioSelection = true)
            } else {
                checkPermissions()
            }
        }

        myVideosCardId.setOnClickListener {
            Log.e("clicking ob button", "$it")
            val intent = Intent(this, VideosActivity::class.java)
            startActivity(intent)
        }

        buttonAudioId.visibility = View.VISIBLE
        buttonAudioId.setOnClickListener {
            startActivity(Intent(this, AudioActivity::class.java))
        }

    }

//    fun replaceFragment(fragment: Fragment) {
//        val fragmentManager = supportFragmentManager
//        val fragmentTransaction = fragmentManager.beginTransaction()
//        fragmentTransaction.replace(R.id.frame_content, fragment)
//        fragmentTransaction.addToBackStack(fragment.toString())
//        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//        fragmentTransaction.commit()
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            mediaFiles = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES)
            Log.e("mdeia files ", mediaFiles.toString())
            (this as FileSelection).selectedFiles(mediaFiles,requestCode)
        }
    }

    override fun selectedFiles(mediaFiles: List<MediaFile>?, requestCode: Int) {
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
            Toast.makeText(this, "Create your videos here", Toast.LENGTH_SHORT).show()
        } else if (item.itemId == R.id.myVideosId) {
            Toast.makeText(this, "Your saved videos are here", Toast.LENGTH_SHORT).show()
        } else if (item.itemId == R.id.loginId) {
            Toast.makeText(this, "Login to account", Toast.LENGTH_SHORT).show()
        } else if (item.itemId == R.id.nav_logout) {
            Toast.makeText(this, "You are logged out", Toast.LENGTH_SHORT).show()
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

    fun combineImagesProcess() {
        val outputPath = Common.getInternalPath(this, Common.VIDEO)
        val pathsList = ArrayList<Paths>()
        mediaFiles?.let {
            for (element in it) {
                val paths = Paths()
                paths.filePath = element.path
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

    private fun checkPermissions() : Boolean{
        //Ask for permissions
        val externalStorageReadPermission: Int = ContextCompat.checkSelfPermission(
            applicationContext, READ_EXTERNAL_STORAGE)
        val externalStorageWritePermission: Int = ContextCompat.checkSelfPermission(
            applicationContext, READ_EXTERNAL_STORAGE)
        val cameraPermission: Int = ContextCompat.checkSelfPermission(
            applicationContext, CAMERA)
        val listPermissionNeeded: ArrayList<String> = ArrayList()

        if (externalStorageReadPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionNeeded.add(READ_EXTERNAL_STORAGE)
        }
        if (externalStorageWritePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionNeeded.add(WRITE_EXTERNAL_STORAGE)
        }
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionNeeded.add(CAMERA)
        }

        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionNeeded.toTypedArray(), 1)
        } else {
//            isGranted = true
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
                permissionMap[READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                permissionMap[WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                permissionMap[CAMERA] = PackageManager.PERMISSION_GRANTED

                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices) {
                        permissionMap[permissions[i]] = grantResults[i]
                    }
                    if (permissionMap[READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED &&
                        permissionMap[WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED &&
                            permissionMap[CAMERA] == PackageManager.PERMISSION_GRANTED) {
//                        isGranted = true

//                        pickVideo()
                        Log.d("Permissions", "All permissions granted")
                    } else {
//                        isGranted = false
                        Log.d("Permissions", "Some permissions not granted ask again")

                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE) ||
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
}