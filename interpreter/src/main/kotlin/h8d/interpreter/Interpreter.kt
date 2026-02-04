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
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

public interface Interpreter {
    public fun execute(program: Program): Flow<String>
}

public fun Interpreter(): Interpreter = SimpleInterpreter()

private class SimpleInterpreter : Interpreter {
    override fun execute(program: Program): Flow<String> =
        flow {
            val context = ExecutionContext()
            val instructions = mutableListOf<StackInstruction<VV>>()
            for (node in program.nodes) {
                instructions.addInstructions(node, context)
                // look ahead implementation for parallel execution can be implemented here
            }
            computeOnStackMachine(instructions, context.memory)
            emitAll(context.output)
        }
}

public fun Interpreter.executeBlocking(program: Program): String =
    runBlocking {
        execute(program)
            .toList(mutableListOf())
            .joinToString(separator = "\n")
    }

//internal suspend fun <T : Number> map(
//    sequence: Sequence<T>,
//    sequenceLength: Long,
//    tree: Any,
//    parallelizationFactor: Int,
//    coroutineContext: CoroutineContext,
//): Sequence<Number> = flow {
//
//    for (i in parallelizationFactor) {
//        sequence.take(sequenceLength / sequence)
//
//    }
//}

private fun MutableList<StackInstruction<VV>>.addInstructions(node: StatementNode, context: ExecutionContext) = apply {
    when (node) {
        is StatementNode.PrintNode -> {
            add(Push(node.value))
            add(II.Print(context))
        }
        is StatementNode.OutputNode -> {
            addInstructions(node.expression, context)
            add(II.Print(context))
        }
//        is StatementNode.VariableAssignmentNode -> node.expression.addInstructions(this, address)
        else -> Unit
    }
}

private fun MutableList<StackInstruction<VV>>.addInstructions(node: ExpressionNode, context: ExecutionContext) = apply {
    when (node) {
        is ExpressionNode.NumberLiteral -> add(Push(node.value))
//        is ExpressionNode.VariableReferenceNode -> add(Load(memory.getAddress(node.variableName)))
        else -> Unit
    }
}

private typealias VV = Any

internal interface II : StackInstruction<VV> {
    data class Store(private val address: Address) : II {
        override fun execute(stack: ValueStack<VV>, memory: ScalarMemory<VV>): VV? {
            memory.writeValue(address, stack.pop())
            return null
        }
    }

    data class Print(private val context: ExecutionContext) : II {
        override fun execute(stack: ValueStack<VV>, memory: ScalarMemory<VV>): VV? {
            context.output(stack.pop().toString())
            return null
        }
    }
}

internal class ExecutionContext {
    val memory: Memory = object : Memory {
        override fun write(address: Address, value: Sequence<Number>) {
            TODO("Not yet implemented")
        }

        override fun write(address: Address, value: Number) {
            TODO("Not yet implemented")
        }

        override fun extendSeq(address: Address, value: List<Number>) {
            TODO("Not yet implemented")
        }

        override fun isValueAvailable(address: Address): Boolean {
            TODO("Not yet implemented")
        }

        override fun <T> readSequence(address: Address): Sequence<T> {
            TODO("Not yet implemented")
        }

        override fun readValue(address: Address): Number {
            TODO("Not yet implemented")
        }

        override fun writeValue(address: Address, value: VV) {
            TODO("Not yet implemented")
        }

        override fun read(address: Address): Any {
            TODO()
        }

        override fun nextAddress(): Address {
            TODO("Not yet implemented")
        }

        fun getAddress(variableName: String): Address {
            TODO()
        }
    }

    private val outputLines = mutableListOf<String>()

    fun output(text: String) {
        outputLines.add(text)
    }

    val output: Flow<String> get() = flowOf(outputLines.joinToString(separator = ""))
}

internal interface Memory : ScalarMemory<VV> {
    fun write(address: Address, value: Sequence<Number>)

    fun write(address: Address, value: Number)

    fun extendSeq(address: Address, value: List<Number>)

    fun isValueAvailable(address: Address): Boolean

    fun <T> readSequence(address: Address): Sequence<T>

    fun read(address: Address): Any

    fun nextAddress(): Address
}

internal sealed interface Value<T : Any> {
    val value: T

    data class Scalar(override val value: Number) : Value<Number>
    data class PartialSeq(override val value: List<Number>) : Value<List<Number>>
    data class Seq(override val value: Sequence<Number>) : Value<Sequence<Number>>
}

internal sealed interface InterpreterInstruction {
    val requiredValues: Set<Long>

    fun execute(context: ExecutionContext)

    data class MapSeq(
        private val address: Address?,
        private val sequenceAddress: Address,
        private val expression: Any,
        override val requiredValues: Set<Long> = emptySet(),
    ) : InterpreterInstruction {
        override fun execute(context: ExecutionContext) {
            context.memory.readSequence<Number>(sequenceAddress)
        }
    }

    data class ReduceSeq(private val address: Address?, private val expression: Any) : InterpreterInstruction {
        override val requiredValues: Set<Long>
            get() = emptySet()

        override fun execute(context: ExecutionContext) {
            TODO("Not yet implemented")
        }
    }

    data class Seq(
        private val address: Address?,
        private val first: Long?,
        private val firstAddress: Address?,
        private val last: Long?,
        private val lastAddress: Address?,
    ) : InterpreterInstruction {
        override val requiredValues: Set<Long>
            get() = emptySet()

        override fun execute(context: ExecutionContext) {
            // TODO check boundaries
            address?.also { context.memory.write(it, emptySequence<Long>()) }
        }
    }

    data class Arithmetic(
        private val address: Address?,
        private val operation: Any,
        override val requiredValues: Set<Long>,
    ) : InterpreterInstruction {
        override fun execute(context: ExecutionContext) {
            address?.also {
                // compile
                // execute
                // write
                //ontext.memory.write(it, computeOnStackMachine(emptyList(), context.memory))
            }
        }
    }
}
