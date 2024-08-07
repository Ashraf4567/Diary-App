package com.example.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.Constants
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.GoogleAuthType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class AuthenticationViewModel: ViewModel() {

    var loadingState = mutableStateOf(false)
        private set

    var authenticated = mutableStateOf(false)

    fun setLoadingState(state: Boolean) {
        loadingState.value = state
    }

    fun signInWithMongoAtlas(
        tokenId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ){
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO){
                    App.create(Constants.APP_ID).login(
                        Credentials.google(tokenId , GoogleAuthType.ID_TOKEN)
                    ).loggedIn
                }
                withContext(Dispatchers.Main){
                    if (result){
                        onSuccess()
                        delay(600)
                        authenticated.value = true
                    }else{
                        onError(Exception("User is not logged in."))
                    }

                }

            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    onError(e)
                }
            }
        }

    }
}