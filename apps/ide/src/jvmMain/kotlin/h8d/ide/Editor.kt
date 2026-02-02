package h8d.ide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import h8d.editor.Editor
import kotlin.collections.orEmpty

// TODO avoid recomposition
@Composable
internal fun Editor(modifier: Modifier, editor: Editor) {
    Box(modifier) {
        val state by editor.state.collectAsState()
        val errors = remember(state) {
            state.takeIf { it.text.isNotEmpty() }
                ?.errors
                .orEmpty()
        }
        val onUpdate = remember(editor) {
            // false positive in Kotlin 2.3.0
            @Suppress("SuspiciousCallableReferenceInLambda", "RedundantSuppression")
            editor::submitUpdate
        }
        Editor(
            modifier = Modifier.fillMaxSize(),
            errors = errors,
            onEdit = onUpdate,
        )
        ErrorsView(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.6f)
                .align(Alignment.TopEnd),
            errors = errors,
        )
    }
}

@Composable
private fun ErrorsView(modifier: Modifier, errors: List<Editor.State.Error>) {
    val text = remember(errors) {
        errors.joinToString("\n", transform = { "${it.lineNumber}:${it.spanStart ?: 1} ${it.message}" })
    }
    Text(
        modifier = modifier
            .background(Color.Transparent)
            .padding(10.dp),
        text = text,
        fontSize = 10.sp,
        color = Color(0xff950606),
    )
}

@Composable
private fun Editor(
    modifier: Modifier,
    errors: List<Editor.State.Error>,
    onEdit: (String) -> Unit,
) {
    val textState = rememberTextFieldState(initialText = "")
    LaunchedEffect(textState) {
        snapshotFlow { textState.text.toString() }.collect(onEdit)
    }
    Editor(modifier, textState, errors, onEdit)
}

@Composable
private fun Editor(
    modifier: Modifier,
    state: TextFieldState,
    errors: List<Editor.State.Error>,
    onEdit: (String) -> Unit,
) {
    TextField(
        state = state,
        modifier = modifier,
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
        ),
        // TODO output transformation to highlight in the line?
    )
}
