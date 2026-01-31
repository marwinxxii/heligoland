package h8d.parser

import h8d.parser.Program.StatementNode
import h8d.parsers.generated.HeligolandBaseVisitor
import h8d.parsers.generated.HeligolandLexer
import h8d.parsers.generated.HeligolandParser
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.StringCharStream

public sealed interface ParsingError

public sealed interface ParseResult {
    public data class SuccessfulProgram(val program: Program) : ParseResult

    public data class Error(val errors: List<ParsingError>) : ParseResult
}

public fun parse(sourceCode: String): ParseResult =
    HeligolandParser(CommonTokenStream(HeligolandLexer(StringCharStream(sourceCode))))
        .program()
        .children
        ?.mapNotNull {
            it.accept(object : HeligolandBaseVisitor<StatementNode?>() {
                override fun visitPrint(ctx: HeligolandParser.PrintContext) = ctx.toNode()

                override fun defaultResult(): StatementNode? = null
            })
        }
        ?.let { ProgramImpl(nodes = it) }
        ?.let(ParseResult::SuccessfulProgram)
        ?: ParseResult.Error(emptyList())

private data class ProgramImpl(override val nodes: List<StatementNode>) : Program

private fun HeligolandParser.PrintContext.toNode(): StatementNode =
    StringLiteral()
        .symbol
        .text
        ?.removePrefix("${'"'}")
        ?.removeSuffix("${'"'}")
        ?.let(StatementNode::PrintNode)!!
