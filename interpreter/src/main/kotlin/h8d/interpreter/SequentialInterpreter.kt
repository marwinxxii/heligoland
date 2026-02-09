package h8d.interpreter

import h8d.interpreter.stackmachine.sequence.SequenceEvaluationStrategy
import h8d.interpreter.stackmachine.sequence.evaluateToIterable
import h8d.interpreter.stackmachine.memory.AddressHolder
import h8d.interpreter.stackmachine.memory.Load
import h8d.interpreter.stackmachine.memory.MapMemory
import h8d.interpreter.stackmachine.memory.Store
import h8d.interpreter.stackmachine.output.OutputHolder
import h8d.interpreter.stackmachine.output.output
import h8d.parser.Program
import h8d.parser.Program.StatementNode
import h8d.parser.Program.StatementNode.ExpressionNode
import h8d.parser.Program.StatementNode.ExpressionNode.BinaryOperationNode
import h8d.stackmachine.ExecutionContext
import h8d.stackmachine.StackInstruction
import h8d.stackmachine.StackInstruction.Push
import h8d.stackmachine.arithmetic.ArithmeticInstruction
import h8d.stackmachine.computeStack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

/**
 * Interpreter which executes all instructions sequentially using
 * a hybrid stack machine with addressable memory.
 */
internal class SequentialInterpreter : Interpreter {
    override fun execute(program: Program): Flow<String> =
        flow {
            val output = OutputHolder<Value>()
            val context = ExecutionContext(
                MapMemory(),
                output,
                SequenceEvaluationStrategy.sequential(),
            )
            val instructions = compileToInstructions(program.nodes, AddressHolder())
            computeStack(instructions, context) { _, _, _ ->
                output.consume()?.also { emit(it) }
            }
        }
}

private fun compileToInstructions(
    statements: List<StatementNode>,
    addressHolder: AddressHolder,
): List<StackInstruction<Value>> =
    mutableListOf<StackInstruction<Value>>().apply {
        val onReadVariable = { node: ExpressionNode.VariableReferenceNode ->
            addressHolder
                .resolveAddressOfVariable(node.variableName)
                .let { Load<Value>(it) }
        }
        statements.forEach { addInstructions(it, addressHolder, onReadVariable) }
    }

private typealias MutableInstructionsList = MutableList<StackInstruction<Value>>

// Traversing the tree depth first and adding instructions to the stack
private fun MutableInstructionsList.addInstructions(
    node: StatementNode,
    addressHolder: AddressHolder,
    onReadVariable: (ExpressionNode.VariableReferenceNode) -> StackInstruction<Value>,
) = apply {
    when (node) {
        is StatementNode.PrintNode -> {
            add(Push(node.value))
            add(InterpreterInstruction.Print)
        }

        is StatementNode.OutputNode -> {
            addInstructions(node.expression, onReadVariable)
            add(InterpreterInstruction.Print)
        }

        is StatementNode.VariableAssignmentNode -> {
            addInstructions(node.expression, onReadVariable)
            add(Store(addressHolder.allocateAddress(node.variableName)))
        }

        is ExpressionNode -> addInstructions(node, onReadVariable)
    }
}

private fun MutableInstructionsList.addInstructions(
    node: ExpressionNode,
    onReadVariable: (ExpressionNode.VariableReferenceNode) -> StackInstruction<Value>,
): MutableInstructionsList = apply {
    when (node) {
        is ExpressionNode.NumberLiteral -> add(Push(node.value))

        is ExpressionNode.VariableReferenceNode -> add(onReadVariable(node))

        is ExpressionNode.SeqNode -> {
            addInstructions(node.first, onReadVariable)
            addInstructions(node.last, onReadVariable)
            add(InterpreterInstruction.Seq)
        }

        is BinaryOperationNode -> {
            addInstructions(node.left, onReadVariable)
            addInstructions(node.right, onReadVariable)
            addInstruction(node.operation)
        }

        is ExpressionNode.FunctionCallNode.MapCallNode -> {
            addInstructions(node.sequence, onReadVariable)
            add(InterpreterInstruction.MapSeq(node.lambda.body))
        }

        is ExpressionNode.FunctionCallNode.ReduceCallNode -> {
            addInstructions(node.sequence, onReadVariable)
            addInstructions(node.accumulator, onReadVariable)
            // TODO pass arguments
            add(InterpreterInstruction.ReduceSeq(node.lambda.body))
        }
    }
}

private fun MutableInstructionsList.addInstruction(
    operation: BinaryOperationNode.Operation,
) = apply {
    when (operation) {
        BinaryOperationNode.Operation.ADDITION -> ArithmeticInstruction.Add
        BinaryOperationNode.Operation.SUBTRACTION -> ArithmeticInstruction.Subtract
        BinaryOperationNode.Operation.MULTIPLICATION -> ArithmeticInstruction.Multiply
        BinaryOperationNode.Operation.DIVISION -> ArithmeticInstruction.Divide
        BinaryOperationNode.Operation.EXPONENTIATION -> ArithmeticInstruction.RaiseToPower
    }.also {
        // TODO fix the types
        @Suppress("UNCHECKED_CAST")
        add(it as StackInstruction<Value>)
    }
}

private typealias Value = Any

internal sealed interface InterpreterInstruction : StackInstruction<Value> {
    /**
     * Send the value from the top of the stack to the output.
     */
    data object Print : InterpreterInstruction {
        override fun execute(context: ExecutionContext<Value>): Value? {
            @Suppress("UNCHECKED_CAST")
            when (val v = context.stack.pop()) {
                // move parallelisation to context extension?
                is Sequence<*> -> context
                    .evaluateToIterable(v as Sequence<Any>)
                    .joinToString(separator = ", ", prefix = "[", postfix = "]")

                else -> v.toString()
            }
                .also(context::output)

            return null
        }
    }

    /**
     * Generate sequence object.
     */
    data object Seq : InterpreterInstruction {
        override fun execute(context: ExecutionContext<Value>): Sequence<Long> {
            val last = context.stack.pop() as Long
            val first = context.stack.pop() as Long
            return (first..last).asSequence()
        }
    }

    /**
     * Transform one sequence to another by mapping old values to new
     * ones using the specified arithmetic expression.
     */
    class MapSeq(private val expression: ExpressionNode) : InterpreterInstruction {
        // TODO runtime type checks
        @Suppress("UNCHECKED_CAST")
        override fun execute(context: ExecutionContext<Value>): Value? {
            val sequence = context.stack.pop() as Sequence<Number>
            return sequence.map {
                // TODO can be pre-compiled
                val instructions = mutableListOf<StackInstruction<Value>>(Push(it))
                instructions.addInstructions(
                    expression,
                    onReadVariable = { Copy(argumentIndex = 0) },
                )
                runBlocking {
                    computeStack(
                        instructions,
                        ExecutionContext(context.extensions),
                    ).pop()
                }
            }
        }
    }

    /**
     * Compute the value from the sequence by reducing it to a single element.
     */
    class ReduceSeq(private val expression: ExpressionNode) : InterpreterInstruction {
        // TODO runtime type checks
        @Suppress("UNCHECKED_CAST")
        override fun execute(context: ExecutionContext<Value>): Value? {
            val accumulator = context.stack.pop() as Number
            val sequence = context.stack.pop() as Sequence<Number>
//            return context.evaluateToIterable(sequence)
//                .fold(accumulator) { accumulator, item -> accumulator }
            return 0
        }
    }

    /**
     * Copy value from the bottom of the stack + [argumentIndex] to the top.
     */
    data class Copy(private val argumentIndex: Int) : InterpreterInstruction {
        override fun execute(context: ExecutionContext<Value>): Value? =
            context.stack.peek(argumentIndex)
    }
}
