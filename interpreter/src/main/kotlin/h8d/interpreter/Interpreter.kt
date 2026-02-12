package h8d.interpreter

import h8d.parser.Program
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

/**
 * Interpreter can run only program at a time.
 */
public interface Interpreter {
    /**
     * Execute the program and stream the output from the instructions.
     * Output follows the order of the instructions in the program.
     *
     * Execution stops upon the completion or when the flow is no longer collected.
     */
    public fun execute(program: Program): Flow<String>
}

public fun Interpreter(
    parallelFactor: Int = 1,
    coroutineDispatcher: CoroutineDispatcher,
): Interpreter = SequentialInterpreter(parallelFactor, coroutineDispatcher)

public fun Interpreter.executeBlocking(program: Program): String =
    runBlocking {
        execute(program)
            .toList(mutableListOf())
            .joinToString(separator = "\n")
    }
