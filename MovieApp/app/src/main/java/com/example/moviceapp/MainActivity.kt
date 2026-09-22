package com.example.moviceapp

import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.moviceapp.book.BookViewModel
import com.example.moviceapp.databinding.ActivityMainBinding
import com.example.moviceapp.myinfo.AccountSettingViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var screenAttributes: ScreenAttribute
    private val viewModel: BookViewModel by viewModels()
    private val accountViewModel: AccountSettingViewModel by viewModels()
    private val appViewModel: AppViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountViewModel.getUserMe()
        val displayMetrics = resources.displayMetrics
        screenAttributes.screenWidth = displayMetrics.widthPixels
        screenAttributes.screenHeight = displayMetrics.heightPixels
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ticketFloatingActionButton.setOnClickListener {
            val booking = viewModel.bookings.value.firstOrNull()

            if (booking != null) {
                val modal = TicketBottomSheet.newInstance(booking)
                modal.show(supportFragmentManager, TicketBottomSheet.TAG)
            } else {
                Toast.makeText(
                    this,
                    "No booking found",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // WindowInsets 처리 - 시스템 바와 겹치지 않도록 패딩 추가
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { _, windowInsets ->
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
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.searchFragment, R.id.bookFragment, R.id.myInfoFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)

        lifecycleScope.launch {
            viewModel.fetchBookings()
        }

        // Handle loading and error states from AppViewModel
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appViewModel.isLoading.collect { isLoading ->
                    binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appViewModel.error.collect { message ->
                    com.google.android.material.snackbar.Snackbar.make(
                        binding.root,
                        message,
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }

        lifecycleScope.launch {
            appViewModel.visibilityFloatingActionButton.collect {
                binding.ticketFloatingActionButton.visibility = it
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            if (it.action == MotionEvent.ACTION_DOWN) {
                val v = currentFocus
                if (v is TextInputEditText) {
                    val outRect = Rect()
                    v.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        v.clearFocus()
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}