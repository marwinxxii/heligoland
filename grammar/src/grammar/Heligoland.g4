grammar Heligoland;

import Literals;

//expr: expr op expr | (expr) | identifier | { expr, expr }
//        | number | map(expr, identifier -> expr)
//        | reduce(expr, expr, identifier identifier -> expr)
expr: expr op expr |
    '('expr')' |
    identifier |
    sequence |
    number;
number: LongLiteral | DoubleLiteral;
sequence: '{' expr ', ' expr '}';
op: '+' | '-' | '*' | '/' | '^';
stmt: assignment | output | print;
assignment: 'var ' identifier ' = ' expr;
output: 'out ' expr;
print: 'print ' StringLiteral;
identifier: (LatinCharacter+ (DecDigit | LatinCharacter)*);
program: stmt+ EOF;
