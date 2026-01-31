package h8d.parser

import h8d.parser.Program.StatementNode.PrintNode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

// run by Kotest
@Suppress("unused")
internal class ParserTest : FunSpec({
    test("print") {
        """print "Hello, World!"""" shouldParseTo PrintNode("Hello, World!")
    }
})

private infix fun String.shouldParseTo(expected: Program.StatementNode) {
    parse(this) shouldBeSingle expected
}

private infix fun ParseResult.shouldBeSingle(expected: Program.StatementNode) {
    shouldBeTypeOf<ParseResult.SuccessfulProgram>()
    this.program.nodes.shouldBeSingleton { it shouldBe expected }
}
