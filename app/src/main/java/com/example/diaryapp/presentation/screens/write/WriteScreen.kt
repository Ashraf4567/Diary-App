package com.example.diaryapp.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.diaryapp.model.Diary

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    onBackPressed: () -> Unit,
    selectedDiary: Diary?,
    onDeleteConfirmed: () -> Unit,
    pagerState: PagerState
) {
    Scaffold(
        topBar = {
            WriteTopBar(
                onBackPressed = onBackPressed,
                onDeleteConfirmed = onDeleteConfirmed
            )
                 },
        content = {
            WriteContent(
                paddingValues = it,
                pagerState = pagerState,
                title = "",
                onTitleChange ={} ,
                description = "",
                onDescriptionChange ={}
            )
        }
    )
}