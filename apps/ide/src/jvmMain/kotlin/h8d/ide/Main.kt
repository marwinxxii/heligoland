package h8d.ide

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import h8d.editor.Editor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
@ExperimentalCoroutinesApi
public fun main(): Unit = application {
    val editor = Editor(Dispatchers.Default)
    Window(
        title = "Heligoland IDE",
        onCloseRequest = ::exitApplication,
    ) {
        var activeView by remember { mutableStateOf(View.EDITOR) }
        val onRun = remember(editor) {
            {
                activeView = View.EXECUTION
                editor.executeCode()
            }
        }
        val onSwitchView = remember {
            { view: View -> activeView = view }
        }
        SystemMenu(onRun = onRun)
        MaterialTheme {
            MainView(activeView, editor, onRun, onSwitchView)
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
