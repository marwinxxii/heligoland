# Heligoland
Micro language, interpreter and IDE.

## Decisions
1. Grammar does not support changing values of the variables, so they are treated as immutable after an assignment
2. `out` operator
    - Can output any expression
    - Sequences are outputted by joining all members to comma separated string
3. Working only with Long and Double values for simplicity
