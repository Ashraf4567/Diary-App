package com.example.diaryapp.presentation.screens.write

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.data.repository.MongoDB
import com.example.diaryapp.model.Diary
import com.example.diaryapp.model.Mood
import com.example.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.example.diaryapp.util.RequestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

class WriteViewModel(
    private val saveStateHandle: SavedStateHandle
): ViewModel() {

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
                    emit(RequestState.Error(it))
                }
                    .collect{diary ->
                        when(diary){
                            is RequestState.Success ->{
                                setSelectedDiary(diary = diary.data)
                                setMood(mood = Mood.valueOf(diary.data.mood))
                                setTitle(title = diary.data.title)
                                setDescription(diary.data.description)
                            }
                            else->{}
                        }
                    }
            }
        }
    }

    fun setSelectedDiary(diary: Diary){
        uiState = uiState.copy(selectedDiary = diary)
    }

    fun setTitle(title: String){
        uiState = uiState.copy(title = title)
    }

    fun setDescription(discription: String){
        uiState = uiState.copy(description = discription)
    }
    fun setMood(mood: Mood){
        uiState = uiState.copy(mood = mood)
    }

}

data class UiState(
    val selectedDiaryId: String? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val selectedDiary: Diary? = null
)