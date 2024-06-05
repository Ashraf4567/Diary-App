package com.example.diaryapp.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.diaryapp.ui.theme.DiaryAppTheme

@Composable
fun DisplayAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    dialogOpen: Boolean,
    onCloseDialog: () -> Unit,
    onYesClick: () -> Unit,
) {
    if (dialogOpen) {
        AlertDialog(
            title = {
                Text(text = title)
            },
            text = {
                Text(text = message)
            },
            onDismissRequest = onCloseDialog,
            confirmButton = {
                Button(
                    onClick = {
                    onYesClick()
                    onCloseDialog() }
                ){
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        onCloseDialog()
                    }
                ){
                    Text(text = "No")
                }
            }
        )
    }

}

@Preview(showBackground = true)
@Composable
private fun AlertDialogPreview() {
    DiaryAppTheme {
        DisplayAlertDialog(
            title = "Sign Out",
            message = "Are you sure you want to sign out?",
            dialogOpen = true,
            onCloseDialog = { /*TODO*/ }
        ) {

        }
    }
}