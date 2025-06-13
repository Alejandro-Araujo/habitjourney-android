package com.alejandro.habitjourney.features.habit.presentation.state

enum class HabitFilterType {
    TODAY,      // Hábitos de hoy (reemplaza el switch anterior)
    ALL,        // Todos los hábitos activos
    ARCHIVED,   // Hábitos archivados
    COMPLETED,  // Hábitos completados hoy
    PENDING     // Hábitos pendientes de hoy
}