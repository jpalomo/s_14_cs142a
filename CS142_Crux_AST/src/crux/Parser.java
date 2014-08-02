/*
 * This is the implementation class for the compiler Parser.  The class implements
 * the basic grammar rules of the Crux language specification.  Each grammar
 * production can be found above its corresponding method call.  The parser uses
 * a recursive descent approach.  In this approach, the current production uses 
 * the current token to determine the next production to forward the call to based
 * on the first sets of the grammar production rules.  
 * 
 * @author John Palomo
 * Date: 5/14/2014
 * CS 142A: Compilers and Interpreters
 * Spring 2014
 * 
 */

package crux;

import ast.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    public static String studentName = "John Palomo";
    public static String studentID = "60206611";
    public static String uciNetID = "jmpalomo";
    
	//Symbol Table Management
    private SymbolTable symbolTable;

	// Grammar Rule Reporting 
    private int parseTreeRecursionDepth;
    private StringBuffer parseTreeBuffer;
    private StringBuffer errorBuffer; 

    private Scanner scanner;
    private Token currentToken;
    
    public Parser(Scanner scanner) {
		this.scanner = scanner;
		this.parseTreeBuffer = new StringBuffer();
		this.errorBuffer = new StringBuffer();
		this.parseTreeRecursionDepth = 0;
		this.currentToken = scanner.next();
    }

	public Command parse() {
		initSymbolTable();

		Command command = null; 
        try {
            command = program();
        } catch (QuitParseException q) {
            errorBuffer.append("SyntaxError(" + lineNumber() + "," + charPosition() + ")");
            errorBuffer.append("[Could not complete parsing.]");
        } catch (RedeclarationError re) {
			System.out.println("Error parsing file.");
			re.toString();
		}

		return command;
    }

    // program := declaration-list EOF .
    private DeclarationList program() {
		DeclarationList dl = declarationList();
		expect(Token.Kind.EOF);
		return dl;
    }

	// declaration-list := { declaration } .
	private DeclarationList declarationList() {
		//this will be the main declaration list
		DeclarationList declarationList = new DeclarationList(currentToken.lineNumber(), currentToken.charPosition());

		while(have(NonTerminal.DECLARATION)){ 
			declarationList.add(declaration());
		}

		return declarationList;
	}

	// declaration := variable-declaration | array-declaration | function-definition .
	private Declaration declaration() {

		Declaration declaration = null;
		if(have(NonTerminal.VARIABLE_DECLARATION)){
			declaration = variableDeclaration();
		}
		else if(have(NonTerminal.ARRAY_DECLARATION)) {
			declaration = arrayDeclaration();
		}
		else if(have(NonTerminal.FUNCTION_DEFINITION)){
			declaration = functionDefintion(); 
		}
		else {
			throw new QuitParseException(reportSyntaxError(NonTerminal.DECLARATION));
		} 

		return declaration;

	}
	
	// function-definition := "func" IDENTIFIER "(" parameter-list ")" ":" type statement-block .
	private FunctionDefinition functionDefintion() {
		Token functionToken = expectRetrieve(Token.Kind.FUNC);

		//put the function name in the current scope
		Symbol functionSymbol = tryDeclareSymbol(currentToken);

		//create scope for the function that includes the function name, paramter list, and and state variables
		enterScope();

		expect(Token.Kind.IDENTIFIER);
		expect(Token.Kind.OPEN_PAREN);

		List<Symbol> parameterList = parameterList();

		expect(Token.Kind.CLOSE_PAREN);
		expect(Token.Kind.COLON);

		//return type of a function
		type();

		StatementList body = statementBlock();
		
		FunctionDefinition functionDef = new FunctionDefinition(functionToken.lineNumber(), functionToken.charPosition(), functionSymbol, parameterList, body);
		exitScope();

		return functionDef;
	}

	// parameter-list := [ parameter { "," parameter } ] .
	private List<Symbol> parameterList() {
		List<Symbol> params = new ArrayList<Symbol>();

		//optional parameters for function definition
		if(have(NonTerminal.PARAMETER)){
			params.add(parameter());

			while(accept(Token.Kind.COMMA)) { 
				params.add(parameter());
			}
		} 

		return params;
	}
	
	// parameter := IDENTIFIER ":" type .
	private Symbol parameter() {
			
		Symbol symbol = tryDeclareSymbol(currentToken);
		
		expect(Token.Kind.IDENTIFIER);
		expect(Token.Kind.COLON);
		type(); 
		
		return symbol;
	}
	
	// statement-block := "{" statement-list "}" .
	private StatementList statementBlock() {
		expect(Token.Kind.OPEN_BRACE);

		StatementList statementList = statementList();

		expect(Token.Kind.CLOSE_BRACE); 

		return statementList;
	}

	// statement-list := { statement } .
	private StatementList statementList() {
		StatementList statementList = new StatementList(currentToken.lineNumber(), currentToken.charPosition());

		while(have(NonTerminal.STATEMENT)){
			statementList.add(statement());
		}

		return statementList;
	}

	// statement := variable-declaration | call-statement | assignment-statement | if-statement | while-statement | return-statement .
	private Statement statement() {
		if(have(NonTerminal.VARIABLE_DECLARATION)){
			return variableDeclaration();
		}
		else if(have(NonTerminal.CALL_STATEMENT)){
			return callStatement();
		}
		else if(have(NonTerminal.ASSIGNMENT_STATEMENT)){
			return assignmentStatement();	
		}
		else if(have(NonTerminal.IF_STATEMENT)){
			return ifStatement();
		}
		else if(have(NonTerminal.WHILE_STATEMENT)){
			return whileStatement();
		}
		else if(have(NonTerminal.RETURN_STATEMENT)){
			return returnStatement();
		}else {
        	throw new QuitParseException(reportSyntaxError(NonTerminal.STATEMENT));
		}
	}

	// assignment-statement := "let" designator "=" expression0 ";"
	private Assignment assignmentStatement() {
		Token assignToken = expectRetrieve(Token.Kind.LET);	

		Symbol assignSym = new Symbol(currentToken.lexeme());
		AddressOf destAddr = new AddressOf(currentToken.lineNumber(), currentToken.charPosition(), assignSym);

		designator();

		expect (Token.Kind.ASSIGN);

		Expression source = expression0();

		expect(Token.Kind.SEMICOLON);

		Assignment assignment = new Assignment(assignToken.lineNumber(), assignToken.charPosition(), destAddr, source); 
		return assignment;
	}

	// expression0 := expression1 [ op0 expression1 ] .
	private Expression expression0() {
		Expression leftSideExpr = expression1();

		//relational operators
		if(have(NonTerminal.OP0)){
			Token opToken = currentToken;
			Expression rightSideExpr = null;

			//operator for the expression
			op0();

			//right-hand side
			rightSideExpr = expression1();
			leftSideExpr = Command.newExpression(leftSideExpr, opToken, rightSideExpr);
		}
		return leftSideExpr; 
	}

	// expression1 := expression2 { op1  expression2 } .
	private Expression expression1() {
		Expression leftSideExpr = expression2();

		//add, sub, or
		while(have(NonTerminal.OP1)){
			Token opToken = currentToken;
			Expression rightSideExpr = null;

			op1();
			
			rightSideExpr = expression2();
			leftSideExpr = Command.newExpression(leftSideExpr, opToken, rightSideExpr);
		} 
		return leftSideExpr;
	}

	// expression2 := expression3 { op2 expression3 } .
	private Expression expression2() {
		Expression leftSideExpr = expression3();
		
		//mult, div, and
		while(have(NonTerminal.OP2)) {
			Token opToken = currentToken;
			Expression rightSideExpr = null;

			op2();
			
			rightSideExpr = expression3();
			leftSideExpr = Command.newExpression(leftSideExpr, opToken, rightSideExpr);
		}
		return leftSideExpr;
	}

	// expression3 := "not" expression3 | "(" expression0 ")" | designator | call-expression | literal .
	private Expression expression3() {
		Token token = currentToken;
		Expression expression = null;

		if (accept(Token.Kind.NOT)) {
			Expression rightSideExpr = expression3();
			expression = Command.newExpression(rightSideExpr, token, null); 
		}
		else if (accept(Token.Kind.OPEN_PAREN)) {
			expression = expression0();
			expect(Token.Kind.CLOSE_PAREN);
		}
		else if(have(NonTerminal.DESIGNATOR)) {
			expression = designator();
		}
		else if(have(NonTerminal.CALL_EXPRESSION)) {
			expression = callExpression();
		}
		else if(have(NonTerminal.LITERAL)) {
			expression = literal();
		}
		else { 
			throw new QuitParseException(reportSyntaxError(NonTerminal.EXPRESSION3));
		}

		return expression;
	}

	// call-expression := "::" IDENTIFIER "(" expression-list ")" .
	private Call callExpression() {
		Token funcToken = expectRetrieve(Token.Kind.CALL);

		Symbol funcSymbol = tryResolveSymbol(currentToken);
		
		expect(Token.Kind.IDENTIFIER);
		expect(Token.Kind.OPEN_PAREN);

		ExpressionList exprList = expressionList();

		expect(Token.Kind.CLOSE_PAREN);

		//create the call expression
		return new Call(funcToken.lineNumber(), funcToken.charPosition(), funcSymbol, exprList);
	}

	// call-statement := call-expression ";"
	private Call callStatement() {
		Call call = callExpression();

		expect(Token.Kind.SEMICOLON);

		return call;
	}

	// expression-list := [ expression0 { "," expression0 } ] .
	private ExpressionList expressionList() {
		ExpressionList exprList = new ExpressionList(currentToken.lineNumber(), currentToken.charPosition());

		if(have(NonTerminal.EXPRESSION0)) {
			
			exprList.add(expression0());

			while(accept(Token.Kind.COMMA)) { // || have(NonTerminal.OP0) || have(NonTerminal.OP1) || have(NonTerminal.OP2)) {
				exprList.add(expression0());
			}
		}

		return exprList;
	}
	
	// if-statement := "if" expression0 statement-block [ "else" statement-block ] .
	private IfElseBranch ifStatement() {

		Token iEToken = expectRetrieve(Token.Kind.IF);
		
		enterScope();

		Expression iEExpression = expression0();

		StatementList ieStatementList = statementBlock();

		exitScope();

		StatementList elseStatementList = new StatementList(currentToken.lineNumber(), currentToken.charPosition());
		if(accept(Token.Kind.ELSE)) {
			enterScope();
			elseStatementList = statementBlock();
			exitScope();
		}

		return new IfElseBranch(iEToken.lineNumber(), iEToken.charPosition(), iEExpression, ieStatementList, elseStatementList);
	}

	// while-statement := "while" expression0 statement-block .
	private WhileLoop whileStatement() {
		Token whileToken = expectRetrieve(Token.Kind.WHILE);

		enterScope();

		Expression whileExpr = expression0();
		StatementList statementList = statementBlock();

		exitScope();

		return new WhileLoop(whileToken.lineNumber(), whileToken.charPosition(), whileExpr, statementList);
	}

	// return-statement := "return" expression0 ";" .
	private Return returnStatement() {
		Return returnStmnt = null;
		Token returnToken = expectRetrieve(Token.Kind.RETURN);

		Expression retExpression = expression0();

		returnStmnt = new Return(returnToken.lineNumber(), returnToken.charPosition(), retExpression);

		expect(Token.Kind.SEMICOLON);

		return returnStmnt;
	}

    // literal := INTEGER | FLOAT | TRUE | FALSE .
	private Expression literal() {
		Expression expr = null;
		Token literalToken = currentToken;

		if(!accept(Token.Kind.INTEGER) && !accept(Token.Kind.FLOAT) && !accept(Token.Kind.TRUE) && !accept(Token.Kind.FALSE) ) {
			throw new QuitParseException(reportSyntaxError(NonTerminal.LITERAL));
		}
		
		expr = Command.newLiteral(literalToken);
		
		return expr;
	}

	// designator := IDENTIFIER { "[" expression0 "]" } .
	private Dereference designator() {
		Symbol exprSymbol = tryResolveSymbol(currentToken);
		
		AddressOf addrDeref = new AddressOf(currentToken.lineNumber(), currentToken.charPosition(), exprSymbol);

		expectRetrieve(Token.Kind.IDENTIFIER);
		
		//optional expression0
		Index index = null;
		while(accept(Token.Kind.OPEN_BRACKET)) {
			if(index == null) {
				index = new Index(currentToken.lineNumber(), currentToken.charPosition(), addrDeref, expression0());
				expect(Token.Kind.CLOSE_BRACKET);
				continue;
			}

			index = new Index(currentToken.lineNumber(), currentToken.charPosition(), index, expression0());

			//get the index
			expect(Token.Kind.CLOSE_BRACKET);
		}

		Expression exprToSet = addrDeref;
		if (index != null) {
			exprToSet = index;
		}

		return  new Dereference(addrDeref.lineNumber(), addrDeref.charPosition(), exprToSet);
	}
	
	// variable-declaration := "var" IDENTIFIER ":" type ";"
	private VariableDeclaration variableDeclaration() {
		Token varToken = expectRetrieve(Token.Kind.VAR);

		Symbol symbol = tryDeclareSymbol(currentToken);

		VariableDeclaration variableDecl = new VariableDeclaration(varToken.lineNumber(), varToken.charPosition(), symbol);
		
		expect(Token.Kind.IDENTIFIER);	
		expect(Token.Kind.COLON);

		type();	

		expect(Token.Kind.SEMICOLON);

		return variableDecl;
	}

	// array-declaration := "array" IDENTIFIER ":" type "[" INTEGER "]" { "[" INTEGER "]" } ";"
	private Declaration arrayDeclaration() {
		Token arrayToken = expectRetrieve(Token.Kind.ARRAY);

		Symbol arraySym = tryDeclareSymbol(currentToken);

		ArrayDeclaration arrayDecl = new ArrayDeclaration(arrayToken.lineNumber(), arrayToken.charPosition(), arraySym);

		expect(Token.Kind.IDENTIFIER);

		expect(Token.Kind.COLON);
		type();

		//at least one
		expect(Token.Kind.OPEN_BRACKET);
		do {
			expect(Token.Kind.INTEGER);	
			expect(Token.Kind.CLOSE_BRACKET);
		} while (accept(Token.Kind.OPEN_BRACKET)); 

		expect(Token.Kind.SEMICOLON);
		
		return arrayDecl;
	}

	// type := IDENTIFIER .
	private void type() {
		expect(Token.Kind.IDENTIFIER);
	}
	
	// op0 := ">=" | "<=" | "!=" | "==" | ">" | "<" .
	private void op0() {
		if (accept(Token.Kind.GREATER_EQUAL));
		else if(accept(Token.Kind.LESSER_EQUAL));	
		else if(accept(Token.Kind.NOT_EQUAL));
		else if(accept(Token.Kind.EQUAL));
		else if(accept(Token.Kind.GREATER_THAN));
		else if(accept(Token.Kind.LESS_THAN));
		else {
			throw new QuitParseException(reportSyntaxError(NonTerminal.OP0));
		}
   } 	

   // op1 := "+" | "-" | "or" .
   private void op1() { 
		if(accept(Token.Kind.ADD));	
		else if(accept(Token.Kind.SUB));	
		else if(accept(Token.Kind.OR));	
		else {
			throw new QuitParseException(reportSyntaxError(NonTerminal.OP1));
		}
   }

	// op2 := "*" | "/" | "and" .
	private void op2() { 
		if(accept(Token.Kind.MUL));
		else if(accept(Token.Kind.DIV));
		else if(accept(Token.Kind.AND));
		else {
			throw new QuitParseException(reportSyntaxError(NonTerminal.OP2));
		}
   }
    
	/************ ********** Helper Methods ************ **********/
	private boolean have(Token.Kind kind) {
		return currentToken.getKind().equals(kind);
    }

	private boolean have(NonTerminal nt) {
        return nt.firstSet().contains(currentToken.getKind());
    }
    
    private boolean expect(Token.Kind kind) {
        if (accept(kind))
            return true;
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
        //return false;
    }

    private boolean expect(NonTerminal nt) {
        if (accept(nt))
            return true;
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
        //return false;
    }
	
    private boolean accept(Token.Kind kind) {
        if (have(kind)) {
            currentToken = scanner.next();
            return true;
        }
        return false;
    }    
    
    private boolean accept(NonTerminal nonTerm) {
        if (have(nonTerm)) {
            currentToken = scanner.next();
            return true;
        }
        return false;
    }
	
	private int lineNumber() {
        return currentToken.lineNumber();
    }
    
    private int charPosition() {
        return currentToken.charPosition();
    }

	@Deprecated
	private void enterRule(NonTerminal nonTerminal) {
        String lineData = new String();
		//pad the string
        for(int i = 0; i < parseTreeRecursionDepth; i++) {
            lineData += "  ";
        }
        lineData += nonTerminal.name();
        //System.out.println("descending " + lineData);
        parseTreeBuffer.append(lineData + "\n");
        parseTreeRecursionDepth++;
    }

	@Deprecated
    private void exitRule(NonTerminal nonTerminal) {
        parseTreeRecursionDepth--;
    }
    
    public String parseTreeReport() {
        return parseTreeBuffer.toString();
    }
	
	private String reportSyntaxError(NonTerminal nt) {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected a token from " + nt.name() + " but got " + currentToken.getKind() + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }
     
    private String reportSyntaxError(Token.Kind kind) {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected " + kind + " but got " + currentToken.getKind() + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }
    
    public String errorReport() {
        return errorBuffer.toString();
    }
    
	/**
	 * Already implemented method that just whether the error buffer string is
	 * empty or not.
	 * 
	 * @return - boolean value indicating whether there was an error or not
	 */
    public boolean hasError() {
        return errorBuffer.length() != 0;
    }
    
    private class QuitParseException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public QuitParseException(String errorMessage) {
            super(errorMessage);
        }
    }

	// SymbolTable Management ==========================
	private void functionEnter() {
		tryDeclareSymbol(currentToken);
		enterScope();
		tryDeclareSymbol(currentToken); 
	}

    private void initSymbolTable() {
		symbolTable = new SymbolTable();
    }
    
    private void enterScope() {
		symbolTable.addNewScope();
    }
    
    private void exitScope() {
		symbolTable.removeCurrentScope();
    }

    private Symbol tryResolveSymbol(Token ident) {
        assert(ident.getKind() == Token.Kind.IDENTIFIER);
        String name = ident.lexeme();
        try {
            return symbolTable.lookup(name);
        } catch (SymbolNotFoundError e) {
            String message = reportResolveSymbolError(name, ident.lineNumber(), ident.charPosition());
            return new ErrorSymbol(message);
        }
    }

    private String reportResolveSymbolError(String name, int lineNum, int charPos) {
        String message = "ResolveSymbolError(" + lineNum + "," + charPos + ")[Could not find " + name + ".]";
        errorBuffer.append(message + "\n");
        errorBuffer.append(symbolTable.toString() + "\n");
        return message;
    }

	/**
	 * Adds a token IDENTIFIER to the symbol table.
	 * 
	 * @param ident
	 * @return 
	 */
    private Symbol tryDeclareSymbol(Token ident) {
        String name = ident.lexeme();
        try {
            return symbolTable.insert(name);
        } catch (RedeclarationError re) {
		 	String message = reportDeclareSymbolError(name, ident.lineNumber(), ident.charPosition());
            return new ErrorSymbol(message); 
        }
    }

    private String reportDeclareSymbolError(String name, int lineNum, int charPos) {
        String message = "DeclareSymbolError(" + lineNum + "," + charPos + ")[" + name + " already exists.]";
        errorBuffer.append(message + "\n");
        errorBuffer.append(symbolTable.toString() + "\n");
        return message;
    }    

    private Token expectRetrieve(Token.Kind kind) {
        Token tok = currentToken;
        if (accept(kind))
            return tok;
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
        //return ErrorToken(errorMessage);
    }
        
    private Token expectRetrieve(NonTerminal nt) {
        Token tok = currentToken;
        if (accept(nt))
            return tok;
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
        //return ErrorToken(errorMessage);
    } 
}