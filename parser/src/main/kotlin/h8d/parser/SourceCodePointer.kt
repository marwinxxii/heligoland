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
    )
}
