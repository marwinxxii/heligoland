package h8d.interpreter.stackmachine.sequence

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.math.max
import kotlin.math.min

internal class ParallelStrategy<V : Any>(
    private val parallelisationFactor: Int,
    private val coroutineScope: CoroutineScope,
) : SequenceComputationStrategy<V> {
    override fun <T : Any> computeToIterable(sequence: IndexedSequence<T>): Iterable<T> =
        when (sequence) {
            is PredefinedNumberSequence -> sequence
            else -> object : Iterable<T> {
                override fun iterator(): Iterator<T> =
                    ParallelCollectingIterator(parallelisationFactor, sequence, coroutineScope)
            }
        }
}

private class ParallelCollectingIterator<T : Any>(
    parallelisationFactor: Int,
    private val sequence: IndexedSequence<T>,
    private val coroutineScope: CoroutineScope,
) : Iterator<T> {
    private var index = 0L
    private val workersCount = max(1, parallelisationFactor)
    private val chunkSize = min(
        sequence.size / workersCount + (if (sequence.size % workersCount.toLong() == 0L) 0 else 1),
        Int.MAX_VALUE.toLong()
    )
        .toInt()
    private val chunks by lazy {
        List(workersCount) {
            chunkSize * it until min((chunkSize * it + chunkSize).toLong(), sequence.size)
        }
            .map { range -> coroutineScope.async { range.map { sequence[it] } } }
    }

    override fun next(): T =
        runBlocking {
            chunks[(index / chunkSize).toInt()]
                .await()
                .let { it[(index % chunkSize).toInt()] }
                .also { index++ }
        }

    override fun hasNext(): Boolean = index < sequence.size
}
