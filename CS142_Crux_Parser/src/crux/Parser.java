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
        try {
            program();
        } catch (QuitParseException q) {
            errorBuffer.append("SyntaxError(" + lineNumber() + "," + charPosition() + ")");
            errorBuffer.append("[Could not complete parsing.]");
        }
    }

    // program := declaration-list EOF .
    private void program() {
		enterRule(NonTerminal.PROGRAM);

		declarationList();
		expect(Token.Kind.EOF);

		exitRule(NonTerminal.PROGRAM);
    }

	// declaration-list := { declaration } .
	private void declarationList() {
		enterRule(NonTerminal.DECLARATION_LIST);
		
		while(have(NonTerminal.DECLARATION)){
			declaration();
		}
		
		exitRule(NonTerminal.DECLARATION_LIST);
	}

	// declaration := variable-declaration | array-declaration | function-definition .
	private void declaration() {
		enterRule(NonTerminal.DECLARATION);

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

		exitRule(NonTerminal.DECLARATION);
	}
	
	// function-definition := "func" IDENTIFIER "(" parameter-list ")" ":" type statement-block .
	private void functionDefintion() {
		enterRule(NonTerminal.FUNCTION_DEFINITION);

		expect(Token.Kind.FUNC);
		expect(Token.Kind.IDENTIFIER);
		expect(Token.Kind.OPEN_PAREN);
		parameterList();
		expect(Token.Kind.CLOSE_PAREN);
		expect(Token.Kind.COLON);
		type();
		statementBlock();

		exitRule(NonTerminal.FUNCTION_DEFINITION);
	}

	// parameter-list := [ parameter { "," parameter } ] .
	private void parameterList() {
		enterRule(NonTerminal.PARAMETER_LIST);

		//optional parameters for function definition
		if(have(NonTerminal.PARAMETER)){
			parameter();

			while(accept(Token.Kind.COMMA)) { 
				parameter();
			}
		}

		exitRule(NonTerminal.PARAMETER_LIST);
	}
	
	// parameter := IDENTIFIER ":" type .
	private void parameter() {
		enterRule(NonTerminal.PARAMETER);

		expect(Token.Kind.IDENTIFIER);
		expect(Token.Kind.COLON);
		type();

		exitRule(NonTerminal.PARAMETER); 
	}
	
	// statement-block := "{" statement-list "}" .
	private void statementBlock() {
		enterRule(NonTerminal.STATEMENT_BLOCK);

		expect(Token.Kind.OPEN_BRACE);
		statementList();
		expect(Token.Kind.CLOSE_BRACE);

		exitRule(NonTerminal.STATEMENT_BLOCK); 
	}

	// statement-list := { statement } .
	private void statementList() {
		enterRule(NonTerminal.STATEMENT_LIST);

		while(have(NonTerminal.STATEMENT)){
			statement();
		}
		
		exitRule(NonTerminal.STATEMENT_LIST);
	}

	// statement := variable-declaration | call-statement | assignment-statement | if-statement | while-statement | return-statement .
	private void statement() {
		enterRule(NonTerminal.STATEMENT);
		
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

		exitRule(NonTerminal.STATEMENT);
	}

	// assignment-statement := "let" designator "=" expression0 ";"
	private void assignmentStatement() {
		enterRule(NonTerminal.ASSIGNMENT_STATEMENT);

		expect(Token.Kind.LET);	
		designator();
		expect (Token.Kind.ASSIGN);
		expression0();
		expect(Token.Kind.SEMICOLON);

		exitRule(NonTerminal.ASSIGNMENT_STATEMENT);
	}

	// expression0 := expression1 [ op0 expression1 ] .
	private void expression0() {
		enterRule(NonTerminal.EXPRESSION0);

		expression1();
		if(have(NonTerminal.OP0)){
			op0();
			expression1();
		}

		exitRule(NonTerminal.EXPRESSION0);
	}

	// expression1 := expression2 { op1  expression2 } .
	private void expression1() {
		enterRule(NonTerminal.EXPRESSION1);

		expression2();
		while(have(NonTerminal.OP1)){
			op1();
			expression2();
		}

		exitRule(NonTerminal.EXPRESSION1);
	}

	// expression2 := expression3 { op2 expression3 } .
	private void expression2() {
		enterRule(NonTerminal.EXPRESSION2);

		expression3();
		while(have(NonTerminal.OP2)) {
			op2();
			expression3();
		}

		exitRule(NonTerminal.EXPRESSION2);
	}

	// expression3 := "not" expression3 | "(" expression0 ")" | designator | call-expression | literal .
	private void expression3() {
		enterRule(NonTerminal.EXPRESSION3);

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

		exitRule(NonTerminal.EXPRESSION3);
	}

	// call-expression := "::" IDENTIFIER "(" expression-list ")" .
	private void callExpression() {
		enterRule(NonTerminal.CALL_EXPRESSION);
		
		expect(Token.Kind.CALL);
		expect(Token.Kind.IDENTIFIER);
		expect(Token.Kind.OPEN_PAREN);
		expressionList();
		expect(Token.Kind.CLOSE_PAREN);

		exitRule(NonTerminal.CALL_EXPRESSION); 
	}

	// call-statement := call-expression ";"
	private void callStatement() {
		enterRule(NonTerminal.CALL_STATEMENT);
		
		callExpression();
		expect(Token.Kind.SEMICOLON);

		exitRule(NonTerminal.CALL_STATEMENT);
	}

	// expression-list := [ expression0 { "," expression0 } ] .
	private void expressionList() {
		enterRule(NonTerminal.EXPRESSION_LIST);

		if(have(NonTerminal.EXPRESSION0)) {
			expression0();

			while(accept(Token.Kind.COMMA)) {
				expression0();
			}
		}

		exitRule(NonTerminal.EXPRESSION_LIST);
	}
	
	// if-statement := "if" expression0 statement-block [ "else" statement-block ] .
	private void ifStatement() {
		enterRule(NonTerminal.IF_STATEMENT);

		expect(Token.Kind.IF);
		expression0();
		statementBlock();
		if(accept(Token.Kind.ELSE)) {
			statementBlock();
		}

		exitRule(NonTerminal.IF_STATEMENT);
	}

	// while-statement := "while" expression0 statement-block .
	private void whileStatement() {
		enterRule(NonTerminal.WHILE_STATEMENT);

		expect(Token.Kind.WHILE);
		expression0();
		statementBlock();

		exitRule(NonTerminal.WHILE_STATEMENT);
	}

	// return-statement := "return" expression0 ";" .
	private void returnStatement() {
		enterRule(NonTerminal.RETURN_STATEMENT);

		expect(Token.Kind.RETURN);
		expression0();
		expect(Token.Kind.SEMICOLON);

		exitRule(NonTerminal.RETURN_STATEMENT);
	}

    // literal := INTEGER | FLOAT | TRUE | FALSE .
	private void literal() {
		enterRule(NonTerminal.LITERAL);
		if(accept(Token.Kind.INTEGER) || accept(Token.Kind.FLOAT) || accept(Token.Kind.TRUE) || accept(Token.Kind.FALSE) ) {
			exitRule(NonTerminal.LITERAL);
		}
		else {
			throw new QuitParseException(reportSyntaxError(NonTerminal.LITERAL));
		}
	}

	// designator := IDENTIFIER { "[" expression0 "]" } .
	private void designator() {
		enterRule(NonTerminal.DESIGNATOR);

		expect(Token.Kind.IDENTIFIER);
		//optional expression0
		while(accept(Token.Kind.OPEN_BRACKET)) {
			expression0();
			expect(Token.Kind.CLOSE_BRACKET);
		}

		exitRule(NonTerminal.DESIGNATOR);
	}
	
	// variable-declaration := "var" IDENTIFIER ":" type ";"
	private void variableDeclaration() {
		enterRule(NonTerminal.VARIABLE_DECLARATION);

		expect(Token.Kind.VAR);
		expect(Token.Kind.IDENTIFIER);	
		expect(Token.Kind.COLON);
		type();	
		expect(Token.Kind.SEMICOLON);

		exitRule(NonTerminal.VARIABLE_DECLARATION);
	}

	// array-declaration := "array" IDENTIFIER ":" type "[" INTEGER "]" { "[" INTEGER "]" } ";"
	private void arrayDeclaration() {
		enterRule(NonTerminal.ARRAY_DECLARATION);

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

		exitRule(NonTerminal.ARRAY_DECLARATION);
	}

	// type := IDENTIFIER .
	private void type() {
		enterRule(NonTerminal.TYPE);

		expect(Token.Kind.IDENTIFIER);

		exitRule(NonTerminal.TYPE);
	}
	
	// op0 := ">=" | "<=" | "!=" | "==" | ">" | "<" .
	private void op0() {
		enterRule(NonTerminal.OP0);

		if (accept(Token.Kind.GREATER_EQUAL));
		else if(accept(Token.Kind.LESSER_EQUAL));	
		else if(accept(Token.Kind.NOT_EQUAL));
		else if(accept(Token.Kind.EQUAL));
		else if(accept(Token.Kind.GREATER_THAN));
		else if(accept(Token.Kind.LESS_THAN));
		else {
			throw new QuitParseException(reportSyntaxError(NonTerminal.OP0));
		}

		exitRule(NonTerminal.OP0);
   } 	

   // op1 := "+" | "-" | "or" .
   private void op1() { 
		enterRule(NonTerminal.OP1);
		
		if(accept(Token.Kind.ADD));	
		else if(accept(Token.Kind.SUB));	
		else if(accept(Token.Kind.OR));	
		else {
			throw new QuitParseException(reportSyntaxError(NonTerminal.OP1));
		}

		exitRule(NonTerminal.OP1);
   }

	// op2 := "*" | "/" | "and" .
	private void op2() { 
		enterRule(NonTerminal.OP2);
		
		if(accept(Token.Kind.MUL));
		else if(accept(Token.Kind.DIV));
		else if(accept(Token.Kind.AND));
		else {
			throw new QuitParseException(reportSyntaxError(NonTerminal.OP2));
		}

		exitRule(NonTerminal.OP2);
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
}