package com.rodolfo.itaxcix.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.MyColors

@Preview
@Composable
fun RegisterOptionsScreenPreview() {
    RegisterOptionsScreen(
        onCitizenClick = {},
        onDriverClick = {}
    )
}

@Composable
fun RegisterOptionsScreen(
    onCitizenClick: () -> Unit,
    onDriverClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(25.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UserCard(
            title = "Ciudadano",
            imageRes = R.drawable.citizen,
            onClick = { onCitizenClick() }
        )

        Text(
            text = "Selecciona tu tipo de registro",
            style = MaterialTheme.typography.titleMedium,
            color = ITaxCixPaletaColors.Blue1
        )

        UserCard(
            title = "Conductor",
            imageRes = R.drawable.driver,
            onClick = { onDriverClick() }
        )
    }
}

@Composable
fun UserCard(
    title: String,
    imageRes: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(200.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(MyColors.Background1)
            .clickable { onClick() },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = title,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
    }
}