
package com.example.randomPhotos.network

import com.example.randomPhotos.model.RandomPhotosItem
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import okhttp3.ResponseBody
import retrofit2.http.Url

private const val BASE_URL =
    "https://picsum.photos/"


private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()


interface ApiService {
    @GET("v2/list")
    suspend fun getPhotos(): ArrayList<RandomPhotosItem>

    suspend fun downloadImages(@Url myUrls: String) : ResponseBody
}


object PhotoApi {
    val retrofitService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

}


