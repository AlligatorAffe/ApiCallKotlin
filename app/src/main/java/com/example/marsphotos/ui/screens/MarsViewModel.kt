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
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marsphotos.network.MarsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * UI state for the Home screen
 */
sealed interface MarsUiState {
    //data class Success(val photos: String) : MarsUiState
    data class Success(val photos: MutableList<Bitmap>) : MarsUiState

    object Error : MarsUiState
    object Loading : MarsUiState
}

class MarsViewModel : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var marsUiState: MarsUiState by mutableStateOf(MarsUiState.Loading)
        private set
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
                val imageResponseBody = async{ fetchImages(imgUrls) }.await()
                Log.d("BILDER", "DETTA ÄR VAD FETCH IMAGES RETURNS!!!!!!!!!!${imageResponseBody}")

                MarsUiState.Success(imageResponseBody)

            } catch (e: IOException) {
                MarsUiState.Error
            } catch (e: HttpException) {
                MarsUiState.Error
            }
        }
    }



    suspend fun fetchImages(urls: List<String>): MutableList<Bitmap> {
        val results = mutableListOf<Bitmap>()
        try {
            coroutineScope {
                val tasks = List(urls.size) { index -> // Drar igång hela url listans
                    async(Dispatchers.IO) {
                        performTask(urls[index])
                    }
                }

                tasks.forEach {
                    val bilder = it.await()
                    //val bitmap = BitmapFactory.decodeStream(inputStream)
                    //val bitmap = decodeStream(inputStream)
                    //inputStream.close()
                    //if (bitmap != null) {
                    //       results.add(bitmap)
                    //  }
                    results.add(bilder)
                }
                val röv = results
                Log.d("BILDER","Detta kommer ut ur röven ${röv[1]}")
            }


        } catch (e: Exception) {

        }
        return results
    }

    suspend fun performTask(myUrls: String): Bitmap {
        val response = MarsApi.retroFitServ.downloadImages(myUrls)

        val byteArray = response.bytes()

        // Logga egenskaperna för bilden
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)

        // Apply the calculated sample size
        options.inSampleSize = calculateInSampleSize(options)

        // Now decode the bitmap with the calculated sample size
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)

        val imageHeight: Int = options.outHeight
        val imageWidth: Int = options.outWidth
        val imageType: String = options.outMimeType
        Log.d("BILDER", "IMAGE Height: $imageHeight")
        Log.d("BILDER", "IMAGE Width: $imageWidth")
        Log.d("BILDER", "IMAGE Type: $imageType")

        return bitmap
    }

    //https://developer.android.com/topic/performance/graphics/load-bitmap
    fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        val reqWidth = 630;
        val reqHeight = 900;
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = reqHeight  //height / 2
            val halfWidth: Int = reqWidth//width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /*
    suspend fun performTask(myUrls: String): ByteArray {
        Log.d("TAG", "Detta går in i responsen ${myUrls} och det är denna gången ")
        val response = MarsApi.retroFitServ.downloadImages(myUrls)
        //val remadeItems = convertImageByteArrayToBitMap(response.bytes())

        return response.bytes() //remadeItems

    }

     */
    /*
    fun convertImageByteArrayToBitMap(imageData: ByteArray) : List<Bitmap> {
        var results = mutableListOf<Bitmap>()
        for(image: Int in imageData){
            results = decodeByteArray(image as ByteArray, 0 , image.size)
        }
        return results
    }

     */




}




