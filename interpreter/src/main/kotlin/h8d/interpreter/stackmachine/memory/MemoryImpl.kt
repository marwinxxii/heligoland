package h8d.interpreter.stackmachine.memory

internal class AddressHolder {
    private val variables = mutableMapOf<String, Address>()
    private var addressPointer = 0L

    fun nextAddress(): Address = Address(addressPointer++)

    fun allocateAddress(variableName: String): Address =
        variables.getOrPut(variableName, ::nextAddress)

    fun resolveAddressOfVariable(variableName: String): Address =
        requireNotNull(variables[variableName]) {
            "Variable $variableName was not assigned"
        }
}

// TODO make it thread safe?
internal class MapMemory<V : Any> : Memory<V>, Memory.Writeable<V> {
    private val values = mutableMapOf<Address, V>()

    override fun readValue(address: Address): V =
        requireNotNull(values[address])

    override fun writeValue(address: Address, value: V) {
        values[address] = value
    }
}
