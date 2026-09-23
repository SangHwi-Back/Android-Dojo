package com.example.moviceapp.myinfo

import androidx.lifecycle.ViewModel
import com.example.moviceapp.repo.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor (
    val cardRepository: CardRepository
) : ViewModel()