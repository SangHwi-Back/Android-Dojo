package com.example.simpleneatpomodoro

import android.animation.ValueAnimator
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.start_button).setOnClickListener {
            val progressView = findViewById<CircularProgressView>(R.id.circular_progress_view)

            // 테스트를 위해 애니메이션으로 progress 값을 0에서 1로 5초 동안 변경
            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = 5000 // 5초
            animator.addUpdateListener { animation ->
                val progressValue = animation.animatedValue as Float
                // progress 값을 업데이트하면 커스텀 뷰가 자동으로 다시 그려짐
                progressView.progress = progressValue
            }
            animator.start()
        }
    }
}