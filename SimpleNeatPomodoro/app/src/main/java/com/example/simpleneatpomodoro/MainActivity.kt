package com.example.simpleneatpomodoro

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var currentDuration: Int = 15
    private var roundCount: Int = 1
    private var currentCount: Int = 1
    private lateinit var durationButtons: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        durationButtons = listOf(
            R.id.button_layout_duration_15min, R.id.button_layout_duration_30min,
            R.id.button_layout_duration_60min, R.id.button_layout_duration_custom
        ).map {
            findViewById(it)
        }

        durationButtons.forEach { button ->
            button.setOnClickListener {
                when(button.id) {
                    R.id.button_layout_duration_15min ->
                        setCurrentTimer(15, 0)
                    R.id.button_layout_duration_30min ->
                        setCurrentTimer(30, 1)
                    R.id.button_layout_duration_60min ->
                        setCurrentTimer(60, 2)
                    else -> {
                        setCurrentTimer(60, 3) // TEMP
                    }
                }
            }
        }

        findViewById<Button>(R.id.start_button)
            .setOnClickListener { startTimer() }

        listOf<Button>(
            findViewById(R.id.repeat_count_plus),
            findViewById(R.id.repeat_count_minus)
        ).forEach { button ->
            button.setOnClickListener {
                setCurrentRound(if (it.id == R.id.repeat_count_plus) { roundCount + 1 } else { roundCount - 1 })
            }
        }

        setCurrentTimer(currentDuration, 0)
        setCurrentRound(roundCount)
    }

    private fun setCurrentTimer(minute: Int, index: Int) {
        this.currentDuration = minute

        setCurrentTimeText(minute)

        (0..<durationButtons.size).forEach { i ->
            val backgroundColor = if (i == index) { Color.BLUE } else { Color.WHITE }
            val textColor = if (i == index) { Color.WHITE } else { Color.BLACK }

            durationButtons[i]
                .setBackgroundColor(backgroundColor)
            durationButtons[i]
                .setTextColor(textColor)
        }
    }

    private fun setCurrentTimeText(minute: Int) {
        val currentTime = "$minute:00"
        findViewById<TextView>(R.id.current_time).text = currentTime
    }

    private fun setCurrentRound(round: Int) {
        if (round < 1) {
            return
        }

        roundCount = round
        val round = "$round"
        findViewById<TextView>(R.id.repeat_count_current_text).text = round

        setCurrentCount(1)
    }

    private fun setCurrentCount(count: Int) {
        currentCount = count
        val subTitle = "Round $count of $roundCount"
        findViewById<TextView>(R.id.sub_title).text = subTitle
    }

    private fun startTimer() {
        // TEMP: Test Code added
        var second = (60 * 1000 * currentDuration).toLong()
        if (currentDuration == 15) {
            second = 3000
        }

        val progressView = findViewById<CircularProgressView>(R.id.circular_progress_view)

        // 테스트를 위해 애니메이션으로 progress 값을 0에서 1로 5초 동안 변경
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = second // 5초
        animator.addUpdateListener { animation ->
            val progressValue = animation.animatedValue as Float
            // progress 값을 업데이트하면 커스텀 뷰가 자동으로 다시 그려짐
            progressView.progress = progressValue

            if (progressValue == 1.0f) {
                currentCount += 1

                if (currentCount > roundCount) {
                    setCurrentCount(1)
                    progressView.progress = 0f
                    return@addUpdateListener
                } else {
                    setCurrentCount(currentCount)
                    startTimer()
                }
            }
        }

        animator.start()
    }
}