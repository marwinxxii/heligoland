package h8d.interpreter.stackmachine

import h8d.interpreter.memory.Address
import h8d.interpreter.memory.ScalarMemory
import kotlin.math.pow

@Throws(InstructionException::class)
internal fun computeOnStackMachine(instructions: Iterable<StackInstruction>, memory: ScalarMemory): Number {
    val stack = SimpleStack()
    for (i in instructions) {
        stack.push(i.execute(stack, memory))
    }
    return stack.pop()
}

internal class InstructionException(message: String) : RuntimeException(message)

internal sealed interface StackInstruction {
    @Throws(InstructionException::class)
    fun execute(stack: ValueStack, memory: ScalarMemory): Number

    interface ValueStack {
        fun pop(): Number
    }

    data class PushLong(val value: Long) : StackInstruction {
        override fun execute(stack: ValueStack, memory: ScalarMemory): Number = value
    }

    data class PushDouble(val value: Double) : StackInstruction {
        override fun execute(stack: ValueStack, memory: ScalarMemory): Number = value
    }

    data class Load(private val address: Address) : StackInstruction {
        override fun execute(stack: ValueStack, memory: ScalarMemory): Number = memory.readNumber(address)
    }

    data object Add : StackInstruction {
        override fun execute(stack: ValueStack, memory: ScalarMemory): Number =
            binaryOperation(
                stack,
                forLong = { left, right -> left + right },
                forDouble = { left, right -> left + right },
            )
    }

    data object Subtract : StackInstruction {
        override fun execute(stack: ValueStack, memory: ScalarMemory): Number =
            binaryOperation(
                stack,
                forLong = { left, right -> left - right },
                forDouble = { left, right -> left - right },
            )
    }

    data object Multiply : StackInstruction {
        override fun execute(stack: ValueStack, memory: ScalarMemory): Number =
            binaryOperation(
                stack,
                forLong = { left, right -> left * right },
                forDouble = { left, right -> left * right },
            )
    }

    data object Divide : StackInstruction {
        override fun execute(stack: ValueStack, memory: ScalarMemory): Number =
            binaryOperation(
                stack,
                forLong = { left, right -> left / right },
                forDouble = { left, right -> left / right },
            )
    }

    /**
     * [Exponentiation](https://en.wikipedia.org/wiki/Exponentiation) operation.
     */
    data object RaiseToPower : StackInstruction {
        override fun execute(stack: ValueStack, memory: ScalarMemory): Number =
            binaryOperation(
                stack,
                forLong = { left, right -> left.toDouble().pow(right.toDouble()).toLong() },
                forDouble = { left, right -> left.pow(right) },
            )
    }
}

@Throws(InstructionException::class)
private inline fun StackInstruction.binaryOperation(
    stack: StackInstruction.ValueStack,
    forLong: (left: Long, right: Long) -> Long,
    forDouble: (left: Double, right: Double) -> Double,
): Number {
    val right = stack.pop()
    val left = stack.pop()
    return when {
        (left is Double) || (right is Double) -> forDouble(left.toDouble(), right.toDouble())
        (left is Long) || (right is Long) -> forLong(left.toLong(), right.toLong())
        else -> throw InstructionException(
            "Unsupported argument types: $left(${left::class}) $this $right(${right::class})"
        )
    }
}

private class SimpleStack : StackInstruction.ValueStack {
    private val list = mutableListOf<Number>()

    override fun pop(): Number {
        if (list.isEmpty()) throw InstructionException("Not enough arguments, stack is empty")
        return list.removeLast()
    }

    fun push(value: Number) {
        list.add(value)
    }
}
