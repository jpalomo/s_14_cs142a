/*
 * This is the entry point of the compiler.  The compiler expects a single command
 * line argument which is the name of the input file to create the tokens from.
 * The class uses the Parse class by calling the parse method to parse the
 * input from the Scanner.
 * 
 * @author John Palomo
 * Date: 5/14/2014
 * CS 142A: Compilers and Interpreters
 * Spring 2014
 * 
 */

package crux;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import mips.CodeGen;
import mips.Program;

public class Compiler {
    public static String studentName = "John Palomo";
    public static String studentID = "60206611";
    public static String uciNetID = "jmpalomo";
    
    public static void main(String[] args) {
		//ensure an argument has been passed to the compiler
//		if(args.length <= 0) {
//			System.err.println("No input arguments specified.  Compiler termininating...");
//			System.exit(-1);
//		}

   		//String sourceFile = args[0];
		String sourceFile = "/Users/Palomo/Documents/School/Graduate/S_2014/CS_142A_Compilers_and_Interpreters/ProjectStubs/tests/test01.crx";
        Scanner s = null;

        try {
            s = new Scanner(new FileReader(sourceFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error accessing the source file: \"" + sourceFile + "\"");
            System.exit(-2);
        }

        Parser p = new Parser(s);
        ast.Command syntaxTree = p.parse();
        if (p.hasError()) {
            System.out.println("Error parsing file " + sourceFile);
            System.out.println(p.errorReport());
            System.exit(-3);
        }

  		types.TypeChecker tc = new types.TypeChecker();
        tc.check(syntaxTree);
        if (tc.hasError()) {
            System.out.println("Error type-checking file.");
            System.out.println(tc.errorReport());
            System.exit(-4);
        }

 		CodeGen cg = new CodeGen(tc);
        cg.generate(syntaxTree);
        if (cg.hasError()) {
            System.out.println("Error generating code for file " + sourceFile);
            System.out.println(cg.errorReport());
            System.exit(-5);
        }
        
        String asmFilename = sourceFile.replace(".crx", ".asm");
        try {
            Program prog = cg.getProgram();
            File asmFile = new File(asmFilename);
            PrintStream ps = new PrintStream(asmFile);
            prog.print(ps);
            ps.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error writing assembly file: \"" + asmFilename + "\"");
            System.exit(-6);
		}	
    }
}
    
