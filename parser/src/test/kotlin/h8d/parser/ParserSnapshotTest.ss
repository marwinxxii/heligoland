╔═ Invalid programs-a: ═╗
mismatched input '<EOF>' expecting {'var ', 'out ', 'print '} (L1:0)
╔═ Invalid programs-b: println("Hello, World!") ═╗
mismatched input 'p' expecting {'var ', 'out ', 'print '} (L1:0)
╔═ Invalid programs-c: var a = {1.0, 9.0} ═╗
Sequence can be generated only from Long values (L1:8-L1:18)
╔═ Valid programs-a: print "Hello, World!" ═╗
PrintNode(pointer=null, value=Hello, World!)
╔═ Valid programs-b: out 100 ═╗
OutputNode(pointer=null, expression=NumberLiteral(pointer=null, value=100))
╔═ Valid programs-c: var a = 20 ═╗
VariableAssignmentNode(pointer=null, variableName=a, expression=NumberLiteral(pointer=null, value=20))
╔═ Valid programs-d ═╗
VariableAssignmentNode(pointer=null, variableName=b, expression=NumberLiteral(pointer=null, value=11))
OutputNode(pointer=null, expression=VariableReferenceNode(pointer=null, variableName=b))
╔═ Valid programs-e: out {0, 10} ═╗
OutputNode(pointer=null, expression=SeqNode(pointer=L1:4-L1:11, first=NumberLiteral(pointer=null, value=0), last=NumberLiteral(pointer=null, value=10)))
╔═ Valid programs-f ═╗
VariableAssignmentNode(pointer=null, variableName=f, expression=NumberLiteral(pointer=null, value=1))
OutputNode(pointer=null, expression=SeqNode(pointer=L2:4-L2:11, first=VariableReferenceNode(pointer=null, variableName=f), last=NumberLiteral(pointer=null, value=10)))
╔═ Valid programs-g ═╗
VariableAssignmentNode(pointer=null, variableName=f, expression=NumberLiteral(pointer=null, value=100))
VariableAssignmentNode(pointer=null, variableName=l, expression=NumberLiteral(pointer=null, value=123))
OutputNode(pointer=null, expression=SeqNode(pointer=L3:4-L3:10, first=VariableReferenceNode(pointer=null, variableName=f), last=VariableReferenceNode(pointer=null, variableName=l)))
╔═ Valid programs-h: out 10+20 ═╗
OutputNode(pointer=null, expression=BinaryOperationNode(pointer=null, left=NumberLiteral(pointer=null, value=10), operation=ADDITION, right=NumberLiteral(pointer=null, value=20)))
╔═ Valid programs-i: out map({0, 10}, i -> i+1) ═╗
OutputNode(pointer=null, expression=MapCallNode(pointer=null, sequence=SeqNode(pointer=L1:8-L1:15, first=NumberLiteral(pointer=null, value=0), last=NumberLiteral(pointer=null, value=10)), lambda=Lambda(argument=VariableReferenceNode(pointer=null, variableName=i), body=BinaryOperationNode(pointer=null, left=VariableReferenceNode(pointer=null, variableName=i), operation=ADDITION, right=NumberLiteral(pointer=null, value=1)))))
╔═ Valid programs-j: out reduce({0, 10}, 0, a b -> a+b) ═╗
OutputNode(pointer=null, expression=ReduceCallNode(pointer=null, sequence=SeqNode(pointer=L1:11-L1:18, first=NumberLiteral(pointer=null, value=0), last=NumberLiteral(pointer=null, value=10)), accumulator=NumberLiteral(pointer=null, value=0), lambda=Lambda(itemArgument=VariableReferenceNode(pointer=null, variableName=a), accumulatorArgument=VariableReferenceNode(pointer=null, variableName=b), body=BinaryOperationNode(pointer=null, left=VariableReferenceNode(pointer=null, variableName=a), operation=ADDITION, right=VariableReferenceNode(pointer=null, variableName=b)))))
╔═ [end of file] ═╗
