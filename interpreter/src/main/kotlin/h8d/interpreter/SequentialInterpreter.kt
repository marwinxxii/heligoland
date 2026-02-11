package h8d.interpreter

import h8d.interpreter.stackmachine.memory.AddressHolder
import h8d.interpreter.stackmachine.memory.Load
import h8d.interpreter.stackmachine.memory.MapMemory
import h8d.interpreter.stackmachine.memory.Store
import h8d.interpreter.stackmachine.output.OutputHolder
import h8d.interpreter.stackmachine.output.output
import h8d.interpreter.stackmachine.sequence.IndexedSequence
import h8d.interpreter.stackmachine.sequence.LazySequence
import h8d.interpreter.stackmachine.sequence.PredefinedNumberSequence
import h8d.interpreter.stackmachine.sequence.SequenceComputationStrategy
import h8d.interpreter.stackmachine.sequence.computeToIterable
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
internal class SequentialInterpreter(private val parallelFactor: Int) : Interpreter {
    private val computationStrategy by lazy {
        SequenceComputationStrategy.parallel<Value>(parallelFactor = parallelFactor)
    }

    override fun execute(program: Program): Flow<String> =
        flow {
            val output = OutputHolder<Value>()
            val context = ExecutionContext(
                MapMemory(),
                output,
                computationStrategy,
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
            InterpreterInstruction.ReduceSeq(
                expression = node.lambda.body,
                accumulatorArgumentName = node.lambda.accumulatorArgument.variableName,
            ).also(::add)
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
                is IndexedSequence<*> -> context
                    .computeToIterable(v as IndexedSequence<Any>)
                    .joinToString(separator = ", ", prefix = "[", postfix = "]")

                else -> v.toString()
            }
                .also(context::output)

            return null
        }
    }

    /**
     * Generate a sequence of [Long] numbers.
     */
    data object Seq : InterpreterInstruction {
        override fun execute(context: ExecutionContext<Value>): PredefinedNumberSequence {
            val last = context.stack.pop() as Long
            val first = context.stack.pop() as Long
            return PredefinedNumberSequence(first = first, last = last)
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
            val sequence = context.stack.pop() as IndexedSequence<Number>
            return LazySequence(sequence) {
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
    class ReduceSeq(
        private val expression: ExpressionNode,
        private val accumulatorArgumentName: String,
    ) : InterpreterInstruction {
        // TODO runtime type checks
        @Suppress("UNCHECKED_CAST")
        override fun execute(context: ExecutionContext<Value>): Value? {
            val accumulator = context.stack.pop() as Number
            val sequence = context.stack.pop() as IndexedSequence<Number>
            return context
                .computeToIterable(sequence)
                .fold(accumulator) { accumulator, item ->
                    val instructions = mutableListOf<StackInstruction<Value>>(
                        Push(accumulator),
                        Push(item),
                    )
                    instructions.addInstructions(
                        expression,
                        onReadVariable = {
                            Copy(
                                if (it.variableName == accumulatorArgumentName) {
                                    0
                                } else {
                                    1
                                }
                            )
                        },
                    )
                    runBlocking {
                        computeStack(
                            instructions,
                            ExecutionContext(context.extensions),
                        ).pop() as Number
                    }
                }
        }
    }

    /**
     * Copy value from the bottom of the stack + [argumentIndex] to the top.
     *
     * @param argumentIndex-index/offset from the bottom of the stack
     * where the target value to copy is. Starts with zero.
     */
    data class Copy(private val argumentIndex: Int) : InterpreterInstruction {
        override fun execute(context: ExecutionContext<Value>): Value? =
            context.stack.peek(argumentIndex)
    }
}
