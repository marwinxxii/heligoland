package h8d.parser

@ConsistentCopyVisibility
public data class SourceCodePointer internal constructor(
    val start: Position,
    val end: Position,
) {
    @ConsistentCopyVisibility
    public data class Position internal constructor(
        val lineNumber: Int,
        val characterPosition: Int?,
    ) {
        override fun toString(): String = "L$lineNumber:$characterPosition"
    }

    override fun toString(): String = buildString {
        append(start)
        if (end != start) {
            append('-')
            append(end)
        }
    }
}
