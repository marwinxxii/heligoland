package h8d.interpreter

import h8d.parser.ParseResult
import h8d.parser.parse
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

// run by Kotest
@Suppress("unused")
internal class InterpreterTest : ShouldSpec({
    should("print hello world") {
        """print "Hello, World!"""" shouldOutput "Hello, World!"
    }
})

private infix fun String.shouldOutput(singleLine: String) =
    parse(this)
        .let { it as ParseResult.SuccessfulProgram }
        .program
        .let(Interpreter()::executeBlocking)
        .shouldBe(singleLine)
