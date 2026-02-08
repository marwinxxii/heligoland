package h8d.stackmachine.arithmetic

import h8d.stackmachine.StackInstruction
import h8d.stackmachine.StackInstruction.Push
import h8d.stackmachine.arithmetic.ArithmeticInstruction.Add
import h8d.stackmachine.arithmetic.ArithmeticInstruction.Subtract
import h8d.stackmachine.computeStack
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe

// test run by Kotest
@Suppress("unused")
internal class ArithmeticStackMachineTest : FunSpec({
    context("Addition") {
        withTests(
            nameFn = ::binaryOperationTestName,
            listOf(
                // @formatter:off
                binaryOperation(9,       Add, 0,      shouldBe = 9),
                binaryOperation(6.1,     Add, 0.0,    shouldBe = 6.1),
                binaryOperation(2,       Add, 8,      shouldBe = 10),
                binaryOperation(10000.1, Add, 0.011,  shouldBe = 10000.111),
                binaryOperation(-10,     Add, 31,     shouldBe = 21L),
                binaryOperation(-200.2,  Add, 2.1,    shouldBe = -198.1),
                binaryOperation(0,       Add, 22,     shouldBe = 22L),
                binaryOperation(0.0,     Add, 0.123,  shouldBe = 0.123),
                binaryOperation(1990,    Add, -90,    shouldBe = 1900),
                binaryOperation(0.1,     Add, -273.1, shouldBe = -273.0),
                binaryOperation(-2000,   Add, -26,    shouldBe = -2026),
                binaryOperation(-400.0,  Add, -51.0,  shouldBe = -451.0),
                // @formatter:on
            ),
        ) {
            val (instructions, expected) = it
            instructions.shouldComputeSingleValue(expected)
        }
    }
    context("Subtraction") {
        withTests(
            nameFn = ::binaryOperationTestName,
            listOf(
                // @formatter:off
                binaryOperation(0,       Subtract, 2,        shouldBe = -2),
                binaryOperation(0.0,     Subtract, 0.1234,   shouldBe = -0.1234),
                binaryOperation(3,       Subtract, 0,        shouldBe = 3),
                binaryOperation(3.45,    Subtract, 0.0,      shouldBe = 3.45),
                binaryOperation(10,      Subtract, 5,        shouldBe = 5),
                binaryOperation(10.1,    Subtract, 4.05,     shouldBe = 6.05),
                binaryOperation(100,     Subtract, 200,      shouldBe = -100),
                binaryOperation(20.25,   Subtract, 40.05,    shouldBe = -19.8),
                binaryOperation(-4,      Subtract, -5,       shouldBe = 1),
                binaryOperation(-1000.5, Subtract, -5000.7,  shouldBe = 4000.2),
                binaryOperation(-350,    Subtract, 150,      shouldBe = -500),
                binaryOperation(-60.5,   Subtract, 21.5,     shouldBe = -82.0),
                // @formatter:on
            ),
        ) { testCase ->
            val (instructions, expected) = testCase
            instructions.shouldComputeSingleValue().also {
                when (it) {
                    is Double -> it.shouldBeEqualWithTolerance(expected.toDouble())
                    else -> it shouldBe expected
                }
            }
        }
    }

    // TODO test other operations
})

// kotest can't compare Double values with tolerance
private fun Double.shouldBeEqualWithTolerance(expected: Double, tolerance: Double = 0.00001) {
    this shouldBeGreaterThanOrEqual expected - tolerance
    this shouldBeLessThanOrEqual expected + tolerance
}

private fun binaryOperation(
    left: Long,
    operation: ArithmeticInstruction,
    right: Long,
    shouldBe: Long,
) = listOf<StackInstruction<Number>>(Push(left), Push(right), operation) to shouldBe

private fun binaryOperation(
    left: Double,
    operation: ArithmeticInstruction,
    right: Double,
    shouldBe: Double,
) = listOf<StackInstruction<Number>>(Push(left), Push(right), operation) to shouldBe

private fun binaryOperationTestName(testCase: Pair<List<StackInstruction<Number>>, Number>) =
    "${testCase.first[0].name} ${testCase.first[2]} ${testCase.first[1].name} = ${testCase.second}"

private val StackInstruction<*>.name: String
    get() =
        when (this) {
            is Push<*> -> value.toString()
            else -> toString()
        }

private suspend fun List<StackInstruction<Number>>.shouldComputeSingleValue(expected: Number) {
    shouldComputeSingleValue() shouldBe expected
}

private suspend fun List<StackInstruction<Number>>.shouldComputeSingleValue(): Number =
    computeStack(this).let { stack ->
        stack.pop()
            .also { shouldThrowAny { stack.pop() } }
    }
