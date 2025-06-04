package com.rodolfo.itaxcix.navigation

import androidx.lifecycle.ViewModel
import com.rodolfo.itaxcix.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val preferencesManager: PreferencesManager
) : ViewModel()