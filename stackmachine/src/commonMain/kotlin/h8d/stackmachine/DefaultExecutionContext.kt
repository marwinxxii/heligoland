package h8d.stackmachine

internal class DefaultExecutionContext<V : Any>(
    override val extensions: Set<ExecutionContext.Extension<V>>,
) : ExecutionContext<V> {
    override val stack = ListBasedStack<V>()
}

@JvmInline
internal value class ListBasedStack<V : Any>(
    private val list: MutableList<V> = mutableListOf(),
) : ExecutionContext.ValueStack<V>, Iterable<V> by list {
    override fun pop(): V {
        // TODO type
        if (list.isEmpty()) throw IllegalStateException("Not enough arguments, stack is empty")
        return list.removeLast()
    }

    // TODO better error message
    override fun peek(index: Int): V = list[index]

    fun push(value: V) {
        list.add(value)
    }
}
