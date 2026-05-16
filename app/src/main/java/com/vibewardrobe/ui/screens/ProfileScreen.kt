package com.vibewardrobe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vibewardrobe.data.ProfileSettings

@Composable
fun ProfileScreen(
    profileSettings: ProfileSettings,
    onSettingsChanged: (ProfileSettings) -> Unit
) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFFFAFAFA), Color(0xFFE7E4EC)))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Профиль",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(profileSettings.avatarUrl ?: "https://i.pravatar.cc/300")
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile avatar",
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth(0.4f)
            )
        }

        Text(
            text = profileSettings.displayName,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = profileSettings.apiKey,
            onValueChange = { onSettingsChanged(profileSettings.copy(apiKey = it)) },
            label = { Text("Gemini API Key") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        ThemeSwitchRow(
            label = "Тёмная тема",
            checked = profileSettings.isDarkTheme,
            onCheckedChange = { onSettingsChanged(profileSettings.copy(isDarkTheme = it)) }
        )
        ThemeSwitchRow(
            label = "Виброотклик",
            checked = profileSettings.enableHaptics,
            onCheckedChange = { onSettingsChanged(profileSettings.copy(enableHaptics = it)) }
        )

        Text(
            text = "Настрой свою атмосферу и сохраняй любимые образы в гардеробе.",
            textAlign = TextAlign.Center,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )

        TextButton(onClick = { /* TODO: open theme picker dialog */ }) {
            Text("Изменить цветовую палитру")
        }
    }
}

@Composable
private fun ThemeSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                checkedTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer
            )
        )
    }
}
