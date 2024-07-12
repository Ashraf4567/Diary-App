package com.example.diaryapp.presentation.screens.home

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.connectivity.ConnectivityObserver
import com.example.diaryapp.connectivity.NetworkConnectivityObserver
import com.example.diaryapp.data.database.ImageToDeleteDao
import com.example.diaryapp.data.database.entity.ImageToDelete
import com.example.diaryapp.data.repository.Diaries
import com.example.diaryapp.data.repository.MongoDB
import com.example.diaryapp.model.RequestState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val connectivity: NetworkConnectivityObserver,
    private val imageToDeleteDao: ImageToDeleteDao
) : ViewModel() {

    private lateinit var allDiariesJob: Job
    private lateinit var filteredDiariesJob: Job

    private var network by mutableStateOf(ConnectivityObserver.Status.Unavailable)

    var diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)
    var dateIsSelected by mutableStateOf(false)
    private set

    init {
        getDiaries()
        viewModelScope.launch {
            connectivity.observe().collect {
                network = it
            }
        }
    }

    private fun observeAllDiaries() {
        allDiariesJob = viewModelScope.launch {
            if (::filteredDiariesJob.isInitialized){
                filteredDiariesJob.cancelAndJoin()
            }
            MongoDB.getAllDiaries().collect{res->
                diaries.value = res
            }
        }
    }

    fun getDiaries(zonedDateTime: ZonedDateTime? = null){
        diaries.value = RequestState.Loading
        dateIsSelected = zonedDateTime !=null
        if (dateIsSelected && zonedDateTime != null){
            observeFilteredDiaries(zonedDateTime = zonedDateTime)
        }else{
            observeAllDiaries()
        }
    }

    private fun observeFilteredDiaries(zonedDateTime: ZonedDateTime){
        filteredDiariesJob = viewModelScope.launch {
            if (::allDiariesJob.isInitialized) {
                allDiariesJob.cancelAndJoin()
            }
            MongoDB.getFilteredDiaries(zonedDateTime).collect{res->
                diaries.value = res
            }
        }
    }

    fun deleteAllDiaries(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (network == ConnectivityObserver.Status.Available) {
            val userId = Firebase.auth.currentUser?.uid
            val imagesDir = "images/$userId"
            val storage = FirebaseStorage.getInstance().reference
            storage.child(imagesDir)
                .listAll()
                .addOnSuccessListener {
                    it.items.forEach { ref ->
                        val path = "images/$userId/${ref.name}"
                        storage.child(path).delete()
                            .addOnFailureListener {
                                viewModelScope.launch(Dispatchers.IO) {
                                    imageToDeleteDao.addImageToDelete(
                                        ImageToDelete(
                                            remoteImagePath = path
                                        )
                                    )
                                }
                            }
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        val res = MongoDB.deleteAllDiaries()
                        if (res is RequestState.Success ) {
                            withContext(Dispatchers.Main){
                                onSuccess()
                            }
                        } else if (res is RequestState.Error) {
                            withContext(Dispatchers.Main){
                                onError(res.error)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    onError(it)
                }
        }else{
            onError(Throwable("No Internet Connection"))
        }
    }
}