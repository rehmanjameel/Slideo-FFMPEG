package org.codebase.slideo

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import org.codebase.slideo.viewmodel.SplashScreenViewModel

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var toolbar: androidx.appcompat.widget.Toolbar? = null
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    private val splashViewModel = SplashScreenViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //installSplashScreen function must be before the 'super' method
        installSplashScreen().apply {
            setKeepOnScreenCondition{
                splashViewModel.isLoading.value
            }
        }
        setContentView(R.layout.activity_main)

        setSupportActionBar(my_awesome_toolbar)

        actionBarDrawerToggle = ActionBarDrawerToggle(this, myDrawerLayoutId,
            R.string.nav_open, R.string.nav_close)

        myDrawerLayoutId.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navMenuId.setNavigationItemSelectedListener(this)

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

    override fun onSupportNavigateUp(): Boolean {
//        myDrawerLayoutId.openDrawer(navMenuId)
        return true
    }

//     override the onBackPressed() function to close the Drawer when the back button is clicked
    override fun onBackPressed() {
        if (this.myDrawerLayoutId.isDrawerOpen(GravityCompat.START)) {
            this.myDrawerLayoutId.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}