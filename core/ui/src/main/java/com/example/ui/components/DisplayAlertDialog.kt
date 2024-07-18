package com.example.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
