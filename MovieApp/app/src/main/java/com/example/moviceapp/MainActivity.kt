package com.example.moviceapp

import android.os.Bundle
import android.view.Menu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.moviceapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // WindowInsets 처리 - 시스템 바와 겹치지 않도록 패딩 추가
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // AppBarLayout 상단에 패딩 추가
            binding.appBarLayout.setPadding(
                insets.left,
                insets.top,
                insets.right,
                0
            )

            // BottomNavigationView 하단에 패딩 추가
            binding.navigationView.setPadding(
                insets.left,
                0,
                insets.right,
                insets.bottom
            )

            WindowInsetsCompat.CONSUMED
        }

        setSupportActionBar(binding.topAppBar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.main_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.navigationView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return true
    }
}