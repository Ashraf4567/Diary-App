package com.example.diaryapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.auth.navigation.authenticationRoute
import com.example.navigation.homeRoute
import com.example.navigation.writeRoute
import com.example.util.model.Screens

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


