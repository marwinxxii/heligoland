package h8d.parser

import h8d.parser.testharness.shouldMatchParsingErrorsSnapshot
import h8d.parser.testharness.shouldMatchTreeNodesSnapshot
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests

private val invalidPrograms = listOf(
    "",
    """
        println("Hello, World!")
    """,
    "var a = {1.0, 9.0}",
)
private val validPrograms = listOf(
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
        out map({0, 10}, i -> i+1)
    """,
    """
        out reduce({0, 10}, 0, a b -> a+b)
    """,
)
// run by Kotest
@Suppress("unused")
internal class ParserSnapshotTest : FunSpec({
    context("Invalid programs") {
        withTests(
            nameFn = { testName(it, invalidPrograms) },
            invalidPrograms,
        ) { it.shouldMatchParsingErrorsSnapshot() }
    }
    context("Valid programs") {
        withTests(
            nameFn = { testName(it, validPrograms) },
            validPrograms,
        ) { it.shouldMatchTreeNodesSnapshot() }
    }
})

private fun testName(p: String, programs: List<String>): String {
    // https://github.com/diffplug/selfie/issues/540
    val index = ('a' + (programs.indexOf(p) % 'a'.code)).toString()
    // TODO handle more than 26 tests
    val program = p.trimIndent().trim()
    return if (program.contains('\n')) {
        index
    } else {
        "$index: $program"
    }
}
