package com.example.diaryapp.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.diaryapp.model.GalleryImage
import com.example.diaryapp.model.Mood
import com.example.diaryapp.presentation.components.DisplayAlertDialog
import com.example.diaryapp.presentation.screens.auth.AuthenticationScreen
import com.example.diaryapp.presentation.screens.auth.AuthenticationViewModel
import com.example.diaryapp.presentation.screens.home.HomeScreen
import com.example.diaryapp.presentation.screens.home.HomeViewModel
import com.example.diaryapp.presentation.screens.write.WriteScreen
import com.example.diaryapp.presentation.screens.write.WriteViewModel
import com.example.diaryapp.util.Constants.APP_ID
import com.example.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.example.diaryapp.model.RequestState
import com.example.diaryapp.model.rememberGalleryState
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SetUpNavGraph(
    modifier: Modifier = Modifier,
    startDestination: String,
    navController: NavHostController,
    onDataLoaded: () -> Unit
) {
    NavHost(
        modifier = Modifier,
        navController = navController,
        startDestination = startDestination
    ){
        authenticationRoute(
            navigateToHome = {
                navController.popBackStack()
                navController.navigate(Screens.Home.route)
            },
            onDataLoaded = onDataLoaded,
        )
        homeRoute(
            onDataLoaded = onDataLoaded,
            navigateToWrite = {
            navController.navigate(Screens.Write.route)
        },
            navigateToAuth = {
                navController.popBackStack()
                navController.navigate(Screens.Authentication.route)
            },
            navigateToWriteWithArgs = {
                navController.navigate(Screens.Write.passDiaryId(it))
            }
        )
        writeRoute(
            onBackPressed = {
                navController.popBackStack()
            }
        )

    }
}

fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit,
    onDataLoaded: () -> Unit
){

    composable(route = Screens.Authentication.route){
        val viewModel: AuthenticationViewModel = viewModel()
        val authenticated by viewModel.authenticated
        val loadingState by viewModel.loadingState
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        LaunchedEffect(key1 = Unit){
            onDataLoaded()
        }

        AuthenticationScreen(
            authenticated = authenticated,
            loadingState = loadingState,
            oneTapState = oneTapState,
            messageBarState = messageBarState,
            onButtonClicked = {
                oneTapState.open()
                viewModel.setLoadingState(true)
            },
            onSuccessfulFirebaseSignIn = { token->

                viewModel.signInWithMongoAtlas(
                    tokenId = token,
                    onSuccess = {
                        messageBarState.addSuccess("Successfully Authenticated!")
                        viewModel.setLoadingState(false)
                    },
                    onError = {message->
                        messageBarState.addError(message)
                        viewModel.setLoadingState(false)
                    }
                )
            },
            onFailedFirebaseSignIn = {
                messageBarState.addError(it)
                viewModel.setLoadingState(false)
            },
            onDialogDismissed = {message->
                messageBarState.addError(Exception(message))
                viewModel.setLoadingState(false)
            },
            navigateToHome = {
                navigateToHome()
            }
        )
    }
}

fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit,
    navigateToAuth: () -> Unit,
    onDataLoaded: () -> Unit
){
    composable(route = Screens.Home.route){
        val viewModel: HomeViewModel = viewModel()
        val diaries by viewModel.diaries
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var signOutDialogOpen by remember { mutableStateOf(false) }
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
            navigateToWriteWithArgs = navigateToWriteWithArgs
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
    }
}

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
        val viewModel: WriteViewModel = viewModel()
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
            Log.d("SelectedDiary", "${uiState.selectedDiaryId}")
        }

        WriteScreen(
            uiState = uiState,
            onBackPressed = onBackPressed,
            moodName = {Mood.values()[pageNumber].name},
            onDeleteConfirmed = {
                viewModel.deleteDiary(
                    onSuccess = {
                        Toast.makeText(context,"Diary Deleted", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    },
                    onError = {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        Log.d("WriteScreen", "WriteScreen: $it")
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
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onDateAndTimeUpdated = {viewModel.updateDateAndTime(it)},
            galleryState = galleryState,
            onImageSelected = {uri->
                val type = context.contentResolver.getType(uri)?.split("/")?.last()?:"jpg"
                Log.d("WriteViewModel", "$uri")
                viewModel.addImage(
                    image = uri,
                    imageType = type
                )
            }
        )
    }
}