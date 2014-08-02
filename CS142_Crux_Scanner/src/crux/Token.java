/**
 * This class acts as a factory class that instantiates tokens that fall into 
 * the classes defined by the crux language, identifier, keyword, EOF, float, integer,
 * and special_chars which consists of '>=', '==', etc.  All other lexemes/chars
 * found in the source that do not match our grammar will be flagged as invalid
 * tokens.
 * @author Palomo
 */
package crux;

import java.util.HashMap;
import java.util.Map;

public class Token { 
	public static enum Kind {
		/** Reserved Words **/
		AND("and"),
		OR("or"),
		NOT("not"),
		LET("let"),
		VAR("var"),
		ARRAY("array"),
		FUNC("func"),
		IF("if"),
		ELSE("else"),
		WHILE("while"),
		TRUE("true"),
		FALSE("false"),
		RETURN("return"),

		/** Special Meaning Words **/
		OPEN_PAREN("("),
		CLOSE_PAREN(")"),
		OPEN_BRACE("{"),
		CLOSE_BRACE("}"),
		OPEN_BRACKET("["),
		CLOSE_BRACKET("]"),
		
		ADD("+"),
		SUB("-"),
		MUL("*"),
		DIV("/"),
		GREATER_EQUAL(">="),
		LESSER_EQUAL("<="),
		NOT_EQUAL("!="),
		EQUAL("=="),
		GREATER_THAN(">"),
		LESS_THAN("<"),
		ASSIGN("="),
		COMMA(","),
		SEMICOLON(";"),
		COLON(":"),
		CALL("::"),
		
		IDENTIFIER(),
		INTEGER(),
		FLOAT(),
		ERROR(),
		EOF("-1");
				
		private String default_lexeme;
		
		Kind() {
        	default_lexeme = null;
		}
		
		Kind(String lexeme) {
        	default_lexeme = lexeme;
		}
		
		public boolean hasStaticLexeme() {
        	return default_lexeme != null;
		}
                
		public String getDefaultLexeme() {
			return default_lexeme;
		}
	}

	public static final String studentName = "John Palomo";
    public static final String studentID = "60206611";
    public static final String uciNetID = "jmpalomo";	

	private int lineNum;
	private int charPos;
	private Kind kind;
	private String lexeme = "";
	private static Map<Kind,Integer> lexemesToPrint;

	/* static initializer populates the map with a list of Kind that need their
	 * associted lexemes printed when the token method toString is called
	 */
	static {
		lexemesToPrint = new HashMap<Kind, Integer>();
		lexemesToPrint.put(Kind.IDENTIFIER, 1);
		lexemesToPrint.put(Kind.INTEGER, 1);
		lexemesToPrint.put(Kind.FLOAT, 1);
		lexemesToPrint.put(Kind.ERROR, 1);
	}

	/**
	 * This is the driving private constructor for all the static factory 
	 * token methods.  It will initialized to an error token with the string:
	 * "No lexeme given".
	 * 
	 * @param lineNum - the line number where the lexeme was found
	 * @param charPos - the starting character position of the lexeme
	 */
	private Token(int lineNum, int charPos) {
		this.lineNum = lineNum;
		this.charPos = charPos;

		// if we don't match anything, signal error
		this.kind = Kind.ERROR;
		this.lexeme = "No Lexeme Given";
	}
	
	public static Token EOF(int linePos, int charPos) {
		Token token = new Token(linePos, charPos);
		token.kind = Kind.EOF;
		return token;
	}

	public static Token INDENTIFIER_OR_KEYWORD(String lexeme, int linePos, int charPos) {
		Token token = new Token(linePos, charPos);

		if (lexeme == null || lexeme.equals("")) {
			return token;
		}

		//assume the lexeme is an identifier
		token.lexeme = lexeme;
		token.kind = Kind.IDENTIFIER;
		
		Kind kind = findKeywordMatch(lexeme);
		if (kind != null) {
			token.lexeme = lexeme;
			token.kind = kind;
		}
		return token;
	}

	public static Token FLOAT(String num, int linePos, int charPos) {
		Token token = new Token(linePos, charPos);
		token.kind = Kind.FLOAT;
		token.lexeme = num;
		return token;
	}

	public static Token INTEGER(String num, int linePos, int charPos) {
		Token token = new Token(linePos, charPos);
		token.kind = Kind.INTEGER;
		token.lexeme = num;
		return token;
	}

	public static Token INVALID(String lexeme, int linePos, int charPos) {
		Token token = new Token(linePos, charPos);
		token.lexeme = "Unrecognized lexeme: " + lexeme;
		return token;
	}

	public static Token SPECIAL_CHARS(String lexeme, int linePos, int charPos) {
		Token token = new Token(linePos, charPos);
		token.lexeme = "Unexpected character: " + lexeme;

		Kind kind = findKeywordMatch(lexeme);
		if (kind != null) {
			token.lexeme = lexeme;
			token.kind = kind;
		}
		
		return token;
	}

	private static Kind findKeywordMatch(String lex){
		/*
		 * Following loop determines if we have a keyword lexeme.  If a keyword
		 * lexeme is found, the kind needs to be updated accordingly.  Otherwise,
		 * we have an identifier which has already been set above and the values
		 * will not get updated.
		 * 
		 */
		for(Kind kind: Kind.values()) {
			if(kind.hasStaticLexeme()) {
				if(kind.getDefaultLexeme().equals(lex)) {
					if( kind == Token.Kind.CALL){
							System.out.println("");
					}
					return kind;
				}
			}
		}
		return null;
	}

	public int lineNumber() {
		return lineNum;
	}
	
	public int charPosition() {
		return charPos;
	}
	
	// Return the lexeme representing or held by this token
	public String lexeme() {
		return this.lexeme;
	}
	
	@Override
	public String toString() {
		StringBuilder tokenString = new StringBuilder(this.kind.toString());
		if(lexemesToPrint.containsKey(this.kind)){
			tokenString.append("(" + this.lexeme + ")");
		}
		tokenString.append("(lineNum:" + this.lineNum + ", charPos:" + this.charPos + ")");

		return tokenString.toString();
	}

	public Kind getKind() {
		return kind;
	}

	public void setKind(Kind kind) {
		this.kind = kind;
	}
}