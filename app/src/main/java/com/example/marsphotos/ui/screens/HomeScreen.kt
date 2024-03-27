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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.marsphotos.R
import com.example.marsphotos.ui.theme.MarsPhotosTheme
import java.lang.System.out

@Composable
fun HomeScreen(
    marsUiState: MarsUiState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    when (marsUiState) {
        is MarsUiState.Loading -> LoadingScreen()
        is MarsUiState.Success -> ResultScreen(
            marsUiState.photos, modifier = modifier.fillMaxWidth()
        )
        is MarsUiState.Error -> ErrorScreen( modifier = modifier.fillMaxSize())
    }
}

/**
 * The home screen displaying the loading message.
 */
@Composable
fun LoadingScreen() {
    CircularProgressIndicator()
}

/**
 * The home screen displaying error message with re-attempt button.
 */
@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Failed to load", modifier = Modifier.padding(16.dp))
    }
}

/**
 * ResultScreen displaying number of photos retrieved.
 */

@Composable
fun ResultScreen(photos: MutableList<ByteArray>, modifier: Modifier = Modifier) {
    val columns = 2
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier
    ) {

        items(photos){
            Text(text = " ${it}")
            //val bild = convertImageByteArrayToBitmap(it)
            //val bilder = BitmapFactory.decodeByteArray(it, 0, it.size)
            /*
            Image(
                bitmap = bild.asImageBitmap(),
                contentDescription = "some useful description",
            )
             */

        }
        //items(photos){ it ->
        //Image(bitmap = it.asImageBitmap(), contentDescription ="HEJ" )
        //}
    }
}

@Composable
fun ImageComposable(imageUrl: String) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .padding(4.dp) // Lägg till mellanrum mellan grid cellerna.
            .clip(RoundedCornerShape(15.dp)) // Använd clip här för att runda hörnen.
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            loading = {
                CircularProgressIndicator()
            },
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            contentDescription = "randomPhotos"
        )
    }
}


@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    MarsPhotosTheme {
        LoadingScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    MarsPhotosTheme {
        ErrorScreen()
    }
}


/*
fun convertImageByteArrayToBitmap(imageData: ByteArray): Bitmap {
    val bildeen = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
    //ByteArrayOutputStream out = new ByteArrayOutputStream();
    bildeen.compress(Bitmap.CompressFormat.PNG, 20, out);

    return bildeen
}

 */
