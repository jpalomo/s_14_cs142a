/*
 * This is the entry point of the compiler.  The compiler expects a single command
 * line argument which is the name of the input file to create the tokens from.
 * The class uses the Parse class by calling the parse method to parse the
 * input from the Scanner.
 * 
 * @author John Palomo
 * Date: 4/21/2014
 * CS 142A: Compilers and Interpreters
 * Spring 2014
 * 
 */

package crux;

import java.io.FileReader;
import java.io.IOException;

public class Compiler {
    public static String studentName = "John Palomo";
    public static String studentID = "60206611";
    public static String uciNetID = "jmpalomo";
    
    public static void main(String[] args) {
		//ensure an argument has been passed to the compiler
		if(args.length <= 0) {
			System.err.println("No input arguments specified.  Compiler termininating...");
			System.exit(-1);
		}

   		String sourceFile = args[0];
        Scanner s = null;

        try {
            s = new Scanner(new FileReader(sourceFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error accessing the source file: \"" + sourceFile + "\"");
            System.exit(-2);
        }

        Parser p = new Parser(s);
        p.parse();
        if (p.hasError()) {
            System.out.println("Error parsing file.");
            System.out.println(p.errorReport());
            System.exit(-3);
        }
		
        System.out.println(p.parseTreeReport());
    }
}
    
