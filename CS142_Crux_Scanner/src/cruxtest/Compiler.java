/*
 * This is the entry point of the compiler.  The compiler expects a single command
 * line argument which is the name of the input file to create the tokens from.
 * The class uses a while loop to call the next() method of the Scanner class
 * to read each possible token from the input file until and End-of-File 
 * character is reached.
 * 
 * @author John Palomo
 * Date: 4/11/2014
 * CS 142A: Compilers and Interpreters
 * Spring 2014
 */
package cruxtest;

import crux.Scanner;
import crux.Token;
import java.io.FileReader;
import java.io.IOException;

public class Compiler {
    public static final String studentName = "John Palomo";
    public static final String studentID = "60206611";
    public static final String uciNetID = "jmpalomo";
	
	public static void main(String[] args)
	{
		//ensure an argument has been passed to the compiler
		if(args.length <= 0) {
			System.err.println("No input arguments specified.  Compiler termininating...");
			System.exit(-1);
		}

		String sourceFile = args[0];
		try {
			Scanner s = new Scanner(new FileReader(sourceFile));
			String tokenString = null;
			Token t = s.next();
			Token.Kind kind = t.getKind();
			while (kind != Token.Kind.EOF) {
				tokenString = t.toString(); 
				System.out.println(tokenString);
				
				t = s.next();
				kind = t.getKind(); 
			}
			tokenString = t.toString();
			System.out.println(tokenString);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error accessing the source file: \"" + sourceFile + "\"");
			System.exit(-2);
		}
	}
}