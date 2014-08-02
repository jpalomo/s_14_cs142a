package mips;

import ast.*;
import types.*;

public class CodeGen implements ast.CommandVisitor {
    
    private StringBuffer errorBuffer = new StringBuffer();
    private TypeChecker tc;
    private Program program;
    private ActivationRecord currentFunction;
	private String functionName;
	private String currentFunctionLabel;

    public CodeGen(TypeChecker tc)
    {
        this.tc = tc;
        this.program = new Program();
		program.setCodeGen(this);
    }
    
    public boolean hasError()
    {
        return errorBuffer.length() != 0;
    }
    
    public String errorReport()
    {
        return errorBuffer.toString();
    }

		@Override
		public void visit(ReadSymbol readSymbol) {
				throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}

    private class CodeGenException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
        public CodeGenException(String errorMessage) {
            super(errorMessage);
        }
    }
    
    public boolean generate(Command ast)
    {
		//entry point for the program, generate a new global stack frame
        try {
            currentFunction = ActivationRecord.newGlobalFrame();
			//call the first node in the AST
            ast.accept(this);
            return !hasError();
        } catch (CodeGenException e) {
            return false;
        }
    }
    
    public Program getProgram()
    {
        return program;
    }

    @Override
    public void visit(ExpressionList node) {
        for (Expression e : node) {
            e.accept(this);
        }
    }

    @Override
    public void visit(DeclarationList node) {
		for (Declaration d : node)
            d.accept(this);
    }

    @Override
    public void visit(StatementList node) {
		 for (Statement stmt : node) {
        	stmt.accept(this);
        	if (stmt instanceof Call) {
        		Call callNode = (Call) stmt;
				Type typeOfReturn = tc.getType(callNode);
				if (!typeOfReturn.equivalent(new VoidType())) {
					//program.debugComment("Cleaning up unused function return value on stack.");
					if (typeOfReturn instanceof IntType || typeOfReturn instanceof BoolType) {
						program.popInt("$t0");
					}
					if (typeOfReturn instanceof FloatType) {
						program.popFloat("$t0");
					} 
				}
        	}
        }
    }

    @Override
    public void visit(AddressOf node) {
        throw new RuntimeException("Implement this");
    }

    @Override
	/**
	 * Example: li $t1, 13
	 */
    public void visit(LiteralBool node) {
		String tempReg0 = "$t0";
		//load the boolean value into register t0
		StringBuilder sb = new StringBuilder("li ").append(tempReg0).append(", ");
		int booleanValue = 1; //denotes true
		if(node.value() == LiteralBool.Value.FALSE){
			booleanValue = 0;
		}
		program.appendInstruction(sb.append(booleanValue).toString());
		program.pushInt(tempReg0);
    }

    @Override
    public void visit(LiteralFloat node) {
		String fpReg0 = "$f0"; //floating point reg0
		StringBuilder sb = new StringBuilder("li.s ").append(fpReg0).append(", ");
		program.appendInstruction(sb.append(node.value()).toString());
		program.pushFloat(fpReg0);
	}

    @Override
    public void visit(LiteralInt node) {
		String tempReg0 = "$t0";
		StringBuilder sb = new StringBuilder("li ").append(tempReg0).append(", ");
		program.appendInstruction(sb.append(node.value()).toString());
		program.pushInt(tempReg0);
	}

    @Override
    public void visit(VariableDeclaration node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(ArrayDeclaration node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(FunctionDefinition node) {
		int codeSegmentSize = setupFunction(node);
		node.body().accept(this);
		program.insertPrologue((codeSegmentSize + 1), currentFunction.stackSize());
		program.appendInstruction(currentFunctionLabel + ":");

		Type retType = tc.getType(node);

		if (retType instanceof IntType || retType instanceof BoolType) { 
			program.popInt("$v0");
		}

		if (retType.equivalent(new FloatType())) {
			program.popFloat("$v0");
		}

		program.appendEpilogue(currentFunction.stackSize()); 
    	currentFunction = currentFunction.parent();
    }

	private int setupFunction(FunctionDefinition node){
		functionName = node.symbol().name(); //save the function name
		currentFunctionLabel = program.newLabel(); //create a label for jump/return statements
        currentFunction = new ActivationRecord(node, currentFunction); //create the new activation record linked to its parent
		return program.appendInstruction(functionName + ":");	
	}

    @Override
    public void visit(Addition node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Subtraction node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Multiplication node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Division node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LogicalAnd node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LogicalOr node) {
        throw new RuntimeException("Implement this");
    }
    
    @Override
    public void visit(LogicalNot node) {
        String label = program.newLabel();
        String pushLabel = program.newLabel();
		node.expression().accept(this);
        program.popInt("$t0");
        program.appendInstruction("beqz $t0, " + label);
        program.appendInstruction("li $t1, 0");
        program.appendInstruction("b " + label);
        program.appendInstruction(label + ":");
        program.appendInstruction("li $t1, 1");
        program.appendInstruction(pushLabel + ":");
        program.pushInt("$t1");
	}

    @Override
    public void visit(Comparison node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Dereference node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Index node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Assignment node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Call node) {
      	node.arguments().accept(this);//get the function arguments
		
		StringBuilder functionNodeName = new StringBuilder("func.").append(node.function().name());
		
		//call the function
        program.appendInstruction("jal " + functionNodeName.toString());

		//add the arguments to the stack
        if (node.arguments().size() > 0) {
        	int argSize = 0;
        	for (Expression expr : node.arguments()) {
				Type type = tc.getType((Command) expr);
        		argSize += ActivationRecord.numBytes(type);
        	}
			program.appendInstruction("addi $sp, $sp, " + argSize);
        }

		FuncType func = (FuncType) node.function().type();
 		if (!func.returnType().equivalent(new VoidType())) {
			program.appendInstruction("subu $sp, $sp, 4");
			program.appendInstruction("sw $v0, 0($sp)");
 		}
    }

    @Override
    public void visit(IfElseBranch node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(WhileLoop node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Return node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(ast.Error node) {
        String message = "CodeGen cannot compile a " + node;
        errorBuffer.append(message);
        throw new CodeGenException(message);
    }

	/**
	 * Helper Methods
	 */ 
	public boolean isCurrentFunctionMain(){
		if(functionName != null){
			return functionName.equals("main");
		}
		return false;
	}
}
