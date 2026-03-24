package com.example.caloriesense.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.caloriesense.ui.AnalysisViewModel
import com.example.caloriesense.ui.screens.AnalysisResultScreen
import com.example.caloriesense.ui.screens.CameraScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val viewModel: AnalysisViewModel = hiltViewModel()
    
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Camera.route
    ) {
        composable(Screen.Camera.route) {
            if (cameraPermissionState.status.isGranted) {
                CameraScreen(
                    onImageCaptured = { bitmap ->
                        viewModel.analyzeImage(bitmap)
                        navController.navigate(Screen.Analysis.route)
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera permission is required")
                        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
        
        composable(Screen.Analysis.route) {
            AnalysisResultScreen(
                viewModel = viewModel,
                onBack = {
                    viewModel.resetState()
                    navController.popBackStack()
                }
            )
        }
    }
}
