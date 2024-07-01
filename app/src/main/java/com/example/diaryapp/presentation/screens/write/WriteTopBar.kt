package com.example.diaryapp.presentation.screens.write

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.diaryapp.model.Diary
import com.example.diaryapp.presentation.components.DisplayAlertDialog
import com.example.diaryapp.util.toInstant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteTopBar(
    selectedDiary: Diary? = null,
    moodName: () -> String,
    onBackPressed: () -> Unit,
    onDeleteConfirmed: () -> Unit
) {

    val currentDate by remember { mutableStateOf(LocalDate.now()) }
    val currentTime by remember { mutableStateOf(LocalTime.now()) }
    val formattedDate by remember(currentDate) {
        mutableStateOf(
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
                .format(currentDate).uppercase()
        )
    }
    val formattedTime by remember(currentTime) {
        mutableStateOf(
            DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
                .format(currentTime).uppercase()
        )
    }

    val selectedDiaryDateTime = remember(selectedDiary) {
        if (selectedDiary != null) {
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(selectedDiary.date.toInstant())
        } else "$formattedDate, $formattedTime"
    }

    CenterAlignedTopAppBar(
        title = {
            Column {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = moodName(),
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = selectedDiaryDateTime,
                    style = TextStyle(fontSize = MaterialTheme.typography.bodySmall.fontSize,),
                    textAlign = TextAlign.Center
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Arrow Icon"
                )
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Date Icon",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            if (selectedDiary != null){
                DeleteDiaryAction(
                    selectedDiary = selectedDiary,
                    onDeleteConfirmed
                )
            }

        }
    )
}

@Composable
fun DeleteDiaryAction(
    selectedDiary: Diary?,
    onDeleteConfirmed: () -> Unit,
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    var opedDialog by remember {
        mutableStateOf(false)
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {expanded = false}
    ) {
        DropdownMenuItem(
            text = { Text(text = "Delete")},
            onClick = {
                expanded = false
                opedDialog = true
            }
        )

    }
    DisplayAlertDialog(
        title = "Delete",
        message = "Are you sure you want to delete this diary '${selectedDiary?.title}'",
        dialogOpen = opedDialog,
        onCloseDialog = { opedDialog = false },
        onYesClick = onDeleteConfirmed
    )
    IconButton(onClick = { expanded = !expanded }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Date Icon"
        )
    }
}