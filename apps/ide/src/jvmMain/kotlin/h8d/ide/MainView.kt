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
import h8d.editor.Editor.ExecutionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart

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
    onRun: (Boolean) -> Unit,
    onSwitchView: (View) -> Unit,
) {
    var executionOutput by remember(editor) {
        mutableStateOf(listOf<String>())
    }
    val executionState by editor.executionState.collectAsState()
    LaunchedEffect(editor) {
        editor.executionState
            .flatMapLatest {
                if (it is ExecutionState.Running) {
                    it.output.onStart { emit("Executing:\n${it.code}") }
                } else {
                    emptyFlow()
                }
            }
            .collect { executionOutput += it }
    }
    Column(Modifier.fillMaxSize()) {
        Toolbar(executionState, onRun, onSwitchView)
        CurrentView(activeView, executionState, executionOutput, editor)
    }
}

@Composable
private fun CurrentView(
    activeView: View,
    executionState: ExecutionState,
    executionOutput: List<String>,
    editor: Editor,
) {
    when (activeView) {
        View.EXECUTION -> ExecutionView(executionState, executionOutput)
        else -> Editor(Modifier.fillMaxSize(), editor)
    }
}

@Composable
private fun Toolbar(
    executionState: ExecutionState,
    onRun: (shouldRun: Boolean) -> Unit,
    onSwitchView: (View) -> Unit,
) {
    Row(Modifier.fillMaxWidth()) {
        Button(
            onClick = { onSwitchView(View.EDITOR) },
        ) {
            Text("Editor")
        }
        Spacer(Modifier.size(16.dp))
        Button(
            onClick = { onRun(executionState is ExecutionState.Idle) },
        ) {
            Text(
                when (executionState) {
                    is ExecutionState.Idle -> "Run"
                    else -> "Stop execution"
                }
            )
        }
    }
}

@Composable
private fun ExecutionView(state: ExecutionState, output: List<String>) {
    if (output.isNotEmpty()) {
        LazyColumn(Modifier.fillMaxSize()) {
            items(count = output.size) {
                Text(output[it])
            }
        }
    }
}
