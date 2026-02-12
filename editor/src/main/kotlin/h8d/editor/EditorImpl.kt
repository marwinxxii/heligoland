package h8d.editor

import h8d.editor.Editor.ExecutionState
import h8d.interpreter.Interpreter
import h8d.parser.ParseResult
import h8d.parser.SourceCodeError
import h8d.parser.parseProgram
import h8d.parser.validateSourceCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transformLatest
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
                        errors = validateSourceCode(it).map(SourceCodeError::toStateModel),
                    )
                }
        }
    }

    override fun submitUpdate(code: String) {
        updatesConsumerJob.isActive // trigger observation
        updates.trySend(code)
    }

    // false positive in Kotlin 2.3.0
    @Suppress("RedundantModalityModifier")
    final override val executionState: StateFlow<ExecutionState>
        field = MutableStateFlow<ExecutionState>(ExecutionState.Idle)

    private val executable = Channel<String?>(Channel.CONFLATED)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val executionJob by lazy {
        scope.launch {
            val interpreter = Interpreter(parallelFactor = 2, coroutineDispatcher)
            executable.receiveAsFlow()
                .transformLatest { code ->
                    code?.let(::parseProgram)
                        ?.let { it as? ParseResult.SuccessfulProgram }
                        ?.program
                        ?.let(interpreter::execute)
                        ?.onCompletion { setIdle() }
                        ?.also { emit(ExecutionState.Running(code = code, output = it)) }
                        ?: emit(ExecutionState.Idle)
                }
                .catch {
                    emit(
                        ExecutionState.Running(
                            code = "",
                            output = flowOf(
                                it.message.orEmpty(),
                                it.stackTraceToString(),
                            ),
                        )
                    )
                }
                .collect { executionState.value = it }
        }
    }

    override fun executeCode() {
        state.value
            .takeIf { it.errors.isEmpty() && it.text.isNotBlank() }
            ?.also { state ->
                if (executionState.value is ExecutionState.Idle) {
                    executionState.value = ExecutionState.Pending
                    executionJob.isActive
                    executable.trySend(state.text)
                }
            }
    }

    override fun stopProgram() {
        executable.trySend(null)
    }

    private fun setIdle() {
        executionState.value = ExecutionState.Idle
    }

    override fun close() {
        scope.cancel()
    }
}

private fun SourceCodeError.toStateModel() =
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
