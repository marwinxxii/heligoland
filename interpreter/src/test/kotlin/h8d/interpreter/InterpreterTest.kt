package h8d.interpreter

import h8d.interpreter.testharness.shouldExecuteAndOutput
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData

// run by Kotest
@Suppress("unused")
internal class InterpreterTest : FunSpec({
    context("program execution and output") {
        listOf(
            """print "Hello, World!"""",
            "out 100",
        )
            .also { programs ->
                withData(programs) { it.shouldExecuteAndOutput() }
            }
    }
})
