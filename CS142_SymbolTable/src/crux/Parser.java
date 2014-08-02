/*
 * This is the implementation class for the compiler Parser.  The class implements
 * the basic grammar rules of the Crux language specification.  Each grammar
 * production can be found above its corresponding method call.  The parser uses
 * a recursive descent approach.  In this approach, the current production uses 
 * the current token to determine the next production to forward the call to based
 * on the first sets of the grammar production rules.  
 * 
 * @author John Palomo
 * Date: 4/21/2014
 * CS 142A: Compilers and Interpreters
 * Spring 2014
 * 
 */

package crux;

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

	public void parse() {
		initSymbolTable();
        try {
            program();
        } catch (QuitParseException q) {
            errorBuffer.append("SyntaxError(" + lineNumber() + "," + charPosition() + ")");
            errorBuffer.append("[Could not complete parsing.]");
        } catch (RedeclarationError re) {
			System.out.println("Error parsing file.");
			re.toString();
		}

    }

    // program := declaration-list EOF .
    private void program() {
		declarationList();
		expect(Token.Kind.EOF);
    }

	// declaration-list := { declaration } .
	private void declarationList() {
		while(have(NonTerminal.DECLARATION)){
			declaration();
		}
	}

	// declaration := variable-declaration | array-declaration | function-definition .
	private void declaration() {
		if(have(NonTerminal.VARIABLE_DECLARATION)){
			variableDeclaration();
		}
		else if(have(NonTerminal.ARRAY_DECLARATION)) {
			arrayDeclaration();
		}
		else if(have(NonTerminal.FUNCTION_DEFINITION)){
			functionDefintion(); 
		}
		else {
			throw new QuitParseException(reportSyntaxError(NonTerminal.DECLARATION));
		} 
	}
	
	// function-definition := "func" IDENTIFIER "(" parameter-list ")" ":" type statement-block .
	private void functionDefintion() {
		expect(Token.Kind.FUNC);
		
		//Symbol table management
		//functionEnter();
		//put the function name in the current scope
		tryDeclareSymbol(currentToken);

		//create scope for the function that includes the function name, paramter list,
		//and and state variables
		enterScope();
		//tryDeclareSymbol(currentToken); 

		expect(Token.Kind.IDENTIFIER);
		expect(Token.Kind.OPEN_PAREN);
		parameterList();
		expect(Token.Kind.CLOSE_PAREN);
		expect(Token.Kind.COLON);
		type();
		statementBlock();
		
		exitScope();
	}

	// parameter-list := [ parameter { "," parameter } ] .
	private void parameterList() {
		//optional parameters for function definition
		if(have(NonTerminal.PARAMETER)){
			parameter();

			while(accept(Token.Kind.COMMA)) { 
				parameter();
			}
		} 
	}
	
	// parameter := IDENTIFIER ":" type .
	private void parameter() {
			
		tryDeclareSymbol(currentToken);
		
		expect(Token.Kind.IDENTIFIER);
		expect(Token.Kind.COLON);
		type(); 
	}
	
	// statement-block := "{" statement-list "}" .
	private void statementBlock() {
		//enterScope();

		expect(Token.Kind.OPEN_BRACE);
		statementList();
		expect(Token.Kind.CLOSE_BRACE);

		//exitScope();
	}

	// statement-list := { statement } .
	private void statementList() {
		while(have(NonTerminal.STATEMENT)){
			statement();
		}
	}

	// statement := variable-declaration | call-statement | assignment-statement | if-statement | while-statement | return-statement .
	private void statement() {
		
		if(have(NonTerminal.VARIABLE_DECLARATION)){
			variableDeclaration();
		}
		else if(have(NonTerminal.CALL_STATEMENT)){
			callStatement();
		}
		else if(have(NonTerminal.ASSIGNMENT_STATEMENT)){
			assignmentStatement();	
		}
		else if(have(NonTerminal.IF_STATEMENT)){
			ifStatement();
		}
		else if(have(NonTerminal.WHILE_STATEMENT)){
			whileStatement();
		}
		else if(have(NonTerminal.RETURN_STATEMENT)){
			returnStatement();
		}else {
        	throw new QuitParseException(reportSyntaxError(NonTerminal.STATEMENT));
		}
	}

	// assignment-statement := "let" designator "=" expression0 ";"
	private void assignmentStatement() {
		expect(Token.Kind.LET);	
		designator();
		expect (Token.Kind.ASSIGN);
		expression0();
		expect(Token.Kind.SEMICOLON);
	}

	// expression0 := expression1 [ op0 expression1 ] .
	private void expression0() {
		expression1();
		if(have(NonTerminal.OP0)){
			op0();
			expression1();
		}
	}

	// expression1 := expression2 { op1  expression2 } .
	private void expression1() {
		expression2();
		while(have(NonTerminal.OP1)){
			op1();
			expression2();
		}
	}

	// expression2 := expression3 { op2 expression3 } .
	private void expression2() {
		expression3();
		while(have(NonTerminal.OP2)) {
			op2();
			expression3();
		}
	}

	// expression3 := "not" expression3 | "(" expression0 ")" | designator | call-expression | literal .
	private void expression3() {

		if (accept(Token.Kind.NOT)) {
			expression3();
		}
		else if (accept(Token.Kind.OPEN_PAREN)) {
			expression0();
			expect(Token.Kind.CLOSE_PAREN);
		}
		else if(have(NonTerminal.DESIGNATOR)) {
			designator();
		}
		else if(have(NonTerminal.CALL_EXPRESSION)) {
			callExpression();
		}
		else if(have(NonTerminal.LITERAL)) {
			literal();
		}
		else { 
			throw new QuitParseException(reportSyntaxError(NonTerminal.EXPRESSION3));
		}
	}

	// call-expression := "::" IDENTIFIER "(" expression-list ")" .
	private void callExpression() {
		expect(Token.Kind.CALL);

		tryResolveSymbol(currentToken);
		
		expect(Token.Kind.IDENTIFIER);
		expect(Token.Kind.OPEN_PAREN);
		expressionList();
		expect(Token.Kind.CLOSE_PAREN);
	}

	// call-statement := call-expression ";"
	private void callStatement() {
		callExpression();
		expect(Token.Kind.SEMICOLON);
	}

	// expression-list := [ expression0 { "," expression0 } ] .
	private void expressionList() {
		if(have(NonTerminal.EXPRESSION0)) {
			expression0();

			while(accept(Token.Kind.COMMA)) {
				expression0();
			}
		}
	}
	
	// if-statement := "if" expression0 statement-block [ "else" statement-block ] .
	private void ifStatement() {
		expect(Token.Kind.IF);

		enterScope();

		expression0();
		statementBlock();

		exitScope();

		if(accept(Token.Kind.ELSE)) {
			enterScope();
			statementBlock();
			exitScope();
		}
	}

	// while-statement := "while" expression0 statement-block .
	private void whileStatement() {
		expect(Token.Kind.WHILE);

		enterScope();

		expression0();
		statementBlock();

		exitScope();
	}

	// return-statement := "return" expression0 ";" .
	private void returnStatement() {
		expect(Token.Kind.RETURN);
		expression0();
		expect(Token.Kind.SEMICOLON);
	}

    // literal := INTEGER | FLOAT | TRUE | FALSE .
	private void literal() {
		if(!accept(Token.Kind.INTEGER) && !accept(Token.Kind.FLOAT) && !accept(Token.Kind.TRUE) && !accept(Token.Kind.FALSE) ) {
			throw new QuitParseException(reportSyntaxError(NonTerminal.LITERAL));
		}
	}

	// designator := IDENTIFIER { "[" expression0 "]" } .
	private void designator() {
		tryResolveSymbol(currentToken);
		expect(Token.Kind.IDENTIFIER);
		//optional expression0
		while(accept(Token.Kind.OPEN_BRACKET)) {
			expression0();
			expect(Token.Kind.CLOSE_BRACKET);
		}
	}
	
	// variable-declaration := "var" IDENTIFIER ":" type ";"
	private void variableDeclaration() {
		expect(Token.Kind.VAR);

		tryDeclareSymbol(currentToken);
		
		expect(Token.Kind.IDENTIFIER);	
		expect(Token.Kind.COLON);
		type();	
		expect(Token.Kind.SEMICOLON);
	}

	// array-declaration := "array" IDENTIFIER ":" type "[" INTEGER "]" { "[" INTEGER "]" } ";"
	private void arrayDeclaration() {
		expect(Token.Kind.ARRAY);
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
        //assert(ident.getKind() == Token.Kind.IDENTIFIER);
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