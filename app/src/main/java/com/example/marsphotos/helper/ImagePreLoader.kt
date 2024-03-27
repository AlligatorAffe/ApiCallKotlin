package com.example.marsphotos.helper

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest


class ImagePreLoader(private val context: Context) {
    private val imageLoader by lazy { ImageLoader(context) }

    fun preloadImages(urls: List<String>) {
        urls.forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .build()
            imageLoader.enqueue(request)
        }
    }
}
