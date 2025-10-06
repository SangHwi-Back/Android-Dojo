package com.example.simpleneatpomodoro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val DEFAULT_STROKE_WIDTH = 3

    // Border
    private val backgroundPaint: Paint = Paint()
    private val progressPaint: Paint = Paint()

    // Background
    private val oval = RectF()

    // Border width
    private var strokeWidthValue = 10f

    // Progress (0.0f ~ 1.0f)
    var progress: Float = 0.0f
        set(value) {
            field = min(max(value, 0.0f), 1f)
            // Draw again!
            invalidate()
        }

    init {
        // Load attrs.xml
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressView, defStyleAttr, 0)
        // Set Border Width
        val defaultStrokeWidth = DEFAULT_STROKE_WIDTH * resources.displayMetrics.density
        strokeWidthValue = typedArray.getDimension(R.styleable.CircularProgressView_strokeWidth,
            defaultStrokeWidth)
        // Set Background Color
        val backgroundColor = typedArray.getColor(R.styleable.CircularProgressView_backgroundColor,
            Color.GRAY)
        // Set Tint Color
        val progressColor = typedArray.getColor(R.styleable.CircularProgressView_progressColor,
            Color.BLUE)

        typedArray.recycle()

        backgroundPaint.apply {
            color = backgroundColor
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthValue
            isAntiAlias = true
        }

        progressPaint.apply {
            color = progressColor
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthValue
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = strokeWidthValue / 2
        oval.set(padding, padding, width - padding, height - padding)

        canvas.drawOval(oval, backgroundPaint)

        val sweepAngle = 360 * progress
        canvas.drawArc(oval, -90f, sweepAngle, false, progressPaint)
    }
}