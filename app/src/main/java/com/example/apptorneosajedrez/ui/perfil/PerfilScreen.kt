package com.example.apptorneosajedrez.ui.perfil

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apptorneosajedrez.R


@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: PerfilViewModel
) {
    val uiState by viewModel.uiState.observeAsState(PerfilUiState())

    ProfileScreenContent(
        modifier = modifier,
        uiState = uiState
    )
}


@Composable
private fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    uiState: PerfilUiState,
    userPicture: Int = R.drawable.pinguino
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.20f)
                .background(
                    color = MaterialTheme.colorScheme.primary
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.065f))

            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = userPicture),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = uiState.userName,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = uiState.userEmail,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = uiState.userType,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Permisos",
                    textAlign = TextAlign.Left,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                uiState.detallePermisos?.let {
                    Text(
                        text = it,
                        textAlign = TextAlign.Left,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    ProfileScreenContent(
        uiState = PerfilUiState(
            isLoading = false,
            userName = "María de los Ángeles Etchepareborda",
            userEmail = "maria.de.los.angeles.etchepareborda@gmail.com",
            userType = "Aficionado/a"
        )
    )
}