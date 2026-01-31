package h8d.interpreter.memory

internal interface ScalarMemory {
    fun readNumber(address: Address): Number

    data object Empty : ScalarMemory {
        override fun readNumber(address: Address): Number = error("Memory is empty, value at $address is missing")
    }
}
