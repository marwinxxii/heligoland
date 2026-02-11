package h8d.interpreter.stackmachine.sequence

internal interface IndexedSequence<T : Any> : Iterable<T> {
    val size: Long

    operator fun get(index: Long): T

    override fun iterator(): Iterator<T> =
        object : Iterator<T> {
            private var currentIndex = 0L

            override fun next(): T = this@IndexedSequence[currentIndex++]

            override fun hasNext(): Boolean = currentIndex < size
        }
}

internal data class PredefinedNumberSequence(
    val first: Long,
    val last: Long,
) : IndexedSequence<Long> {
    override val size: Long = last - first + 1

    override fun get(index: Long): Long = (first + index).also { require(it <= last) }
}

internal class LazySequence<T : Any, R : Any>(
    private val source: IndexedSequence<T>,
    private val transform: (T) -> R,
) : IndexedSequence<R> {
    override val size: Long get() = source.size

    override fun get(index: Long): R = transform(source[index])
}
