# Heligoland
Micro language, interpreter and IDE.

## Decisions
1. Grammar does not support changing values of the variables, so they are treated as immutable after an assignment
2. `out` operator
    - Can output any expression
    - Sequences are outputted by joining all members to comma separated string
3. Working only with Long and Double values for simplicity
4. Operations are allowed only between scalar values
   - `map` and reduce operations can't be used inside the lambda
5. `reduce` cannot be parallelised for every case, only for associative operations,
only for operations on `Long` type
6. No garbage collection
7. Sequences are lazy and evaluated only at collection

## Clarifications:
- Q: Can ANTLR be used? A: Yes, as any other normal library
- Q: Are there any requirements about parallelization of the map/reduce computation? There could be multiple different strategies for this.

## Important notes
1. Snapshot tests in `:parser` and `:interpreter` modules rely on the [unreleased fix in selfie](https://github.com/diffplug/selfie/pull/559)
    - Note: this snapshot can't be published on JDK 25 (Dokkatoo fails), 21 can be used instead
2. Selfie does not support snapshot tests for parameterized tests for Kotest,
see [local fix](selfie-parameterized-tests.patch)

## TODO
1. ~~Finalise parser~~ (some minimal type checks added)
2. Proper support for negative numbers in the grammar
3. ~~Add map + reduce execution to interpreter~~ (reduce is not parallelised)
4. ~~Run the code from IDE~~
5. Add Detekt + ktlint
6. More tests, add Kover
