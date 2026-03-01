package com.example.twoeyes

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.twoeyes.ui.camera.CameraActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var cameraFloatingButton: FloatingActionButton
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        cameraFloatingButton = findViewById(R.id.fab_camera)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        val menuView = bottomNavigationView.getChildAt(0) as ViewGroup
        val margin = (80 * resources.displayMetrics.density).toInt()
        for (i in 0 until menuView.childCount) {
            val item = menuView.getChildAt(i)
            val newParams = ViewGroup.MarginLayoutParams(item.layoutParams)
            newParams.marginStart = if (i == 0) margin else 0
            newParams.marginEnd = if (i == menuView.childCount - 1) margin else 0
            item.layoutParams = newParams
            item.requestLayout()
        }

        cameraFloatingButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, 0)
        }
    }
}