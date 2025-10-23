package com.vibewardrobe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vibewardrobe.api.GeminiService
import com.vibewardrobe.data.ProfileSettings
import com.vibewardrobe.data.WardrobeLook
import kotlinx.coroutines.launch

private val styles = listOf("Повседневный", "Бизнес", "Спорт", "Фантазийный")

@OptIn(ExperimentalAnimationApi::class, ExperimentalMotionApi::class)
@Composable
fun TryOnScreen(
    selectedLook: WardrobeLook?,
    profileSettings: ProfileSettings,
    onBack: () -> Unit,
    onLookGenerated: (WardrobeLook) -> Unit
) {
    var intensity by remember { mutableStateOf(selectedLook?.intensity ?: 0.5f) }
    var expanded by remember { mutableStateOf(false) }
    var style by remember { mutableStateOf(selectedLook?.style ?: styles.first()) }
    var generatedImageUrl by remember { mutableStateOf(selectedLook?.imageUrl) }
    var isLoading by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val shimmerAlpha by animateFloatAsState(targetValue = if (isLoading) 0.7f else 0f, label = "shimmer")

    val gradient = Brush.verticalGradient(listOf(Color(0xFFFAFAFA), Color(0xFFE7E4EC)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                }
            }

            Text(
                text = "Создай новый образ",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            MotionLayoutContainer(shimmerAlpha = shimmerAlpha) {
                if (generatedImageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(generatedImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Generated look",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    androidx.compose.material3.Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp),
                        tonalElevation = 3.dp,
                        shape = MaterialTheme.shapes.large
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            androidx.compose.material3.Slider(
                value = intensity,
                onValueChange = { intensity = it },
                modifier = Modifier.fillMaxWidth(),
                valueRange = 0f..1f
            )
            Text(
                text = "Интенсивность: ${(intensity * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = style,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Стиль") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    styles.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                style = it
                                expanded = false
                            }
                        )
                    }
                }
            }

            ElevatedButton(
                onClick = {
                    if (profileSettings.enableHaptics) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    }
                    scope.launch {
                        isLoading = true
                        val prompt = "Создай новый образ на основе фотографии. Стиль: $style. Интенсивность: ${"%.2f".format(intensity)}. Сохрани реалистичность лица."
                        val response = GeminiService.generateLook(
                            prompt = prompt,
                            baseImageUrl = selectedLook?.imageUrl,
                            apiKey = profileSettings.apiKey
                        )
                        generatedImageUrl = response?.imageUrl ?: selectedLook?.imageUrl
                        isLoading = false
                        if (profileSettings.enableHaptics) {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        }
                        onLookGenerated(
                            (selectedLook ?: WardrobeLook())
                                .copy(
                                    style = style,
                                    intensity = intensity,
                                    imageUrl = generatedImageUrl,
                                    thumbnailUrl = response?.thumbnailUrl
                                )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Look")
            }
        }

        AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
            ProcessingOverlay()
        }
    }
}

@Composable
private fun MotionLayoutContainer(
    shimmerAlpha: Float,
    content: @Composable () -> Unit
) {
    val scene = remember {
        MotionScene(
            """
            {
              ConstraintSets: {
                start: {
                  image: {
                    width: 'spread',
                    height: 'spread',
                    start: ['parent', 'start', 16],
                    end: ['parent', 'end', 16],
                    top: ['parent', 'top', 0],
                    bottom: ['parent', 'bottom', 0]
                  }
                },
                end: {
                  image: {
                    width: 'spread',
                    height: 'spread',
                    start: ['parent', 'start', 8],
                    end: ['parent', 'end', 8],
                    top: ['parent', 'top', 0],
                    bottom: ['parent', 'bottom', 0]
                  }
                }
              },
              Transitions: {
                default: {
                  from: 'start',
                  to: 'end',
                  pathMotionArc: 'start'
                }
              }
            }
            """
        )
    }
    MotionLayout(
        motionScene = scene,
        progress = 1f,
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    ) {
        Box(
            modifier = Modifier
                .layoutId("image")
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = shimmerAlpha),
                            Color.Transparent
                        )
                    )
                )
        ) {
            content()
        }
    }
}

@Composable
private fun ProcessingOverlay() {
    val shimmerTransition = rememberInfiniteTransition(label = "overlay")
    val alpha by shimmerTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "overlayAlpha"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAAE7E4EC)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = "Создаём твой образ...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
        }
    }
}
