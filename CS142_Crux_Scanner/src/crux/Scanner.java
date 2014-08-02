/*
 * This class implements the lexical analyzer for the crux compiler.  The pubic
 * next() method returns the next token identified in the source input. The call 
 * to next() can be viewed as the initial start state of a DFA.  Since the firs 
 * character has already been read into the nextChar instance variable when 
 * next() is called, it uses this character to determine which of the three states
 * to go to.  Basically, the 3 states can be mapped to the valid tokens we can 
 * have in a program.  The handleAlphaNum method handles the keywords and identifiers
 * of the program after it has determine the first character is either a letter or 
 * underscore.  The handleNumeric handles intgers and floats once it has been
 * determined that the first character is a digit.  Otherwise a third state called
 * handleSpecialChars, will handle all other characters trying to match reserved 
 * special character tokens from the crux language of length 1 or 2.  
 * 
 * @author John Palomo
 * 
 */
package crux;

import java.io.IOException;
import java.io.Reader;

public class Scanner {
	public static final String studentName = "John Palomo";
	public static final String studentID = "60206611";
	public static final String uciNetID = "jmpalomo";
	
	private int lineNum;  // current line count
	private int charPos;  // character offset for current line
	private int nextChar; // contains the next char (-1 == EOF)
	private int startCharPos;

	private Reader input;
	private Token token;

	public Scanner (Reader reader) {
		charPos = 1;
		lineNum = 1;
		input = reader;
		startCharPos = 1;
		
		//set the point to the first character
		try { 
			nextChar = input.read();
		}
		catch (IOException e) {
			System.err.println("Error trying to read the file.");
			System.exit(-1);
		}
		readChar(true);
	}

	/**
	 * This is a helper function that advances the character pointer.  It takes 
	 * boolean value that indicates whether to advance the character pointer 
	 * any contiguous whitespace.  For instance, if the current character is 
	 * any whitespace, passing a value of true will advance the pointer to the
	 * next non-whitespace character to be processed.  Otherwise, the whitespace
	 * character is returned and used to determine an end of a lexeme being 
	 * processed.
	 * 
	 * @param advancePointer - boolean value indicating whether or not to skip
	 * whitespace
	 * @return 
	 */
	private int readChar(boolean advancePointer) {
		try {
			if (advancePointer) {
				//advance pointer
				while (Character.isWhitespace(nextChar)) {
					if( ((char)nextChar) == '\n') {
						charPos = 1;
						lineNum++;
						nextChar = input.read();
						//advance to new line and prime the pump
						continue;
					}
					nextChar = input.read();
					charPos++;
				}
			}
			else {
				nextChar = input.read();
				charPos++;	
			}
		} catch (IOException e) {
			System.err.println("There was an error trying to read the file.");
			System.exit(-1);
		}
		return nextChar;
	}
	
	private void handleAlphaNum(StringBuilder sb) {
		while(Character.isLetterOrDigit(nextChar) || ((char)nextChar) == '_' ){
			appendChar(sb); 
			readChar(false);
		}
		token = Token.INDENTIFIER_OR_KEYWORD(sb.toString(), lineNum, startCharPos);
	}

	/*
	 * INTEGER = digit {digit}
	 * FLOAT = digit {digit} "." {digit}
	 */
	private void handleNumeric(StringBuilder sb) {
		//append as many digits as you can before reach non digit
		while (Character.isDigit(nextChar)) {
			appendChar(sb);
			readChar(false);
		} 

		//processed all digits-determine if int, float, or invalid 
		if (nextChar == '.'){ //float case
			appendChar(sb);
			readChar(false);

			//append as many digits as possible to the float
			while(Character.isDigit(nextChar)) {
				appendChar(sb);
				readChar(false);
			}
			token = Token.FLOAT(sb.toString(), lineNum, startCharPos);
			return;
		}

		//encountered only digits, create integer token
		token = Token.INTEGER(sb.toString(), lineNum, startCharPos);
	}

	/**
	 * 
	 * @param sb 
	 */
	/*
	 * Invariant:  sb always always consists of 1 character at time of method call
	 */
	private void handleSpecialChars(StringBuilder sb) {
		//handle the comments
		char ch = (char) nextChar;

		//handle comments
		if( ch == '/' && sb.toString().charAt(0) == '/' ) {
			//read to the end of the line
			while ( ch != '\n' && nextChar != -1) {
				readChar(false);
				ch = (char) nextChar;
			}

			//if we encountered the end of file, return the EOF token
			if(nextChar == -1) {
				token = Token.EOF(lineNum, charPos);
				return;
			}

			//advance the pointer to the next char and find the token
			readChar(true);
			next();
		}
		else {
			//string holding the first character
			String origCharacter = sb.toString();

			//append the next char and see if we can make a special char string
			appendChar(sb);

			if(sb.toString().equals("-1")) {
				token = Token.SPECIAL_CHARS(origCharacter, lineNum, startCharPos);
				return;
			}
			
			token = Token.SPECIAL_CHARS(sb.toString(), lineNum, startCharPos); 

			if(token.getKind() == Token.Kind.ERROR) {
				//first char was valid
				token = Token.SPECIAL_CHARS(origCharacter, lineNum, startCharPos);
				return;
			}

			readChar(false); //prime the pump for next call
		}
	}

	/* Invariants:
	 *  1. call assumes that nextChar is already holding an unread character
	 *  2. return leaves nextChar containing an untokenized character
	 */
	/**
	 * This method determines the type of token for the next lexeme.  Since the
	 * invariant holds that there is an unread character in nextChar, we start
	 * by determine what the first character is and map that to one of the token.
	 * Given our grammar, we know that with the first character we can determine
	 * whether the identifier can possible be one of the following: EOF, RESERVED, 
	 * INTEGER/FLOAT, IDENTIFIER, SPECIAL CHAR SEQUENCES
	 * @return 
	 */

	public Token next()
	{
		//create the string builder for the lexeme
		StringBuilder sb = new StringBuilder(String.valueOf((char)nextChar));	
		startCharPos = charPos;

		//EOF
	    if (nextChar == -1) {
			token = Token.EOF(lineNum, startCharPos);
			return token;
		}
		else if(Character.isLetter(nextChar) || ((char)nextChar == '_')) { 
			readChar(false);	
			handleAlphaNum(sb);
		}
		else if(Character.isDigit(nextChar)){
			readChar(false);
			handleNumeric(sb);
		}
		else {
			readChar(false);
			handleSpecialChars(sb);
		}
		//advance the next non-whitespace character
		readChar(true);
		return token;
	}

	private void appendChar(StringBuilder sb){
		sb.append(String.valueOf((char) nextChar));
	}
}