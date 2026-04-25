package com.yami.coding

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Yami Coding"
    ) {
        MaterialTheme {
            Surface {
                App()
            }
        }
    }
}

@Composable
fun App() {
    Text("Yami Coding - Desktop")
}
