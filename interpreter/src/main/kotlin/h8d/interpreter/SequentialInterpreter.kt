package h8d.interpreter

import h8d.interpreter.memory.Address
import h8d.interpreter.memory.ScalarMemory
import h8d.interpreter.stackmachine.StackInstruction
import h8d.interpreter.stackmachine.StackInstruction.Push
import h8d.interpreter.stackmachine.StackInstruction.ValueStack
import h8d.interpreter.stackmachine.computeOnStackMachine
import h8d.parser.Program
import h8d.parser.Program.StatementNode
import h8d.parser.Program.StatementNode.ExpressionNode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * Interpreter which executes all instructions sequentially using
 * a hybrid stack machine with addressable memory.
 */
internal class SequentialInterpreter : Interpreter {
    override fun execute(program: Program): Flow<String> =
        flow {
            val context = ExecutionContext()
            val instructions = mutableListOf<StackInstruction<Value>>()
            for (node in program.nodes) {
                instructions.addInstructions(node, context)
                // look ahead implementation for parallel execution can be implemented here
            }
            computeOnStackMachine(instructions, context.memory)
            // TODO emit updates on the fly
            emitAll(context.output)
        }
}

// Traversing the tree depth first and adding instructions to the stack
private fun MutableList<StackInstruction<Value>>.addInstructions(
    node: StatementNode,
    context: ExecutionContext,
) = apply {
    when (node) {
        is StatementNode.PrintNode -> {
            add(Push(node.value))
            add(InterpreterInstruction.Print(context))
        }

        is StatementNode.OutputNode -> {
            addInstructions(node.expression, context)
            add(InterpreterInstruction.Print(context))
        }

        is StatementNode.VariableAssignmentNode -> {
            addInstructions(node.expression, context)
            add(InterpreterInstruction.Store(context.memory.allocateAddress(node.variableName)))
        }

        is ExpressionNode -> addInstructions(node, context)
    }
}

private fun MutableList<StackInstruction<Value>>.addInstructions(node: ExpressionNode, context: ExecutionContext) =
    apply {
        when (node) {
            is ExpressionNode.NumberLiteral -> add(Push(node.value))
//        is ExpressionNode.VariableReferenceNode -> add(Load(memory.getAddress(node.variableName)))
            else -> TODO("Implement the rest of expressions")
        }
    }

private typealias Value = Any

internal interface InterpreterInstruction : StackInstruction<Value> {
    /**
     * Store the value from the top of the stack in memory.
     */
    data class Store(private val address: Address) : InterpreterInstruction {
        override fun execute(stack: ValueStack<Value>, memory: ScalarMemory<Value>): Value? {
            memory.writeValue(address, stack.pop())
            return null
        }
    }

    /**
     * Send the value from the top of the stack to the output.
     */
    data class Print(private val context: ExecutionContext) : InterpreterInstruction {
        override fun execute(stack: ValueStack<Value>, memory: ScalarMemory<Value>): Value? {
            context.output(stack.pop().toString())
            return null
        }
    }

    /**
     * Generate sequence object.
     */
    data object Seq : InterpreterInstruction {
        override fun execute(
            stack: ValueStack<Value>,
            memory: ScalarMemory<Value>,
        ): Value? {
            // read 2 values from stack and push the sequence
            return null
        }
    }

    /**
     * Single instruction which encapsulates arithmetic computations.
     * Is executed on a separate stack, can be parallelised.
     */
    data class Arithmetic(
        private val operation: Any,
    ) : InterpreterInstruction {
        override fun execute(
            stack: ValueStack<Value>,
            memory: ScalarMemory<Value>,
        ): Value? {
            // computeOnStackMachine
            // push the value on stack
            return null
        }
    }

    /**
     * Transform one sequence to another by mapping old values to new
     * ones using the specified arithmetic expression.
     */
    data class MapSeq(private val expression: Any) : InterpreterInstruction {
        override fun execute(
            stack: ValueStack<Value>,
            memory: ScalarMemory<Value>,
        ): Value? {
            // delegate computation of arithmetic instructions to parallel executor
            // push the result sequence back to stack
            return null
        }
    }

    /**
     * Compute the value from the sequence by reducing it to a single element.
     */
    data class ReduceSeq(private val expression: Any) : InterpreterInstruction {
        override fun execute(
            stack: ValueStack<Value>,
            memory: ScalarMemory<Value>,
        ): Value? {
            // delegate computation of arithmetic instructions to parallel executor (if applicable)
            // push the result number back to stack
            return null
        }
    }
}

internal class ExecutionContext {
    val memory: Memory = MapMemory()

    private val outputLines = mutableListOf<String>()

    fun output(text: String) {
        outputLines.add(text)
    }

    val output: Flow<String>
        get() = flowOf(outputLines.joinToString(separator = ""))
}

internal interface Memory : ScalarMemory<Value> {
    // fun extendSeq(address: Address, value: List<Number>)

    fun isValueAvailable(address: Address): Boolean

    fun nextAddress(): Address

    fun allocateAddress(variableName: String): Address
}

// TODO make it thread safe?
private class MapMemory : Memory {
    private val variables = mutableMapOf<String, Address>()
    private val values = mutableMapOf<Address, Value>()
    private var addressPointer = 0L

    override fun isValueAvailable(address: Address): Boolean =
        values.containsKey(address)

    override fun nextAddress(): Address {
        return Address(addressPointer++)
    }

    override fun allocateAddress(variableName: String): Address =
        variables.getOrPut(variableName, ::nextAddress)

    override fun readValue(address: Address): Value =
        requireNotNull(values[address])

    override fun writeValue(address: Address, value: Value) {
        values[address] = value
    }
}
