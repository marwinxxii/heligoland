package h8d.parser

public interface Program {
    public val nodes: List<StatementNode>

    public sealed interface StatementNode {
        public data class VariableAssignmentNode(
            val variableName: String,
            val expression: ExpressionNode,
        ) : StatementNode

        public data class OutputNode(val expression: ExpressionNode) : StatementNode

        public data class PrintNode(val value: String) : StatementNode

        public sealed interface ExpressionNode : StatementNode {
            public data class NumberLiteral(val value: Number) : ExpressionNode

            public data class SeqNode(
                val first: ExpressionNode,
                val last: ExpressionNode,
            ) : ExpressionNode

            public data class VariableReferenceNode(val variableName: String) : ExpressionNode

            public data class ArithmeticNode(
                val left: ExpressionNode,
                val right: ExpressionNode,
            ) : ExpressionNode
        }
    }
}
