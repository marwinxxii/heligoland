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
5. `reduce` cannot be parallelised for every case, only for associative operations
6. No garbage collection

## Clarifications:
- Q: Can ANTLR be used? A: Yes, as any other normal library
- Q: Are there any requirements about parallelization of the map/reduce computation? There could be multiple different strategies for this.

## Important notes
1. Snapshot tests in `:parser` module rely on the [unreleased fix in selfie](https://github.com/diffplug/selfie/pull/559)
    - Note: this snapshot can't be published on JDK 25 (Dokkatoo fails), 21 can be used instead

## TODO
1. Finalise parser
2. Add map + reduce execution to interpreter
3. Run the code from IDE
4. Add Detekt + ktlint
5. More tests, add Kover
