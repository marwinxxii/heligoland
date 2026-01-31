package h8d.interpreter.memory

@JvmInline
internal value class Address(val value: Long) {
    override fun toString(): String = "Address($value)"
}
