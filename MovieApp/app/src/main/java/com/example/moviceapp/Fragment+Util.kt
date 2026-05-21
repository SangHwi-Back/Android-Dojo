package com.example.moviceapp

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment

val Fragment.screenWidth: Float
    get() = requireActivity().screenWidth

val Activity.screenWidth: Float
    get() {
        if (Build.VERSION_CODES.R <= Build.VERSION.SDK_INT) {
            return windowManager.currentWindowMetrics.bounds.width().toFloat()
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.widthPixels.toFloat()
        }
    }