package com.example.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

@Composable
fun rememberGalleryState(): GallerySate {
    return remember { GallerySate() }
}

class GallerySate {
    val images = mutableStateListOf<GalleryImage>()
    val imagesToBeDeleted = mutableStateListOf<GalleryImage>()

    fun addImage(image: GalleryImage) {
        images.add(image)
    }

    fun removeImage(image: GalleryImage) {
        images.remove(image)
        imagesToBeDeleted.add(image)
    }

    fun clearImagesToBeDeleted() {
        imagesToBeDeleted.clear()
    }
}

data class GalleryImage(
    val imageUri: Uri,
    val remotePath: String = "",
)