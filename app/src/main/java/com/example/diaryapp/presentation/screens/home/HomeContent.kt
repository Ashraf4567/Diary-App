package com.example.diaryapp.presentation.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diaryapp.model.Diary
import com.example.diaryapp.presentation.components.DiaryHolder
import java.time.LocalDate


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    paddingValues: PaddingValues,
    diaryNotes: Map<LocalDate, List<Diary>>,
    onDiaryClick: (String) -> Unit
) {
    if (diaryNotes.isNotEmpty()){
        LazyColumn(
            modifier = Modifier.padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            diaryNotes.forEach{(localDate , diaries)->
                stickyHeader(key = localDate) {
                    DateHeader(localDateTime = localDate)
                }
                items(diaries.size) { index ->
                    DiaryHolder(
                        diary = diaries[index],
                        onDiaryClick = onDiaryClick
                    )
                }
            }
        }
    }else{
        EmptyPage()
    }
}


@Composable
fun DateHeader(
    localDateTime: LocalDate
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.End) {

            Text(
                text = String.format("%02d" , localDateTime.dayOfMonth),
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Light
                )
            )

            Text(
                text = localDateTime.dayOfWeek.toString().take(3),
                style = TextStyle(
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = FontWeight.Light
                )
            )

        }
        Spacer(modifier = Modifier.width(14.dp))

        Column(horizontalAlignment = Alignment.Start) {

            Text(
                text = localDateTime.month.toString().lowercase()
                    .replaceFirstChar { it.titlecase() },
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Light
                )
            )

            Text(
                text = "${ localDateTime.year }",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                style = TextStyle(
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = FontWeight.Light
                )
            )

        }
    }

}

@Composable
fun EmptyPage(
    modifier: Modifier = Modifier,
    title: String = "Empty Diary",
    subTitle: String = "Write something"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
       Text(
           text = title,
           style = TextStyle(
               fontSize = MaterialTheme.typography.titleMedium.fontSize,
               fontWeight = FontWeight.Medium
           )
       )
        Text(
            text = subTitle,
            style = TextStyle(
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                fontWeight = FontWeight.Normal
            )
        )
    }

}
