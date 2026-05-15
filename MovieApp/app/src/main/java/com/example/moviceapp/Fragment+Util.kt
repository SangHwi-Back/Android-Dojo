package com.example.moviceapp

import android.app.Activity
import androidx.fragment.app.Fragment

val Fragment.screenWidth: Float
    get() {
        return requireActivity().screenWidth
    }

val Activity.screenWidth: Float
    get() = windowManager.currentWindowMetrics.bounds.width() / resources.displayMetrics.density