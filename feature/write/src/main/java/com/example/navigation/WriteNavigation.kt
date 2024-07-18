package com.example.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.example.util.model.Mood
import com.example.util.model.Screens
import com.example.write.WriteScreen
import com.example.write.WriteViewModel

@OptIn(ExperimentalFoundationApi::class)
fun NavGraphBuilder.writeRoute(
    onBackPressed: () -> Unit
){

    composable(
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY){
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }),
        route = Screens.Write.route
    ){
        val viewModel: WriteViewModel = hiltViewModel()
        val galleryState = viewModel.galleryState
        val uiState = viewModel.uiState
        val context = LocalContext.current
        val pagerState = rememberPagerState(pageCount = {
            Mood.values().size
        })
        val pageNumber by remember {
            derivedStateOf {
                pagerState.currentPage
            }
        }

        LaunchedEffect(key1 = uiState) {
            android.util.Log.d("SelectedDiary", "${uiState.selectedDiaryId}")
        }

        WriteScreen(
            uiState = uiState,
            onBackPressed = onBackPressed,
            moodName = { Mood.values()[pageNumber].name},
            onDeleteConfirmed = {
                viewModel.deleteDiary(
                    onSuccess = {
                        android.widget.Toast.makeText(context,"Diary Deleted", android.widget.Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    },
                    onError = {
                        android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
                        android.util.Log.d("WriteScreen", "WriteScreen: $it")
                    }
                )
            },
            pagerState = pagerState,
            onTitleChange = viewModel::setTitle,
            onDescriptionChange = viewModel::setDescription,
            onSavedClicked = {
                viewModel.upsertDiary(
                    diary = it.apply { mood = Mood.values()[pageNumber].name },
                    onSuccess = {onBackPressed()},
                    onError = {message->
                        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onDateAndTimeUpdated = {viewModel.updateDateAndTime(it)},
            galleryState = galleryState,
            onImageSelected = {uri->
                val type = context.contentResolver.getType(uri)?.split("/")?.last()?:"jpg"
                android.util.Log.d("WriteViewModel", "$uri")
                viewModel.addImage(
                    image = uri,
                    imageType = type
                )
            },
            onImageDeleteClicked = {
                galleryState.removeImage(it)
            }
        )
    }
}