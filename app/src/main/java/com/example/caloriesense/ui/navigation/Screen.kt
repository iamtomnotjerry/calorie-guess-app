package com.example.caloriesense.ui.navigation

sealed class Screen(val route: String) {
    object Camera : Screen("camera")
    object Analysis : Screen("analysis")
}
