package com.example.moviceapp.repo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UTCData(
    val raw: String,
    val date: String,
    val time: String,
) : Parcelable
fun UTCData.dateString(): String =
    raw.split("T").first().filter { it != '-' }
fun UTCData.timeString(): String =
    raw.split("T").last().filter { it != '-' && it != 'Z' }