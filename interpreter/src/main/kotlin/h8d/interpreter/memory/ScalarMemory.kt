package h8d.interpreter.memory

internal interface ScalarMemory<V : Any> {
    fun readValue(address: Address): V

    fun writeValue(address: Address, value: V)

    companion object {
        // safe cast since object can never read a value
        @Suppress("UNCHECKED_CAST")
        fun <V : Any> empty(): ScalarMemory<V> = Empty as ScalarMemory<V>
    }

    data object Empty : ScalarMemory<Any> {
        override fun readValue(address: Address): Any = error("Memory is empty, value at $address is missing")

        override fun writeValue(address: Address, value: Any) = Unit
    }
}
