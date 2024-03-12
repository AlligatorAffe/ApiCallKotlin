
package com.example.marsphotos.network

import com.example.marsphotos.model.RandomPhotosItem
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import okhttp3.ResponseBody
import retrofit2.http.Url

private const val BASE_URL =
    "https://picsum.photos/"

//private const val BASE_URL = "https://picsum.photos/v2/list"
/**
 * Use the Retrofit builder to build a retrofit object using a kotlinx.serialization converter
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()

/**
 * Retrofit service object for creating api calls
 */
interface MarsApiService {
    @GET("v2/list")
    suspend fun getPhotos(): ArrayList<RandomPhotosItem>

    //suspend fun downloadImages(downloadable_url: List<String>) : ResponseBody
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object MarsApi {
    val retrofitService: MarsApiService by lazy {
        retrofit.create(MarsApiService::class.java)
    }
    val retroFitServ: DownloadPhotosApiService by lazy {
        retrofit.create(DownloadPhotosApiService::class.java)
    }
}

public interface DownloadPhotosApiService{
    @GET("")
    suspend fun downloadImages(@Url myUrls: String) : ResponseBody
}

