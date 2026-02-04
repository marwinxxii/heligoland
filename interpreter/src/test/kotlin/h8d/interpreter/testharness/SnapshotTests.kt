package h8d.interpreter.testharness

import com.diffplug.selfie.coroutines.expectSelfie
import h8d.interpreter.Interpreter
import h8d.parser.ParseResult
import h8d.parser.parseProgram
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.toList

internal suspend fun String.shouldExecuteAndOutput() {
    parseProgram(this.trimIndent())
        .shouldBeInstanceOf<ParseResult.SuccessfulProgram>()
        .program
        .let(Interpreter()::execute)
        .toList()
        .joinToString("\n")
        .also { shouldMatchSnapshot(actual = it) }
}

private suspend fun shouldMatchSnapshot(actual: String) {
    expectSelfie(actual).toMatchDisk()
}
