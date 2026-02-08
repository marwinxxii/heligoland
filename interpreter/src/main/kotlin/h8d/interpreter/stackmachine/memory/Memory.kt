package h8d.interpreter.stackmachine.memory

import h8d.stackmachine.ExecutionContext
import h8d.stackmachine.StackInstruction
import h8d.stackmachine.getExtension

@JvmInline
internal value class Address(val value: Long) {
    override fun toString(): String = "Address($value)"
}

internal interface Memory<V : Any> : ExecutionContext.Extension<V> {
    fun readValue(address: Address): V

    interface Writeable<V : Any> : Memory<V> {
        fun writeValue(address: Address, value: V)
    }
}

private fun <V : Any> ExecutionContext<V>.readFromMemory(address: Address): V =
    getExtension<V, Memory<V>>().readValue(address)

private fun <V : Any> ExecutionContext<V>.writeToMemory(address: Address, value: V) {
    getExtension<V, Memory.Writeable<V>>().writeValue(address, value)
}

internal data class Load<V : Any>(private val address: Address) : StackInstruction<V> {
    override fun execute(context: ExecutionContext<V>): V = context.readFromMemory(address)
}

/**
 * Store the value from the top of the stack in memory.
 */
internal data class Store<V : Any>(private val address: Address) : StackInstruction<V> {
    override fun execute(context: ExecutionContext<V>): V? {
        context.writeToMemory(address, context.stack.pop())
        return null
    }
}
