

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.randomPhotos.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.randomPhotos.ui.screens.HomeScreen
import com.example.randomPhotos.ui.screens.PhotoViewModel

@Composable
fun PhotosApp() {
    Scaffold(
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
                .padding(top = 30.dp, start = 5.dp, end = 5.dp)
        ) {
            val photoViewModel: PhotoViewModel = viewModel()
            HomeScreen(
                photoUiState = photoViewModel.photoUiState,
            )
        }
    }
}

