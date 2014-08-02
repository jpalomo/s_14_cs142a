package types;

import java.util.HashMap;
import ast.*;
import crux.Symbol;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeChecker implements CommandVisitor {
    
    private HashMap<Command, Type> typeMap;
    private StringBuffer errorBuffer;


	    /* Set to true if the current execution path needs a return statement. */
    private boolean returnReqd;
	private boolean returnFound;

    /* A count of the number of returns that has been found in the current function body. */
    private int returnsCount;

	/* The symbol for the current function we're in. */
	private Symbol currentFuncSymb;

	private Type currentFuncRetType;

    /* Useful error strings:
     *
     * "Function " + func.name() + " has a void argument in position " + pos + "."
     * "Function " + func.name() + " has an error in argument in position " + pos + ": " + error.getMessage()
     *
     * "Function main has invalid signature."
     *
     * "Not all paths in function " + currentFunctionName + " have a return."
     *
     * "IfElseBranch requires bool condition not " + condType + "."
     * "WhileLoop requires bool condition not " + condType + "."
     *
     * "Function " + currentFunctionName + " returns " + currentReturnType + " not " + retType + "."
     *
     * "Variable " + varName + " has invalid type " + varType + "."
     * "Array " + arrayName + " has invalid base type " + baseType + "."
     */

    public TypeChecker()
    {
        typeMap = new HashMap<Command, Type>();
        errorBuffer = new StringBuffer();
    }

    private void reportError(int lineNum, int charPos, String message)
    {
        errorBuffer.append("TypeError(" + lineNum + "," + charPos + ")");
        errorBuffer.append("[" + message + "]" + "\n");
    }

    private void put(Command node, Type type)
    {
        if (type instanceof ErrorType) {
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType)type).getMessage());
        }
        typeMap.put(node, type);
    }
    
    public Type getType(Command node)
    {
        return typeMap.get(node);
    }
    
    public boolean check(Command ast)
    {
        ast.accept(this);
        return !hasError();
    }
    
    public boolean hasError()
    {
        return errorBuffer.length() != 0;
    }
    
    public String errorReport()
    {
        return errorBuffer.toString();
    }

	private Type visitRetriveType(Visitable node) {
		node.accept(this);
		return getType((Command) node);
    }

    @Override
	//Pass the expression list and put the TypeList of the nodes into the type map
    public void visit(ExpressionList node) {
		//Create the new TypeList, may be empty if not expression (eg empty paramters in function sig)
		TypeList tl = new TypeList();

        for(Expression expression : node) {
			tl.append(visitRetriveType(expression));
        }

		//put the type list into the map
        put(node, tl);
    }

    @Override
    public void visit(DeclarationList node) {
		for (Declaration d : node) {
			d.accept(this);	
		} 
    }

    @Override
    public void visit(StatementList node) {
		returnReqd = true;		
		boolean isReturn = false;
		
        for (Statement s : node) {
        	s.accept(this);
			//check if we have encountered a return statement
			isReturn = (returnReqd) ? false : true;

			//if we found a return set the returnRqd to false, cause we found it
			returnReqd = (isReturn) ? false: true;
        }
		
    }

    @Override
    public void visit(AddressOf node) {
		Symbol nodeSymbol = node.symbol();
		Type type = nodeSymbol.type();

		//Create the new address type to put in the typemap
		AddressType addrType = new AddressType(type);

		put(node, addrType);
    }

    @Override
    public void visit(LiteralBool node) {
		BoolType bt = new BoolType();
		put(node, bt); 
    }

    @Override
    public void visit(LiteralFloat node) {
		FloatType ft = new FloatType();
        put(node, ft); 
    }

    @Override
    public void visit(LiteralInt node) {
		IntType it = new IntType();
       	put(node, it);
    }

    @Override
    public void visit(VariableDeclaration node) {
		System.out.println(node.toString());
		put(node, node.symbol().type());
    }

    @Override
    public void visit(ArrayDeclaration node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(FunctionDefinition node) {
		//reset the counter determining the number of returns that are found in the ast
		returnsCount = 0;
		
		Symbol functionSymbol = node.symbol(); //first get the symbol representing the function

		//reference to the symbol of the function we are processing
		currentFuncSymb = functionSymbol;

		//Get the function arguments
		List<Symbol> functionArgs = node.arguments();
		
		//Determine the return type of the function
		FuncType funcType = (FuncType) functionSymbol.type();
		Type funcReturnType = funcType.returnType();

		//keep track of the return type of the function we are processing
		currentFuncRetType = funcReturnType;	

		String functionName = functionSymbol.name();

		//Special handling for the main function
		if(functionName.equals("main")) {
			//cant have arguments passed to main
			Type errorType = new ErrorType("Function " + functionName + " has invalid signature.");
			if(functionArgs.size() > 0){
				put(node, errorType);
				//dont need to keep processing the ast, return and print the error
				return;
			}

			//main can only return void type
			if(!funcReturnType.equivalent(new VoidType())) {
				put(node, errorType);
				//dont need to keep processing the ast, return and print the error
				return;
			}
		}
		else {
			if(functionArgs != null) {
				for(int i = 0; i < functionArgs.size(); i++) {
					Type argType = functionArgs.get(i).type();
				}
			}
		}
		
		//function signature is correct, visit the body (statement list) 
		visit(node.body());
    }

    @Override
    public void visit(Comparison node) {
        throw new RuntimeException("Implement this");
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
        throw new RuntimeException("Implement this");
    }
    
    @Override
    public void visit(Dereference node) {
		Expression expression = node.expression();
		
		Type type = visitRetriveType(expression);

		Type deref = type.deref();
		
		put(node, deref); 
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
		Type funcType = node.function().type();
		
		ExpressionList functionArgs = node.arguments();
		
		Type callArgTypeList = visitRetriveType(functionArgs);
		
		Type returnType = funcType.call(callArgTypeList);
		
		put(node, returnType); 
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
		//found a return statement, increment the count
		returnsCount += 1;

		//we found a return so signal in the global variable
        returnReqd = false; 

		//get the return arguments
		Type retType = visitRetriveType(node.argument());

		//make sure the return arguments match the current function's return arguments
		if (retType.equivalent(currentFuncRetType) == false) {  //not equivalent
			put(node, new ErrorType("Function " + currentFuncSymb.name() + " returns " + currentFuncRetType + " not " + retType + "."));
		} 
		else {
			put(node, retType);
		}
    }

    @Override
    public void visit(ast.Error node) {
        put(node, new ErrorType(node.message()));
    }
}
