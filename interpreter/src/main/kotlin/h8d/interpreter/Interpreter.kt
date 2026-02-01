package h8d.interpreter

import h8d.interpreter.memory.Address
import h8d.interpreter.memory.ScalarMemory
import h8d.interpreter.stackmachine.computeOnStackMachine
import h8d.parser.Program
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
            for (node in program.nodes) {
                // create a list of instructions
                // iterate over them
                // wait till next instruction is evaluated
                node.toInstructions().forEach { it.execute(context) }
                // TODO check look ahead limit
            }
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

private fun Program.StatementNode.toInstructions(): List<InterpreterInstruction> =
    when (this) {
        is Program.StatementNode.PrintNode -> InterpreterInstruction.PrintString(this.value)
        else -> null
    }.let(::listOfNotNull)

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

        override fun readNumber(address: Address): Number {
            TODO("Not yet implemented")
        }

    }

    private val outputLines = mutableListOf<String>()

    fun output(text: String) {
        outputLines.add(text)
    }

    val output: Flow<String> get() = flowOf(outputLines.joinToString(separator = ""))
}

internal interface Memory : ScalarMemory {
    fun write(address: Address, value: Sequence<Number>)

    fun write(address: Address, value: Number)

    fun extendSeq(address: Address, value: List<Number>)

    fun isValueAvailable(address: Address): Boolean

    fun <T> readSequence(address: Address): Sequence<T>
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

    data class PrintValue(
        private val address: Address,
        override val requiredValues: Set<Long>,
    ) : InterpreterInstruction {
        override fun execute(context: ExecutionContext) {
            context.output(context.memory.readNumber(address).toString())
        }
    }

    data class PrintString(private val value: String) : InterpreterInstruction {
        override val requiredValues: Set<Long> get() = emptySet()

        override fun execute(context: ExecutionContext) {
            context.output(value)
        }
    }

    data class MapSeq(
        private val address: Address?,
        private val sequenceAddress: Address,
        private val expression: Any,
        override val requiredValues: Set<Long>,
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
                context.memory.write(it, computeOnStackMachine(emptyList(), context.memory))
            }
        }
    }
}
