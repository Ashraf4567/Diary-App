package com.example.write

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.data.database.entity.ImageToUpload
import com.example.mongo.repository.MongoDB
import com.example.util.model.Diary
import com.example.util.model.Mood
import com.example.util.model.RequestState
import com.example.mongo.database.ImageToDeleteDao
import com.example.mongo.database.ImageToUploadDao
import com.example.mongo.database.entity.ImageToDelete
import com.example.ui.GalleryImage
import com.example.ui.GallerySate
import com.example.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.example.util.fetchImagesFromFirebase
import com.example.util.toRealmInstant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class WriteViewModel @Inject constructor (
    private val saveStateHandle: SavedStateHandle,
    private val imageToUploadDao: ImageToUploadDao,
    private val imageToDeleteDao: ImageToDeleteDao
): ViewModel() {

    val galleryState = GallerySate()

    var uiState by mutableStateOf(UiState())
        private set

    init {
        getDiaryIdArgument()
        fetchSelectedDiary()
    }

    private fun getDiaryIdArgument(){
        uiState = uiState.copy(
            selectedDiaryId = saveStateHandle.get<String>(
                key = WRITE_SCREEN_ARGUMENT_KEY
            )
        )
    }

    private fun fetchSelectedDiary(){
        val selectedDiaryId = uiState.selectedDiaryId
        if (selectedDiaryId!= null){
            viewModelScope.launch(Dispatchers.Main) {
                MongoDB.getSelectedDiary(
                    diaryId = BsonObjectId.invoke(selectedDiaryId)
                ).catch {
                    emit(RequestState.Error(Exception("Diary is Already deleted")))
                }
                    .collect{diary ->
                        when(diary){
                            is RequestState.Success ->{
                                setSelectedDiary(diary = diary.data)
                                setMood(mood = Mood.valueOf(diary.data.mood))
                                setTitle(title = diary.data.title)
                                setDescription(diary.data.description)

                                fetchImagesFromFirebase(
                                    remoteImagePaths = diary.data.images,
                                    onImageDownload = {downloadUri ->
                                        galleryState.addImage(
                                            GalleryImage(
                                                imageUri = downloadUri,
                                                remotePath = extractImagePath(downloadUri.toString())
                                            )
                                        )
                                    }
                                )
                            }
                            else->{}
                        }
                    }
            }
        }
    }
    private fun extractImagePath(fullImageUrl: String): String {
        val chunks = fullImageUrl.split("%2F")
        val imageName = chunks[2].split("?").first()
        return "images/${Firebase.auth.currentUser?.uid}/$imageName"
    }

    fun upsertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO) {
            if (uiState.selectedDiaryId != null){
                updateDiary(diary = diary, onSuccess = onSuccess, onError = onError)
            }else{
                insertDiary(diary = diary, onSuccess = onSuccess, onError = onError)
            }
        }
    }

    private suspend fun insertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ){
        val result = MongoDB.addNewDiary(diary.apply {
            if (uiState.updatedDateAndTime != null){
                date = uiState.updatedDateAndTime!!
            }
        })
        if (result is RequestState.Success){
            uploadImageToFirebase()
            withContext(Dispatchers.Main){
                onSuccess()
            }
        }
        else if (result is RequestState.Error){
            withContext(Dispatchers.Main){
                onError(result.error.message.toString())
            }
        }
    }

    private fun setSelectedDiary(diary: Diary){
        uiState = uiState.copy(selectedDiary = diary)
    }

    fun setTitle(title: String){
        uiState = uiState.copy(title = title)
    }

    fun setDescription(discription: String){
        uiState = uiState.copy(description = discription)
    }
    private fun setMood(mood: Mood){
        uiState = uiState.copy(mood = mood)
    }
    fun updateDateAndTime(dateAndTime: ZonedDateTime ){
        uiState = uiState.copy(updatedDateAndTime = dateAndTime.toInstant()?.toRealmInstant())

    }


    private suspend fun updateDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ){
         val result = MongoDB.updateDiary(
             diary =  diary.apply {
             _id = BsonObjectId.invoke(uiState.selectedDiaryId!!)
             date = if (uiState.updatedDateAndTime != null){
                 uiState.updatedDateAndTime!!
             }else{
                 uiState.selectedDiary!!.date
             }
         }
         )
        if (result is RequestState.Success){
            uploadImageToFirebase()
            deleteImagesFromFirebase()
            withContext(Dispatchers.Main){
                onSuccess()
            }
        }
        else if (result is RequestState.Error){
            withContext(Dispatchers.Main){
                onError(result.error.message.toString())
            }
        }
    }

    fun deleteDiary(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO) {
            if (uiState.selectedDiaryId != null){
                val result = MongoDB.deleteDiary(BsonObjectId.invoke(uiState.selectedDiaryId!!))
                if(result is RequestState.Success) {

                    withContext(Dispatchers.Main){
                        uiState.selectedDiary?.images?.let { deleteImagesFromFirebase(it) }
                        onSuccess()
                    }
                }else if(result is RequestState.Error){
                    withContext(Dispatchers.Main){
                        onError(result.error.message.toString())
                    }
                }
            }
        }
    }

    private fun deleteImagesFromFirebase(images: List<String>? = null){
        val storage = Firebase.storage.reference
        if (images != null){
            images.forEach {remoteImagePath ->
                storage.child(remoteImagePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToDeleteDao
                                .addImageToDelete(
                                    ImageToDelete(remoteImagePath = remoteImagePath)
                                )
                        }
                    }
            }
        }else{
            galleryState.imagesToBeDeleted.map {
                it.remotePath
            }.forEach {remoteImagePath ->
                storage.child(remoteImagePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToDeleteDao
                                .addImageToDelete(
                                    ImageToDelete(remoteImagePath = remoteImagePath)
                                )
                        }
                    }
            }
        }
    }

    fun addImage(image: Uri , imageType: String){
        val remoteImagePath = "images/${FirebaseAuth.getInstance().currentUser?.uid}/" +
                "${image.lastPathSegment}-${System.currentTimeMillis()}.$imageType"
        Log.d("WriteViewModel", "addImage: $remoteImagePath")
        galleryState.addImage(
            GalleryImage(
                imageUri = image,
                remotePath = remoteImagePath
            )
        )
    }

    private fun uploadImageToFirebase(){
        val storage = Firebase.storage.reference
        galleryState.images.forEach {galleryImage ->
            val imagePath = storage.child(galleryImage.remotePath)
            imagePath.putFile(galleryImage.imageUri)
                .addOnProgressListener {
                    val sessionUri = it.uploadSessionUri
                    if (sessionUri != null){
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToUploadDao.addImageToUpload(
                                ImageToUpload(
                                    remoteImagePath = galleryImage.remotePath,
                                    imageUri = galleryImage.imageUri.toString(),
                                    sessionUri = sessionUri.toString()
                                )
                            )
                        }
                    }
                }
        }
    }
}

data class UiState(
    val selectedDiaryId: String? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val selectedDiary: Diary? = null,
    val updatedDateAndTime: RealmInstant? = null
)