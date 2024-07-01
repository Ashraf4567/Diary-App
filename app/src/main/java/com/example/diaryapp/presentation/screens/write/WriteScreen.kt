package com.example.diaryapp.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.diaryapp.model.Diary
import com.example.diaryapp.model.Mood

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    uiState: UiState,
    onBackPressed: () -> Unit,
    moodName: () -> String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDeleteConfirmed: () -> Unit,
    pagerState: PagerState
) {

    LaunchedEffect(key1 = uiState.mood) {
        pagerState.scrollToPage(Mood.valueOf(uiState.mood.name).ordinal)
    }

    Scaffold(
        topBar = {
            WriteTopBar(
                onBackPressed = onBackPressed,
                onDeleteConfirmed = onDeleteConfirmed,
                moodName = moodName,
                selectedDiary = uiState.selectedDiary
            )
                 },
        content = {
            WriteContent(
                paddingValues = it,
                pagerState = pagerState,
                title = uiState.title,
                onTitleChange = onTitleChange ,
                description = uiState.description,
                onDescriptionChange =onDescriptionChange
            )
        }
    )
}