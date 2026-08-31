package com.example.moviceapp

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenAttribute @Inject constructor() {
    var screenWidth: Int = 0
    var screenHeight: Int = 0
}