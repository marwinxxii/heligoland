package h8d.parser.testharness

import com.diffplug.selfie.coroutines.expectSelfie
import h8d.parser.ParseResult
import h8d.parser.parseProgram
import io.kotest.matchers.types.shouldBeInstanceOf

internal suspend fun String.shouldMatchParsingErrorsSnapshot() {
    shouldBeParseResult<ParseResult.Error>()
        .errors
        .joinToString(separator = "\n")
        .also { shouldMatchSnapshot(it) }
}

internal suspend fun String.shouldMatchTreeNodesSnapshot() {
    shouldBeParseResult<ParseResult.SuccessfulProgram>()
        .program
        .nodes
        .joinToString(separator = "\n")
        .also { shouldMatchSnapshot(it) }
}

private inline fun <reified T : ParseResult> String.shouldBeParseResult() =
    parseProgram(this.trimIndent().trim())
        .shouldBeInstanceOf<T>()

private suspend fun shouldMatchSnapshot(actual: String) {
    expectSelfie(actual).toMatchDisk()
}
