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
package com.example.randomPhotos.ui.screens

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.randomPhotos.network.MarsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


sealed interface PhotoUiState {

    object Success: PhotoUiState

    object Error : PhotoUiState
    object Loading : PhotoUiState
}




class PhotoViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    var photoUiState: PhotoUiState by mutableStateOf(PhotoUiState.Loading)
        private set
    init {
        getMarsPhotos()
    }

    fun getMarsPhotos() {
        viewModelScope.launch {
            photoUiState = PhotoUiState.Loading
            photoUiState = try {
                val listResult = async { MarsApi.retrofitService.getPhotos() }.await()
                val imgUrls = listResult.map { it.downloadURL }
                async{ fetchImages(imgUrls) }.await()
                PhotoUiState.Success

            } catch (e: IOException) {
                PhotoUiState.Error
            } catch (e: HttpException) {
                PhotoUiState.Error
            }
        }
    }



    suspend fun fetchImages(urls: List<String>): MutableList<Bitmap> {
        var i = 1;
        val results = mutableListOf<Bitmap>()
        try {
            coroutineScope {
                val tasks = List(urls.size) { index -> // Drar igång hela url listans
                    async(Dispatchers.IO) {
                        performTask(urls[index])
                    }
                }
                tasks.forEach {
                    saveImageToInternalStorage(context,it.await(), i)
                    i++;

                }
            }
        } catch (e: Exception) {
            PhotoUiState.Error
        }
        return results
    }



    fun saveImageToInternalStorage(context: Context, bild: Bitmap, i : Int) {
        val folderName = "MyImages"
        val fileName = "IMG_$i.jpg"

        val folder = File(context.filesDir, folderName)
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val picFile = File(folder, fileName)

        if (picFile.exists()) {
            Log.d("BILDER", "EXISTS")
            return
        }

        try {
            val outputStream = FileOutputStream(picFile)
            outputStream.use {
                bild.compress(Bitmap.CompressFormat.JPEG, 70, it)
            }
            Log.d(
                "BILDER",
                "Bilden har sparats till den interna lagringen: $fileName i mappen $folderName"
            )
        } catch (e: IOException) {
            e.printStackTrace()
            PhotoUiState.Error
            Log.e("BILDER", "Misslyckades med att spara bilden till den interna lagringen")
        }
    }

    suspend fun performTask(myUrls: String): Bitmap {
        val response = MarsApi.retroFitServ.downloadImages(myUrls)

        val byteArray = response.bytes()
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)


        options.inSampleSize = calculateInSampleSize(options)

        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)

        return bitmap
    }

    //https://developer.android.com/topic/performance/graphics/load-bitmap
    fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        val reqWidth = 180;
        val reqHeight = 255;
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            while (height / inSampleSize >= reqHeight && width / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
