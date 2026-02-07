╔═ Invalid programs-0: ═╗
ParsingErrorDescriptor(pointer=SourceCodePointer(start=Position(lineNumber=1, characterPosition=0), end=Position(lineNumber=1, characterPosition=0)), message=mismatched input '<EOF>' expecting {'var ', 'out ', 'print '})
╔═ Invalid programs-1: println("Hello, World!") ═╗
ParsingErrorDescriptor(pointer=SourceCodePointer(start=Position(lineNumber=1, characterPosition=0), end=Position(lineNumber=1, characterPosition=0)), message=mismatched input 'p' expecting {'var ', 'out ', 'print '})
╔═ Valid programs-0: print "Hello, World!" ═╗
PrintNode(pointer=null, value=Hello, World!)
╔═ Valid programs-1: out 100 ═╗
OutputNode(pointer=null, expression=NumberLiteral(pointer=null, value=100))
╔═ Valid programs-2: var a = 20 ═╗
VariableAssignmentNode(pointer=null, variableName=a, expression=NumberLiteral(pointer=null, value=20))
╔═ Valid programs-3 ═╗
VariableAssignmentNode(pointer=null, variableName=b, expression=NumberLiteral(pointer=null, value=11))
OutputNode(pointer=null, expression=VariableReferenceNode(pointer=null, variableName=b))
╔═ Valid programs-4: out {0, 10} ═╗
OutputNode(pointer=null, expression=SeqNode(pointer=null, first=NumberLiteral(pointer=null, value=0), last=NumberLiteral(pointer=null, value=10)))
╔═ Valid programs-5 ═╗
VariableAssignmentNode(pointer=null, variableName=f, expression=NumberLiteral(pointer=null, value=1))
OutputNode(pointer=null, expression=SeqNode(pointer=null, first=VariableReferenceNode(pointer=null, variableName=f), last=NumberLiteral(pointer=null, value=10)))
╔═ Valid programs-6 ═╗
VariableAssignmentNode(pointer=null, variableName=f, expression=NumberLiteral(pointer=null, value=100))
VariableAssignmentNode(pointer=null, variableName=l, expression=NumberLiteral(pointer=null, value=123))
OutputNode(pointer=null, expression=SeqNode(pointer=null, first=VariableReferenceNode(pointer=null, variableName=f), last=VariableReferenceNode(pointer=null, variableName=l)))
╔═ [end of file] ═╗
