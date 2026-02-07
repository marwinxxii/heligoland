package h8d.parser

import com.strumenta.antlrkotlin.runtime.BitSet
import h8d.parser.Program.StatementNode
import h8d.parser.Program.StatementNode.ExpressionNode
import h8d.parser.Program.StatementNode.ExpressionNode.BinaryOperationNode
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
    public val pointer: SourceCodePointer?
    public val message: String
}

public sealed interface ParseResult {
    @ConsistentCopyVisibility
    public data class SuccessfulProgram internal constructor(val program: Program) : ParseResult

    @ConsistentCopyVisibility
    public data class Error internal constructor(val errors: List<ParsingError>) : ParseResult
}

public fun parseProgram(sourceCode: String): ParseResult {
    val errorCollector = ParsingErrorCollector()
    return sourceCode.toParser(errorCollector)
        .program()
//        .also { it.exception?.also(errorCollector::onErrorFromNode) }
        .children
        ?.mapNotNull {
            it.accept(object : HeligolandBaseVisitor<StatementNode?>() {
                override fun visitPrint(ctx: HeligolandParser.PrintContext) = ctx.toNode()

                override fun visitOutput(ctx: HeligolandParser.OutputContext) = ctx.toNode()

                override fun visitAssignment(ctx: HeligolandParser.AssignmentContext) = ctx.toNode()

                override fun visitExpr(ctx: HeligolandParser.ExprContext) = ctx.toNode()

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
                        .let { it ?: listOf(emptySourceCodeError()) }
                        .let(ParseResult::Error)

                collectedErrors.isNotEmpty() -> ParseResult.Error(collectedErrors)

                else -> ParseResult.SuccessfulProgram(ProgramImpl(nodes))
            }
        }
}

public fun validateSourceCode(sourceCode: String): List<ParsingError> =
    ParsingErrorCollector().also { sourceCode.toParser(it).program() }
        // TODO add tree validation
        .collectedErrors

private fun String.toParser(errorListener: ANTLRErrorListener): HeligolandParser =
    HeligolandParser(CommonTokenStream(HeligolandLexer(StringCharStream(this))))
        .apply { addErrorListener(errorListener) }

private fun emptySourceCodeError() =
    ParsingErrorDescriptor(pointer = null, message = "Source code couldn't be parsed. Is it empty?")

private data class ProgramImpl(override val nodes: List<StatementNode>) : Program

private fun HeligolandParser.PrintContext.toNode() =
    StringLiteral().let {
        StatementNode.PrintNode(
            pointer = null,
            value = it.symbol
                .text
                ?.removePrefix("${'"'}")
                ?.removeSuffix("${'"'}")!!,
        )
    }

private fun HeligolandParser.OutputContext.toNode() =
    StatementNode.OutputNode(
        pointer = null,
        expression = expr().toNode(),
    )

private fun HeligolandParser.AssignmentContext.toNode() =
    StatementNode.VariableAssignmentNode(
        pointer = null,
        variableName = identifier().text,
        expression = expr().toNode(),
    )

private fun HeligolandParser.ExprContext.toNode() =
    number()?.toNode()
        ?: identifier()?.toNode()
        ?: sequence()?.toNode()
        ?: binaryOperationNode(expr(0)!!, op()!!, expr(1)!!)

private fun HeligolandParser.NumberContext.toNode() =
    (LongLiteral() ?: DoubleLiteral())!!.let {
        ExpressionNode.NumberLiteral(
            pointer = null,
            value = if (it.text.contains('.')) it.text.toDouble() else it.text.toLong(),
        )
    }

private fun HeligolandParser.IdentifierContext.toNode() =
    ExpressionNode.VariableReferenceNode(
        pointer = null,
        variableName = this.text,
    )

private fun HeligolandParser.SequenceContext.toNode(): ExpressionNode.SeqNode =
    ExpressionNode.SeqNode(
        pointer = null,
        first = this.expr(0)!!.toNode(),
        last = this.expr(1)!!.toNode(),
    )

private fun binaryOperationNode(
    left: HeligolandParser.ExprContext,
    op: HeligolandParser.OpContext,
    right: HeligolandParser.ExprContext,
): BinaryOperationNode =
    BinaryOperationNode(
        pointer = null,
        left = left.toNode(),
        operation = op.toOperation(),
        right = right.toNode(),
    )

private fun HeligolandParser.OpContext.toOperation() =
    when (this.text) {
        "+" -> BinaryOperationNode.Operation.ADDITION
        "-" -> BinaryOperationNode.Operation.SUBTRACTION
        "*" -> BinaryOperationNode.Operation.MULTIPLICATION
        "/" -> BinaryOperationNode.Operation.DIVISION
        "^" -> BinaryOperationNode.Operation.EXPONENTIATION
        // TODO better error handling
        else -> error("Unknown binary operation: ${this.text}")
    }

private data class ParsingErrorDescriptor(
    override val pointer: SourceCodePointer?,
    override val message: String,
) : ParsingError

private class ParsingErrorCollector : ANTLRErrorListener {
    private val errors = mutableListOf<ParsingError>()

    val collectedErrors: List<ParsingError> get() = errors

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
        ParsingErrorDescriptor(
            pointer = SourceCodePointer.Position(lineNumber = line, characterPosition = charPositionInLine).let {
                SourceCodePointer(it, it)
            },
            message = msg,
        ).also(errors::add)
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
