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
)
// run by Kotest
@Suppress("unused")
internal class ParserSnapshotTest : FunSpec({
    context("Invalid programs") {
        withTests(
            nameFn = { programName(it, invalidPrograms) },
            invalidPrograms,
        ) { it.shouldMatchParsingErrorsSnapshot() }
    }
    context("Valid programs") {
        withTests(
            nameFn = { programName(it, validPrograms) },
            validPrograms,
        ) { it.shouldMatchTreeNodesSnapshot() }
    }
})

private fun programName(p: String, programs: List<String>): String {
    val index = programs.indexOf(p)
    val program = p.trimIndent().trim()
    return if (program.contains('\n')) {
        "$index"
    } else {
        "$index: $program"
    }
}
