package com.example.twoeyes.ui.camera

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.twoeyes.R

class CameraActivity : AppCompatActivity() {
    private lateinit var backButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_camera)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_camera)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backButton = findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.slide_down)
    }
}