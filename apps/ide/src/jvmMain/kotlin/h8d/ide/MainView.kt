package h8d.ide

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import h8d.editor.Editor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlin.collections.plus

internal enum class View {
    EDITOR,
    EXECUTION,
    ;
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
internal fun MainView(
    activeView: View,
    editor: Editor,
    onRun: () -> Unit,
    onSwitchView: (View) -> Unit,
) {
    var executionOutput by remember(editor) {
        mutableStateOf(listOf<String>())
    }
    val executionState by editor.executionState.collectAsState()
    LaunchedEffect(editor) {
        editor.executionState
            .filterIsInstance<Editor.ExecutionState.Running>()
            .flatMapLatest {
                it.output.onStart { emit("Executing:\n${it.code}") }
            }
            .collect { executionOutput += it }
    }
    Column(Modifier.fillMaxSize()) {
        Toolbar(onRun, onSwitchView)
        CurrentView(activeView, executionState, executionOutput, editor)
    }
}

@Composable
private fun CurrentView(
    activeView: View,
    executionState: Editor.ExecutionState,
    executionOutput: List<String>,
    editor: Editor,
) {
    when (activeView) {
        View.EXECUTION -> ExecutionView(executionState, executionOutput)
        else -> Editor(Modifier.fillMaxSize(), editor)
    }
}

@Composable
private fun Toolbar(onRun: () -> Unit, onSwitchView: (View) -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        Button(
            onClick = { onSwitchView(View.EDITOR) },
        ) {
            Text("Editor")
        }
        Spacer(Modifier.size(16.dp))
        Button(
            onClick = onRun,
        ) {
            Text("Run")
        }
    }
}

@Composable
private fun ExecutionView(state: Editor.ExecutionState, output: List<String>) {
    Column(Modifier.fillMaxSize()) {
        if (state is Editor.ExecutionState.Running) {
            Text("Running:")
        } else {
            Text("Idle")
        }
        if (output.isNotEmpty()) {
            LazyColumn(Modifier.fillMaxSize()) {
                items(count = output.size) {
                    Text(output[it])
                }
            }
        }
    }
}
