package h8d.parser

import h8d.parser.testharness.shouldMatchParseResultSnapshot
import io.kotest.core.spec.style.FunSpec

// run by Kotest
@Suppress("unused")
internal class ParserSnapshotTest : FunSpec({
    test("print") {
        """print "Hello, World!"""".shouldMatchParseResultSnapshot()
    }
    test("empty") {
        "".shouldMatchParseResultSnapshot()
    }
    test("unknown function") {
        """println("Hello, World!")""".shouldMatchParseResultSnapshot()
    }
})
