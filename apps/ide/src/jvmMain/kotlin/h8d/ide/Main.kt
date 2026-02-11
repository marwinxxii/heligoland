package h8d.ide

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import h8d.editor.Editor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
public fun main(): Unit = application {
    val editor = Editor(Dispatchers.Default)
    CoroutineScope(Dispatchers.Default).launch {
        editor.executionState
            .flatMapLatest {
                (it as? Editor.ExecutionState.Running)
                    ?.output
                    ?: emptyFlow()
            }
            .collect(::println)
    }
    Window(
        title = "Heligoland IDE",
        onCloseRequest = ::exitApplication,
    ) {
        SystemMenu(onRun = editor::executeCode)
        MaterialTheme {
            Editor(Modifier.fillMaxSize(), editor)
        }
    }
}

@Composable
private fun FrameWindowScope.SystemMenu(onRun: () -> Unit) {
    MenuBar {
        Menu(text = "Run") {
            Item(text = "Run code", shortcut = KeyShortcut(Key.R, ctrl = true), onClick = onRun)
        }
    }
}
