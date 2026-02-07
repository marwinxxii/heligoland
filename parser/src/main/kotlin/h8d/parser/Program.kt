package h8d.parser

public interface Program {
    public val nodes: List<StatementNode>

    public sealed interface StatementNode {
        public val pointer: SourceCodePointer? // TODO should not be null

        public data class VariableAssignmentNode(
            override val pointer: SourceCodePointer?,
            val variableName: String,
            val expression: ExpressionNode,
        ) : StatementNode

        public data class OutputNode(
            override val pointer: SourceCodePointer?,
            val expression: ExpressionNode,
        ) : StatementNode

        public data class PrintNode(
            override val pointer: SourceCodePointer?,
            val value: String,
        ) : StatementNode

        public sealed interface ExpressionNode : StatementNode {
            public data class NumberLiteral(
                override val pointer: SourceCodePointer?,
                val value: Number,
            ) : ExpressionNode

            public data class SeqNode(
                override val pointer: SourceCodePointer?,
                val first: ExpressionNode,
                val last: ExpressionNode,
            ) : ExpressionNode

            public data class VariableReferenceNode(
                override val pointer: SourceCodePointer?,
                val variableName: String,
            ) : ExpressionNode

            public data class BinaryOperationNode(
                override val pointer: SourceCodePointer?,
                val left: ExpressionNode,
                val operation: Operation,
                val right: ExpressionNode,
            ) : ExpressionNode {
                public enum class Operation {
                    ADDITION,
                    SUBTRACTION,
                    MULTIPLICATION,
                    DIVISION,
                    EXPONENTIATION,
                    ;
                }
            }
        }
    }
}
