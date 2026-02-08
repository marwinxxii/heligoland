package h8d.interpreter

import h8d.interpreter.testharness.shouldExecuteAndOutput
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests

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
    """
        out map({0, 10}, i -> i+1)
    """,
    """
        out reduce({10, 20}, 100, a b -> a+b)
    """,
)

// run by Kotest
@Suppress("unused")
internal class InterpreterTest : FunSpec({
    context("program execution and output") {
        withTests(
            nameFn =  { testName(it, programs) },
            programs,
        ) { it.shouldExecuteAndOutput() }
    }
})

private fun testName(program: String, programs: List<String>): String {
    // https://github.com/diffplug/selfie/issues/540
    val index = ('a' + (programs.indexOf(program) % 'a'.code))
        .toString()
    // TODO handle more than 26 tests
    val p = program.trimIndent().trim()
    return if (p.contains('\n')) {
        index
    } else {
        "$index: $p"
    }
}
