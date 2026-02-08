package h8d.interpreter

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

/**
 * Interpreter which executes all instructions sequentially using
 * a hybrid stack machine with addressable memory.
 */
internal class SequentialInterpreter : Interpreter {
    override fun execute(program: Program): Flow<String> =
        flow {
            val output = OutputHolder<Value>()
            val context = ExecutionContext(MapMemory(), output)
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
        statements.forEach { addInstructions(it, addressHolder) }
    }

private typealias MutableInstructionsList = MutableList<StackInstruction<Value>>

// Traversing the tree depth first and adding instructions to the stack
private fun MutableInstructionsList.addInstructions(
    node: StatementNode,
    addressHolder: AddressHolder,
) = apply {
    when (node) {
        is StatementNode.PrintNode -> {
            add(Push(node.value))
            add(InterpreterInstruction.Print)
        }

        is StatementNode.OutputNode -> {
            addInstructions(node.expression, addressHolder)
            add(InterpreterInstruction.Print)
        }

        is StatementNode.VariableAssignmentNode -> {
            addInstructions(node.expression, addressHolder)
            add(Store(addressHolder.allocateAddress(node.variableName)))
        }

        is ExpressionNode -> addInstructions(node, addressHolder)
    }
}

private fun MutableInstructionsList.addInstructions(
    node: ExpressionNode,
    addressHolder: AddressHolder,
): MutableInstructionsList = apply {
    when (node) {
        is ExpressionNode.NumberLiteral -> add(Push(node.value))

        is ExpressionNode.VariableReferenceNode ->
            addressHolder
                .resolveAddressOfVariable(node.variableName)
                .also { add(Load(it)) }

        is ExpressionNode.SeqNode -> {
            addInstructions(node.first, addressHolder)
            addInstructions(node.last, addressHolder)
            add(InterpreterInstruction.Seq)
        }

        is BinaryOperationNode -> {
            addInstructions(node.left, addressHolder)
            addInstructions(node.right, addressHolder)
            addInstruction(node.operation)
        }

        is ExpressionNode.FunctionCallNode.MapCallNode -> {
            addInstructions(node.sequence, addressHolder)
            // TODO pass variable
            add(InterpreterInstruction.MapSeq(node.lambda.body))
        }

        is ExpressionNode.FunctionCallNode.ReduceCallNode -> {
            addInstructions(node.sequence, addressHolder)
            addInstructions(node.accumulator, addressHolder)
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

internal interface InterpreterInstruction : StackInstruction<Value> {

    /**
     * Send the value from the top of the stack to the output.
     */
    data object Print : InterpreterInstruction {
        override fun execute(context: ExecutionContext<Value>): Value? {
            when (val v = context.stack.pop()) {
                is Sequence<*> -> v.joinToString(separator = ", ", prefix = "[", postfix = "]")
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
//            return context.executeInParallel(
//                expression = expression,
//                sequence = sequence,
//            ) as Number
            return 0
        }
    }

    /**
     * Compute the value from the sequence by reducing it to a single element.
     */
    class ReduceSeq(private val expression: ExpressionNode) : InterpreterInstruction {
        // TODO runtime type checks
        @Suppress("UNCHECKED_CAST")
        override fun execute(context: ExecutionContext<Value>): Value? {
            // delegate computation of arithmetic instructions to parallel executor (if applicable)
            // push the result number back to stack
            val accumulator = context.stack.pop() as Number
            val sequence = context.stack.pop() as Sequence<Number>
            return 0
        }
    }
}

//internal class ExecutionContext {
//    fun executeInParallel(
//        expression: ExpressionNode,
//        sequence: Sequence<Number>,
//        arguments: Map<String, Number>,
//    ): Value {
//        val subContext = ExecutionContext()
//        val instructions = mutableListOf<StackInstruction<Value>>().apply {
//            addInstructions(expression, context = subContext)
//        }
//        return sequence.map { argumentValue ->
//            computeOnStackMachine(
//                instructions,
//                ScalarMemory.readOnly(arguments),
//            )
//        }
//    }
//}
