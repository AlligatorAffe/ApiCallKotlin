package com.example.marsphotos.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RandomPhotosItem(
    val author: String,
    //@SerialName(value = "download_url")
    val download_url: String,
    val height: Int,
    val id: String,
    val url: String,
    val width: Int
)