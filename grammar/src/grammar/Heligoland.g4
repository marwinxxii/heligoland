grammar Heligoland;

import Literals;

expr: expr op expr |
    '('expr')' |
    identifier |
    sequence |
    number |
    mapCall |
    reduceCall;
number: LongLiteral | DoubleLiteral;
sequence: '{' expr ', ' expr '}';
// TODO generalise function calls
mapCall: 'map(' expr ', ' identifier ' -> ' expr ')';
reduceCall: 'reduce(' expr ', ' expr ', ' identifier ' ' identifier ' -> ' expr ')';
op: '+' | '-' | '*' | '/' | '^';
stmt: assignment | output | print;
assignment: 'var ' identifier ' = ' expr;
output: 'out ' expr;
print: 'print ' StringLiteral;
identifier: (LatinCharacter+ (DecDigit | LatinCharacter)*);
program: stmt+ EOF;
