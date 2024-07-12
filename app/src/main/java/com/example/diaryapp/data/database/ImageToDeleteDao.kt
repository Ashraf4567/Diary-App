package com.example.diaryapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.diaryapp.data.database.entity.ImageToDelete

@Dao
interface ImageToDeleteDao {
    @Query("SELECT * FROM images_to_delete_table ORDER BY id ASC")
    suspend fun getAll(): List<ImageToDelete>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImageToDelete(imageToDelete: ImageToDelete)

    @Query("DELETE FROM images_to_delete_table WHERE id = :imageId")
    suspend fun cleanUpImage(imageId: Int)
}