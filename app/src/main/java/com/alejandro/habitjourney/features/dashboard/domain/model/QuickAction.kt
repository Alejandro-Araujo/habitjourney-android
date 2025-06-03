package com.alejandro.habitjourney.features.dashboard.domain.model


import androidx.compose.ui.graphics.vector.ImageVector

data class QuickAction(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val route: String
)