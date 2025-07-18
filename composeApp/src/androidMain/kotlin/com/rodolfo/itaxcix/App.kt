package com.rodolfo.itaxcix


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import com.rodolfo.itaxcix.navigation.AppNavigation

@Composable
fun App() {
    MaterialTheme {
        Surface (
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            AppNavigation()
        }
    }
}