package h8d.interpreter.stackmachine

import h8d.interpreter.memory.ScalarMemory
import h8d.interpreter.stackmachine.StackInstruction.ValueStack
import kotlin.math.pow

internal sealed interface ArithmeticInstruction : StackInstruction<Number> {
    data object Add : ArithmeticInstruction {
        override fun execute(stack: ValueStack<Number>, memory: ScalarMemory<Number>): Number =
            binaryOperation(
                stack,
                forLong = { left, right -> left + right },
                forDouble = { left, right -> left + right },
            )
    }

    data object Subtract : ArithmeticInstruction {
        override fun execute(stack: ValueStack<Number>, memory: ScalarMemory<Number>): Number =
            binaryOperation(
                stack,
                forLong = { left, right -> left - right },
                forDouble = { left, right -> left - right },
            )
    }

    data object Multiply : ArithmeticInstruction {
        override fun execute(stack: ValueStack<Number>, memory: ScalarMemory<Number>): Number =
            binaryOperation(
                stack,
                forLong = { left, right -> left * right },
                forDouble = { left, right -> left * right },
            )
    }

    data object Divide : ArithmeticInstruction {
        override fun execute(stack: ValueStack<Number>, memory: ScalarMemory<Number>): Number =
            binaryOperation(
                stack,
                forLong = { left, right -> left / right },
                forDouble = { left, right -> left / right },
            )
    }

    /**
     * [Exponentiation](https://en.wikipedia.org/wiki/Exponentiation) operation.
     */
    data object RaiseToPower : ArithmeticInstruction {
        override fun execute(stack: ValueStack<Number>, memory: ScalarMemory<Number>): Number =
            binaryOperation(
                stack,
                forLong = { left, right -> left.toDouble().pow(right.toDouble()).toLong() },
                forDouble = { left, right -> left.pow(right) },
            )
    }
}

@Throws(InstructionException::class)
private inline fun ArithmeticInstruction.binaryOperation(
    stack: ValueStack<Number>,
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
