package com.example.moviceapp

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment

val Fragment.screenWidth: Float
    @RequiresApi(Build.VERSION_CODES.R)
    get() {
        return requireActivity().screenWidth
    }

val Activity.screenWidth: Float
    @RequiresApi(Build.VERSION_CODES.R)
    get() = windowManager.currentWindowMetrics.bounds.width() / resources.displayMetrics.density