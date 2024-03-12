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

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marsphotos.network.MarsApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
                Log.d("TAG", "Inne i denna röv funktionen!");
                //val image =  MarsApi.retroFitServ.downloadImages("https://picsum.photos/id/0/5000/3333")
                Log.d("TAG", "efter röv funktionen!");
                //Log.d("TAG","detta är i image ${image.string()}")
                val imageResponseBody = async {  fetchImages(imgUrls)}.await()
                Log.d("TAG", "DETTA ÄR VAD FETCH IMAGES RETURNS!!!!!!!!!!${imageResponseBody}")
                // KALLA PÅ DENNA ASYNCRONT OCH VÄNTA PÅ DEN
                //SEDAN SKICKAS BILDERNA VIDARE TILL SUCCESS!
                //MarsUiState.Success(imgUrls)
                MarsUiState.Success(imageResponseBody)
                
            } catch (e: IOException) {
                MarsUiState.Error
            } catch (e: HttpException) {
                MarsUiState.Error
            }
        }
    }



    fun fetchImages(urls: List<String>) = runBlocking {
        try {
            val results = mutableListOf<ByteArray>()
            coroutineScope {
                var i =0
                val tasks = List(urls.size) { index -> // Drar igång hela url listans
                    async {
                        i++

                        performTask(urls[index], i)

                    }
                }

                tasks.forEach {
                    results.add(it.await()) // Collect results, awaiting each task's completion
                }
                val röv = results
                Log.d("TAG","Detta kommer ut ur röven ${röv[1]}")
            }
            // Use results here
            return@runBlocking results
        } catch (e: Exception) {
            // Handle any exceptions thrown by tasks
        }
    }

    suspend fun performTask(myUrls: String, i : Int): ByteArray {
        Log.d("TAG", "Detta går in i responsen ${myUrls} och det är denna gången ${i}")
        val response = MarsApi.retroFitServ.downloadImages(myUrls)

        return response.bytes()

    }




}
