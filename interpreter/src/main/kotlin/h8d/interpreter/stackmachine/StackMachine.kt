package h8d.interpreter.stackmachine

import h8d.interpreter.memory.Address
import h8d.interpreter.memory.ScalarMemory

@Throws(InstructionException::class)
internal fun <V : Any, I : StackInstruction<V>> computeOnStackMachine(
    instructions: Iterable<I>,
    memory: ScalarMemory<V>,
): V? {
    val stack = SimpleStack<V>()
    for (i in instructions) {
        i.execute(stack, memory)?.also(stack::push)
    }
    return stack
        .takeIf { !it.isEmpty }
        ?.pop()
}

internal class InstructionException(message: String) : RuntimeException(message)

internal interface StackInstruction<V : Any> {
    @Throws(InstructionException::class)
    fun execute(stack: ValueStack<V>, memory: ScalarMemory<V>): V?

    interface ValueStack<V : Any> {
        fun pop(): V
    }

    data class Push<V : Any>(internal val value: V) : StackInstruction<V> {
        override fun execute(stack: ValueStack<V>, memory: ScalarMemory<V>): V = value
    }

    data class Load<V : Any>(private val address: Address) : StackInstruction<V> {
        override fun execute(stack: ValueStack<V>, memory: ScalarMemory<V>): V =
            memory.readValue(address)
    }
}

private class SimpleStack<V : Any> : StackInstruction.ValueStack<V> {
    private val list = mutableListOf<V>()

    override fun pop(): V {
        if (list.isEmpty()) throw InstructionException("Not enough arguments, stack is empty")
        return list.removeLast()
    }

    val isEmpty: Boolean get() = list.isEmpty()

    fun push(value: V) {
        list.add(value)
    }
}
