package com.vibewardrobe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vibewardrobe.data.WardrobeLook

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WardrobeScreen(
    looks: List<WardrobeLook>,
    onAddLook: (WardrobeLook) -> Unit,
    onSelectLook: (WardrobeLook) -> Unit,
    onDeleteLook: (WardrobeLook) -> Unit,
    onEditLook: (WardrobeLook) -> Unit,
    enableHaptics: Boolean
) {
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFFFAFAFA), Color(0xFFE7E4EC))
    )
    val haptic = LocalHapticFeedback.current
    val snackbarHostState: SnackbarHostState = rememberSnackbarHostState()
    var pendingAction by remember { mutableStateOf<PendingAction?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "VibeWardrobe",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(24.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(looks, key = { it.id ?: it.hashCode() }) { look ->
                    var showMenu by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(CardDefaults.shape)
                            .combinedClickable(
                                onClick = {
                                    if (enableHaptics) {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    }
                                    onSelectLook(look)
                                },
                                onLongClick = {
                                    if (enableHaptics) {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    }
                                    showMenu = true
                                }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(look.thumbnailUrl ?: look.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = look.title,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = look.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            ActionMenu(
                                expanded = showMenu,
                                onDismiss = { showMenu = false },
                                onDelete = {
                                    onDeleteLook(look)
                                    showMenu = false
                                },
                                onEdit = {
                                    onEditLook(look)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                if (enableHaptics) {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                }
                val draft = WardrobeLook(
                    id = null,
                    title = "New Vibe",
                    style = "Casual",
                    intensity = 0.5f,
                    imageUrl = null,
                    thumbnailUrl = null
                )
                onAddLook(draft)
                pendingAction = PendingAction.Added
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add look")
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )

        LaunchedEffect(pendingAction) {
            when (pendingAction) {
                PendingAction.Added -> {
                    snackbarHostState.showSnackbar("Образ добавлен в гардероб 🌸")
                    pendingAction = null
                }
                null -> Unit
            }
        }
    }
}

@Composable
private fun ActionMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    AnimatedVisibility(visible = expanded, enter = fadeIn(), exit = fadeOut()) {
        androidx.compose.material3.DropdownMenu(
            expanded = true,
            onDismissRequest = onDismiss
        ) {
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Удалить") },
                onClick = onDelete
            )
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Редактировать") },
                onClick = onEdit
            )
        }
    }
}

enum class PendingAction { Added }
