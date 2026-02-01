package h8d.parser.testharness

import com.diffplug.selfie.coroutines.expectSelfie
import h8d.parser.ParseResult
import h8d.parser.parse

internal suspend fun String.shouldParseAndMatchSnapshot() {
    when (val result = parse(this)) {
        is ParseResult.SuccessfulProgram ->
            shouldMatchSnapshot(result.program.nodes.joinToString(separator = "\n"))

        is ParseResult.Error ->
            shouldMatchSnapshot(result.errors.joinToString(separator = "\n"))
    }
}

private suspend fun shouldMatchSnapshot(actual: String) {
    expectSelfie(actual).toMatchDisk()
}
