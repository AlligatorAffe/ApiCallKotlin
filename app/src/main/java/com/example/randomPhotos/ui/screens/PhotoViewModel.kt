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
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.marsphotos.network.MarsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


sealed interface MarsUiState {

    data class Success(val photos: MutableList<Bitmap>) : MarsUiState

    object Error : MarsUiState
    object Loading : MarsUiState
}




class PhotoViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    var photoUiState: MarsUiState by mutableStateOf(MarsUiState.Loading)
        private set
    init {
        getMarsPhotos()
    }

    fun getMarsPhotos() {
        viewModelScope.launch {
            photoUiState = MarsUiState.Loading
            photoUiState = try {
                val listResult = async { MarsApi.retrofitService.getPhotos() }.await()
                val imgUrls = listResult.map { it.downloadURL }
                val imageResponseBody = async{ fetchImages(imgUrls) }.await()
                MarsUiState.Success(imageResponseBody)

            } catch (e: IOException) {
                MarsUiState.Error
            } catch (e: HttpException) {
                MarsUiState.Error
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

                    val bilder = it.await()
                    saveImageToInternalStorage(context,it.await(), i)
                    results.add(bilder)
                    i++;

                }
                val röv = results
                Log.d("BILDER","Detta kommer ut ur röven ${röv[1]}")
            }
        } catch (e: Exception) {

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


/*
    fun saveImageToInternalStorage(context: Context, bild: Bitmap) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val folderName = "MyImages" // Namnet på din mapp
        val fileName = "IMG_$timeStamp.jpg"

        val folder = File(context.filesDir, folderName)
        if (!folder.exists()) {
            folder.mkdirs() // Skapa mappen om den inte redan finns
        }

        try {
            val file = File(folder, fileName)
            val outputStream = FileOutputStream(file)
            outputStream.use {
                bild.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            Log.d("BILDER", "Bilden har sparats till den interna lagringen: $fileName i mappen $folderName")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("BILDER", "Misslyckades med att spara bilden till den interna lagringen")
        }

    }
 */


/*
    fun saveImageToInternalStorage(context: Context, bild:Bitmap) {
        val folderName = "MyImages"
        val folder = File(context.filesDir, folderName)
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val outputStream = context.openFileOutput(bild.toString(), Context.MODE_PRIVATE)
        bild.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        Log.d("BILDER", "Bilden har sparats till den interna lagringen ${bild}")
    }
 */
/*
fun storeImage(image: Bitmap): Uri? {
    var pictureFile: File = File(Environment.getExternalStorageDirectory().path + "/Folder")
    val name = image.toString()
    pictureFile = File(pictureFile.path + File.separator + name)

    try {
        val fos = FileOutputStream(pictureFile)
        image.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        fos.close()
        Log.d("BILDER","SAVED TO INTERNAL ==${pictureFile.toUri()}")
        return pictureFile.toUri()
    } catch (e: Exception) {
        e.printStackTrace()
        Log.d("BILDER","FAIL")
        return null
    }
}

 */