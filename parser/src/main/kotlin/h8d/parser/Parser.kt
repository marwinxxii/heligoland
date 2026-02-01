package h8d.parser

import com.strumenta.antlrkotlin.runtime.BitSet
import h8d.parser.Program.StatementNode
import h8d.parsers.generated.HeligolandBaseVisitor
import h8d.parsers.generated.HeligolandLexer
import h8d.parsers.generated.HeligolandParser
import org.antlr.v4.kotlinruntime.ANTLRErrorListener
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.Parser
import org.antlr.v4.kotlinruntime.RecognitionException
import org.antlr.v4.kotlinruntime.Recognizer
import org.antlr.v4.kotlinruntime.StringCharStream
import org.antlr.v4.kotlinruntime.atn.ATNConfigSet
import org.antlr.v4.kotlinruntime.dfa.DFA

public sealed interface ParsingError {
}

public sealed interface ParseResult {
    public data class SuccessfulProgram(val program: Program) : ParseResult

    public data class Error(val errors: List<String>) : ParseResult
}

public fun parse(sourceCode: String): ParseResult {
    val errorCollector = ParsingErrorCollector()
    return HeligolandParser(CommonTokenStream(HeligolandLexer(StringCharStream(sourceCode))))
        .apply { addErrorListener(errorCollector) }
        .program()
//        .also { it.exception?.also(errorCollector::onErrorFromNode) }
        .children
        ?.mapNotNull {
            it.accept(object : HeligolandBaseVisitor<StatementNode?>() {
                override fun visitPrint(ctx: HeligolandParser.PrintContext) = ctx.toNode()

                override fun defaultResult(): StatementNode? = null
            })
        }
        ?.takeIf { it.isNotEmpty() }
        .let { nodes ->
            val collectedErrors = errorCollector.collectedErrors
            when {
                nodes == null ->
                    collectedErrors
                        .takeIf { it.isNotEmpty() }
                        .let { it ?: listOf("Source code couldn't be parsed. Is it empty?") }
                        .let(ParseResult::Error)

                collectedErrors.isNotEmpty() -> ParseResult.Error(collectedErrors)

                else -> ParseResult.SuccessfulProgram(ProgramImpl(nodes))
            }
        }
}

private data class ProgramImpl(override val nodes: List<StatementNode>) : Program

private fun HeligolandParser.PrintContext.toNode(): StatementNode =
    StringLiteral()
        .symbol
        .text
        ?.removePrefix("${'"'}")
        ?.removeSuffix("${'"'}")
        ?.let(StatementNode::PrintNode)!!

private class ParsingErrorCollector : ANTLRErrorListener {
    private val errors = mutableListOf<String>()

    val collectedErrors: List<String> get() = errors

//    fun onErrorFromNode(e: RecognitionException) {
//        errors.add(e.message!!)
//    }

    override fun syntaxError(
        recognizer: Recognizer<*, *>,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException?,
    ) {
        errors.add(msg)
    }

    override fun reportAmbiguity(
        recognizer: Parser,
        dfa: DFA,
        startIndex: Int,
        stopIndex: Int,
        exact: Boolean,
        ambigAlts: BitSet,
        configs: ATNConfigSet,
    ) = Unit

    override fun reportAttemptingFullContext(
        recognizer: Parser,
        dfa: DFA,
        startIndex: Int,
        stopIndex: Int,
        conflictingAlts: BitSet,
        configs: ATNConfigSet,
    ) = Unit

    override fun reportContextSensitivity(
        recognizer: Parser,
        dfa: DFA,
        startIndex: Int,
        stopIndex: Int,
        prediction: Int,
        configs: ATNConfigSet,
    ) = Unit
}
