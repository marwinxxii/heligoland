grammar Heligoland;

import Literals;

//expr: expr op expr | (expr) | identifier | { expr, expr }
//        | number | map(expr, identifier -> expr)
//        | reduce(expr, expr, identifier identifier -> expr)
expr: expr op expr | '('expr')' | identifier | number;
number: LongLiteral | DoubleLiteral;
op: '+' | '-' | '*' | '/' | '^';
stmt: 'var ' identifier ' = ' expr | output | print;
output: 'out ' expr;
print: 'print ' StringLiteral;
identifier: (LatinCharacter+ (DecDigit | LatinCharacter)*);
program: stmt | program stmt EOF;
