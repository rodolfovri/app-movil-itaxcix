package com.rodolfo.itaxcix.feature.citizen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.rodolfo.itaxcix.feature.driver.viewModel.DriverHomeViewModel

@Preview
@Composable
fun CitizenHomeScreenPreview() {
    CitizenHomeScreen()
}

@Composable
fun CitizenHomeScreen(
    viewModel: DriverHomeViewModel = hiltViewModel()
) {

    val userData by viewModel.userData.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Bienvenido, ${userData?.firstName ?: "Ciudadano"}",
            color = Color.Black,
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}