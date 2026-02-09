package h8d.stackmachine

import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

@Throws(InstructionException::class)
public suspend fun <V : Any, I : StackInstruction<V>> computeStack(
    instructions: Iterable<I>,
    context: ExecutionContext<V> = DefaultExecutionContext(extensions = emptySet()),
    onEach: suspend (instruction: I, pointer: Int, stack: Iterable<V>) -> Unit = { _, _, _ -> },
): ExecutionContext.ValueStack<V> {
    context as DefaultExecutionContext<V>
    instructions.forEachIndexed { index, instruction ->
        coroutineContext.ensureActive()
        instruction.execute(context)
            ?.also(context.stack::push)
        onEach(instruction, index, context.stack)
    }
    return context.stack
}

public class InstructionException(
    instruction: StackInstruction<*>,
    message: String,
) : RuntimeException("Error executing $instruction: $message")

public interface StackInstruction<V : Any> {
    /**
     * Execute the instruction in the given context.
     * @return value to push on to the stack or `null` if no value should be pushed.
     */
    @Throws(InstructionException::class)
    public fun execute(context: ExecutionContext<V>): V?

    public data class Push<V : Any>(val value: V) : StackInstruction<V> {
        override fun execute(context: ExecutionContext<V>): V = value
    }
}
