package h8d.interpreter.stackmachine.sequence

import h8d.stackmachine.ExecutionContext
import h8d.stackmachine.getExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 * Define how items of a sequence should be computed.
 */
internal interface SequenceComputationStrategy<V : Any> : ExecutionContext.Extension<V> {
    fun <T : Any> computeToIterable(sequence: IndexedSequence<T>): Iterable<T>

    companion object {
        fun <V : Any> sequential() = object : SequenceComputationStrategy<V> {
            override fun <T : Any> computeToIterable(sequence: IndexedSequence<T>): Iterable<T> =
                sequence.asIterable()
        }

        fun <V : Any> parallel(parallelFactor: Int): SequenceComputationStrategy<V> =
            if (parallelFactor == 1) sequential() else
                ParallelStrategy(
                    parallelisationFactor = parallelFactor,
                    // TODO pool should be managed outside
                    coroutineScope = Executors.newFixedThreadPool(parallelFactor)
                        .asCoroutineDispatcher()
                        .let(::CoroutineScope),
                )
    }
}

internal fun <T : Any, V : Any> ExecutionContext<V>.computeToIterable(
    sequence: IndexedSequence<T>,
): Iterable<T> = getExtension<V, SequenceComputationStrategy<V>>().computeToIterable(sequence)
