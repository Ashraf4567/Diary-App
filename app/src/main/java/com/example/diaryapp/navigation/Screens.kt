package com.example.diaryapp.navigation

import com.example.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY

sealed class Screens(val route: String) {
    object Authentication : Screens( route = "authentication_screen")
    object Home : Screens(route =  "home_screen")
    object Write : Screens(route =  "write_screen?$WRITE_SCREEN_ARGUMENT_KEY={$WRITE_SCREEN_ARGUMENT_KEY}"){
        fun passDiaryId(diaryId: String) = "$WRITE_SCREEN_ARGUMENT_KEY=$diaryId"
    }
}