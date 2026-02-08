package h8d.stackmachine.arithmetic

import h8d.stackmachine.ExecutionContext
import h8d.stackmachine.InstructionException
import h8d.stackmachine.StackInstruction
import kotlin.math.pow

public sealed interface ArithmeticInstruction : StackInstruction<Number> {
    public data object Add : ArithmeticInstruction {
        override fun execute(context: ExecutionContext<Number>): Number =
            binaryOperation(
                context.stack,
                forLong = { left, right -> left + right },
                forDouble = { left, right -> left + right },
            )
    }

    public data object Subtract : ArithmeticInstruction {
        override fun execute(context: ExecutionContext<Number>): Number =
            binaryOperation(
                context.stack,
                forLong = { left, right -> left - right },
                forDouble = { left, right -> left - right },
            )
    }

    public data object Multiply : ArithmeticInstruction {
        override fun execute(context: ExecutionContext<Number>): Number =
            binaryOperation(
                context.stack,
                forLong = { left, right -> left * right },
                forDouble = { left, right -> left * right },
            )
    }

    public data object Divide : ArithmeticInstruction {
        override fun execute(context: ExecutionContext<Number>): Number =
            binaryOperation(
                context.stack,
                forLong = { left, right -> left / right },
                forDouble = { left, right -> left / right },
            )
    }

    /**
     * [Exponentiation](https://en.wikipedia.org/wiki/Exponentiation) operation.
     */
    public data object RaiseToPower : ArithmeticInstruction {
        override fun execute(context: ExecutionContext<Number>): Number =
            binaryOperation(
                context.stack,
                forLong = { left, right -> left.toDouble().pow(right.toDouble()).toLong() },
                forDouble = { left, right -> left.pow(right) },
            )
    }
}

@Throws(InstructionException::class)
private inline fun ArithmeticInstruction.binaryOperation(
    stack: ExecutionContext.ValueStack<Number>,
    forLong: (left: Long, right: Long) -> Long,
    forDouble: (left: Double, right: Double) -> Double,
): Number {
    val right = stack.pop()
    val left = stack.pop()
    return when {
        (left is Double) || (right is Double) -> forDouble(left.toDouble(), right.toDouble())
        left is Long && right is Long -> forLong(left, right)
        else -> throw InstructionException(
            instruction = this,
            message = "Unsupported argument types: $left(${left::class}) $this $right(${right::class})"
        )
    }
}
