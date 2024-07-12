package com.example.diaryapp.presentation.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.diaryapp.R
import com.example.diaryapp.data.repository.Diaries
import com.example.diaryapp.model.RequestState
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    diaries: Diaries,
    onMenuClick: () -> Unit,
    navigateToWriteScreen: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit,
    drawerState: DrawerState,
    onSignOutClick: () -> Unit,
    onDeleteAllClick: () -> Unit,
    dateSelected: Boolean,
    onDateSelected: (ZonedDateTime) -> Unit,
    onDateReset: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    NavigationDrawer(
        drawerState = drawerState ,
        onSignOutClick = onSignOutClick,
        onDeleteAllClick = onDeleteAllClick
    ) {
        Scaffold(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                HomeTopBar(
                    scrollBehavior = scrollBehavior,
                    onMenuClicked = { onMenuClick() },
                    dateSelected = dateSelected,
                    onDateSelected = onDateSelected,
                    onDateReset = onDateReset
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {navigateToWriteScreen()}) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "New diary Icon"
                    )
                }
            },
            content = {
                when(diaries){
                    is RequestState.Success -> {
                        HomeContent(
                            diaryNotes = diaries.data,
                            onDiaryClick = navigateToWriteWithArgs,
                            paddingValues = it
                        )
                    }
                    is RequestState.Error -> {
                        EmptyPage(
                            title = "Error",
                            subTitle = "${diaries.error.message}"
                        )
                    }
                    is RequestState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    else -> {}
                }

            }
        )
    }
}

@Composable
fun NavigationDrawer(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    onSignOutClick: () -> Unit,
    onDeleteAllClick: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(content = {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ){
                    Image(
                        modifier = Modifier.size(250.dp),
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo"
                    )
                }
                NavigationDrawerItem(
                    label = {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.google_logo),
                                tint = MaterialTheme.colorScheme.onSurface,
                                contentDescription = "Google Logo"
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Sign Out" , color = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    selected = false,
                    onClick = onSignOutClick
                )
                NavigationDrawerItem(
                    label = {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                tint = MaterialTheme.colorScheme.onSurface,
                                contentDescription = "Delete all Diaries"
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Delete All Diaries" , color = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    selected = false,
                    onClick = onDeleteAllClick
                )

            })
        },
        drawerState = drawerState,
        content = content
    )
}