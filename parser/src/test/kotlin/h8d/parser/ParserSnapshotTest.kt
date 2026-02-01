package h8d.parser

import h8d.parser.testharness.shouldParseAndMatchSnapshot
import io.kotest.core.spec.style.FunSpec

// run by Kotest
@Suppress("unused")
internal class ParserSnapshotTest : FunSpec({
    test("print") {
        """print "Hello, World!"""".shouldParseAndMatchSnapshot()
    }
    test("empty") {
        "".shouldParseAndMatchSnapshot()
    }
    test("invalid") {
        """println("Hello, World!")""".shouldParseAndMatchSnapshot()
    }
})
