package com.example.moviceapp.common

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment

val Fragment.screenWidth: Float
    get() = requireActivity().screenWidth

val Activity.screenWidth: Float
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.width().toFloat()
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels.toFloat()
        }
    }
