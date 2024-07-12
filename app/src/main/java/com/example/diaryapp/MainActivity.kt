package com.example.diaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.diaryapp.data.database.ImageToDeleteDao
import com.example.diaryapp.data.database.ImageToUploadDao
import com.example.diaryapp.navigation.Screens
import com.example.diaryapp.navigation.SetUpNavGraph
import com.example.diaryapp.ui.theme.DiaryAppTheme
import com.example.diaryapp.util.Constants.APP_ID
import com.example.diaryapp.util.retryDeleteImageFromFirebase
import com.example.diaryapp.util.retryUploadImageToFirebase
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    var keepSplashScreen = true
    @Inject
    lateinit var imageToUploadDao: ImageToUploadDao
    @Inject
    lateinit var imageToDeleteDao: ImageToDeleteDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        installSplashScreen().setKeepOnScreenCondition{
            keepSplashScreen
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DiaryAppTheme {
                val navController = rememberNavController()
                SetUpNavGraph(
                    startDestination = getStartDestination(),
                    navController = navController,
                    onDataLoaded = {
                        keepSplashScreen = false
                    }
                )
            }
        }
        cleanupCheck(
            lifecycleScope,
            imageToUploadDao,
            imageToDeleteDao
        )
    }
}

private fun cleanupCheck(
    scope: CoroutineScope,
    imageToUploadDao: ImageToUploadDao,
    imageToDeleteDao: ImageToDeleteDao
){
    scope.launch(Dispatchers.IO) {
        val result = imageToUploadDao.getAllImages()
        result.forEach {imageToUpload ->
            retryUploadImageToFirebase(
                imageToUpload = imageToUpload,
                onSuccess = {
                    scope.launch(Dispatchers.IO) {
                        imageToUploadDao.deleteImage(imageToUpload.id)
                    }
                }
            )
        }
        val resultToDelete = imageToDeleteDao.getAll()
        resultToDelete.forEach {imageToDelete ->
            retryDeleteImageFromFirebase(imageToDelete){
                scope.launch(Dispatchers.IO) {
                    imageToDeleteDao.cleanUpImage(imageToDelete.id)
                }
            }
        }
    }
}

private fun getStartDestination(): String {
    val user = App.Companion.create(APP_ID).currentUser
    return if (user != null && user.loggedIn) Screens.Home.route
            else Screens.Authentication.route
}
