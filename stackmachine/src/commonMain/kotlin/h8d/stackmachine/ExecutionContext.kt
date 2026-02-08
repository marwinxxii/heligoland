package h8d.stackmachine

public sealed interface ExecutionContext<V : Any> {
    public val stack: ValueStack<V>

    public val extensions: Set<Extension<V>>

    public sealed interface ValueStack<V : Any> {
        public fun pop(): V
    }

    public interface Extension<V : Any>
}

public fun <V : Any> ExecutionContext(): ExecutionContext<V> =
    DefaultExecutionContext(extensions = emptySet())

public fun <V : Any> ExecutionContext(vararg extension: ExecutionContext.Extension<V>): ExecutionContext<V> =
    DefaultExecutionContext(extension.toSet())

public inline fun <V : Any, reified T : ExecutionContext.Extension<V>> ExecutionContext<V>.getExtension(): T =
    extensions.firstNotNullOf { it as? T }
