package h8d.interpreter.stackmachine.output

import h8d.stackmachine.ExecutionContext
import h8d.stackmachine.getExtension

internal interface OutputStream<V : Any> : ExecutionContext.Extension<V> {
    fun output(value: String)
}

internal fun <V : Any> ExecutionContext<V>.output(value: String) {
    getExtension<V, OutputStream<V>>().output(value)
}

internal class OutputHolder<V : Any> : OutputStream<V> {
    private var latestOutput: String? = null

    override fun output(value: String) {
        latestOutput = value
    }

    fun consume(): String? {
        val result = latestOutput
        latestOutput = null
        return result
    }
}
