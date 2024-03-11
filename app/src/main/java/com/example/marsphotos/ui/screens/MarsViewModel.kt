/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.marsphotos.ui.screens

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.marsphotos.helper.ImagePreLoader
import com.example.marsphotos.model.MarsPhoto
import com.example.marsphotos.network.MarsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * UI state for the Home screen
 */
sealed interface MarsUiState {
    //data class Success(val photos: String) : MarsUiState
    data class Success(val photos: List<String>) : MarsUiState

    object Error : MarsUiState
    object Loading : MarsUiState
}

class MarsViewModel : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var marsUiState: MarsUiState by mutableStateOf(MarsUiState.Loading)
        private set
    var photos: MutableState<List<Bitmap>> = mutableStateOf(emptyList())


     //  getMarsPhotos() on init so we can display status immediately.

    init {
        getMarsPhotos()
    }

    fun getMarsPhotos() {
        viewModelScope.launch {
            marsUiState = MarsUiState.Loading
            marsUiState = try {
                //val listResult = MarsApi.retrofitService.getPhotos()
                val listResult = async { MarsApi.retrofitService.getPhotos() }.await()
                val imgUrls = listResult.map { it.downloadURL }

                //async { preloadImages(context = Context,imgUrls ) }.await()
                MarsUiState.Success(imgUrls)
                
            } catch (e: IOException) {
                MarsUiState.Error
            } catch (e: HttpException) {
                MarsUiState.Error
            }
        }
    }

    fun fetchImages() = runBlocking {
        try {
            val results = mutableListOf<ResultType>() // Replace ResultType with your actual type
            coroutineScope {
                val tasks = java.util.List(10) { index -> // Launch 10 tasks as an example
                    async {
                        // Your asynchronous task here. For example: här trycker vi in bilderna.
                        performTask(index)
                    }
                }
                tasks.forEach {
                    results.add(it.await()) // Collect results, awaiting each task's completion
                }
            }
            // Use results here
        } catch (e: Exception) {
            // Handle any exceptions thrown by tasks
        }
    }

    suspend fun performTask(index: Int): ResultType {
        // Implement your task here downloada images here?!
    }


}
