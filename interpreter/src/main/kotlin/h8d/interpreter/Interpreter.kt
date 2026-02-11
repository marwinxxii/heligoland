package h8d.interpreter

import h8d.parser.Program
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

public interface Interpreter {
    /**
     * Execute the program and stream the output from the instructions.
     * Output follows the order of the instructions in the program.
     */
    public fun execute(program: Program): Flow<String>
}

public fun Interpreter(parallelFactor: Int = 1): Interpreter = SequentialInterpreter(parallelFactor)

public fun Interpreter.executeBlocking(program: Program): String =
    runBlocking {
        execute(program)
            .toList(mutableListOf())
            .joinToString(separator = "\n")
    }
