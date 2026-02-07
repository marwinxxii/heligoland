package h8d.interpreter

import h8d.interpreter.testharness.shouldExecuteAndOutput
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData

private val programs = listOf(
    """
        print "Hello, World!"
    """,
    "out 100",
    "var a = 20",
    """
        var b = 11
        out b
    """,
    "out {0, 10}",
    """
        var f = 1
        out {f, 10}
    """,
    """
        var f = 100
        var l = 123
        out {f, l}
    """,
    """
        out 10+20
    """,
    """
        out 10-20
    """,
)

// run by Kotest
@Suppress("unused")
internal class InterpreterTest : FunSpec({
    // see interpreter/src/test/kotlin/program execution and output.ss
    // for snapshot
    context("program execution and output") {
        withData(
            nameFn = { p ->
                val index = programs.indexOf(p)
                val program = p.trimIndent().trim()
                if (program.contains('\n')) {
                    "$index"
                } else {
                    "$index: $program"
                }
            },
            programs,
        ) { it.shouldExecuteAndOutput() }
    }
})
