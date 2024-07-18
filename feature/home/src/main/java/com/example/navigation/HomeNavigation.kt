package com.example.navigation

import android.widget.Toast
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.home.HomeScreen
import com.example.home.HomeViewModel
import com.example.ui.components.DisplayAlertDialog
import com.example.util.Constants.APP_ID
import com.example.util.model.RequestState
import com.example.util.model.Screens
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit,
    navigateToAuth: () -> Unit,
    onDataLoaded: () -> Unit
){
    composable(route = Screens.Home.route){
        val viewModel: HomeViewModel = hiltViewModel()
        val diaries by viewModel.diaries
        val context = LocalContext.current
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var signOutDialogOpen by remember { mutableStateOf(false) }
        var deleteAllDialogOpen by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(key1 = diaries){
            if(diaries !is RequestState.Loading){
                onDataLoaded()
            }
        }
        HomeScreen(
            diaries = diaries,
            onMenuClick = {
                coroutineScope.launch(Dispatchers.Default){
                    drawerState.open()
                }
            },
            navigateToWriteScreen = navigateToWrite,
            drawerState = drawerState,
            onSignOutClick = {signOutDialogOpen = true},
            navigateToWriteWithArgs = navigateToWriteWithArgs,
            onDeleteAllClick = {deleteAllDialogOpen = true},
            dateSelected = viewModel.dateIsSelected,
            onDateSelected = {viewModel.getDiaries(it)},
            onDateReset = {viewModel.getDiaries()}
        )

        DisplayAlertDialog(
            title ="Sign Out",
            message ="Are you sure you want to sign out?",
            dialogOpen = signOutDialogOpen,
            onCloseDialog = {signOutDialogOpen = false},
            onYesClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    val user = App.create(APP_ID).currentUser
                    user?.logOut()
                    withContext(Dispatchers.Main){
                        navigateToAuth()
                    }
                }
            }
        )
        DisplayAlertDialog(
            title ="Delete All Diaries",
            message ="Are you sure you want to Delete All Diaries?",
            dialogOpen = deleteAllDialogOpen,
            onCloseDialog = {deleteAllDialogOpen = false},
            onYesClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    viewModel.deleteAllDiaries(
                        onSuccess = {
                            Toast.makeText(
                                context,
                                "All Diaries Deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        },
                        onError = {
                            Toast.makeText(
                                context,
                                if (it.message == "No Internet Connection") it.message else "Failed to Delete All Diaries",
                                Toast.LENGTH_SHORT
                            ).show()
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        }
                    )
                }
            }
        )
    }
}