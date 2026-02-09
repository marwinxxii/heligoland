package h8d.interpreter.stackmachine.sequence

import h8d.stackmachine.ExecutionContext
import h8d.stackmachine.getExtension

internal interface SequenceEvaluationStrategy<V : Any> : ExecutionContext.Extension<V> {
    fun <T : Any> evaluateToIterable(sequence: Sequence<T>): Iterable<T>

    companion object {
        fun <V : Any> sequential() = object : SequenceEvaluationStrategy<V> {
            override fun <T : Any> evaluateToIterable(sequence: Sequence<T>): Iterable<T> =
                sequence.asIterable()
        }
    }
}

internal fun <T : Any, V : Any> ExecutionContext<V>.evaluateToIterable(
    sequence: Sequence<T>,
): Iterable<T> = getExtension<V, SequenceEvaluationStrategy<V>>().evaluateToIterable(sequence)
