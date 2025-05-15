package com.rodolfo.itaxcix.feature.citizen.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

@Preview
@Composable
fun DashboardCitizenScreenPreview() {
    DashboardCitizenScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCitizenScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CitizenDrawerContent(onItemClick = {
                coroutineScope.launch { drawerState.close() }
                // Maneja la navegación según el ítem
            })
        },
        scrimColor = Color.Black.copy(alpha = 0.3f)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Panel del Ciudadano") },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            },
            content = { padding ->
                Column(modifier = Modifier.padding(padding)) {
                    Text("¡Bienvenido al panel principal del conductor!")
                }
            }
        )
    }
}