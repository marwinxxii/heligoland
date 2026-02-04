package h8d.editor

import h8d.parser.ParsingError
import h8d.parser.validateSourceCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class EditorImpl(
    coroutineDispatcher: CoroutineDispatcher,
) : Editor, AutoCloseable {
    private val scope = CoroutineScope(coroutineDispatcher)
    private val updates = Channel<String>(Channel.CONFLATED) // TODO check capacity

    // false positive in Kotlin 2.3.0
    @Suppress("RedundantModalityModifier")
    final override val state: StateFlow<Editor.State>
        field = MutableStateFlow(StateImpl(text = "", errors = emptyList()))

    private val updatesConsumerJob by lazy {
        scope.launch {
            updates.receiveAsFlow()
                .collect {
                    state.value = StateImpl(
                        text = it,
                        errors = validateSourceCode(it).map(ParsingError::toStateModel),
                    )
                }
        }
    }

    override fun submitUpdate(code: String) {
        updatesConsumerJob.isActive // trigger observation
        updates.trySend(code)
    }

    override fun close() {
        scope.cancel()
    }
}

private fun ParsingError.toStateModel() =
    StateImpl.ErrorImpl(
        lineNumber = pointer?.start?.lineNumber?.toUInt() ?: 1U,
        spanStart = pointer?.start?.characterPosition?.toUInt() ?: 1U,
        spanEnd = null,
        message = this.message,
    )

private data class StateImpl(
    override val text: String,
    override val errors: List<Editor.State.Error>,
) : Editor.State {
    data class ErrorImpl(
        override val lineNumber: UInt,
        override val spanStart: UInt,
        override val spanEnd: UInt?,
        override val message: String,
    ) : Editor.State.Error
}
