/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crux;

/**
 *
 * @author Palomo
 */
import crux.Parser;
import crux.Scanner;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import mips.CodeGen;
import mips.Program;
import org.apache.commons.io.FileUtils;
import types.TypeChecker;

public class CompilerTest {
    public static final String studentName = "John Palomo";
    public static final String studentID = "60206611";
    public static final String uciNetID = "jmpalomo";
	
	public static void main(String[] args)
	{
		String test_dir = "/Users/Palomo/Documents/School/Graduate/S_2014/CS_142A_Compilers_and_Interpreters/ProjectStubs/tests/";
		File file = new File(test_dir);

		File[] files = file.listFiles();

		for (File f: files) {
			f = new File("/Users/Palomo/Documents/School/Graduate/S_2014/CS_142A_Compilers_and_Interpreters/ProjectStubs/tests/test01.crx");
			String fileName = null;
			try {
				if(f.getName().endsWith(".crx")) {
	
				fileName = f.getName();
				String numbers = (fileName.substring(fileName.length() - 6)).substring(0, 2);

				Scanner s = new Scanner(new FileReader(f.getAbsolutePath()));
	
				File outfile= new File("/Users/Palomo/Desktop/out/test" + numbers + ".out");
				Writer writer = new FileWriter(outfile);
	
				Parser p = new Parser(s);
        		ast.Command syntaxTree = p.parse();
        
        		//ast.PrettyPrinter pp = new ast.PrettyPrinter();
        		//syntaxTree.accept(pp);
				//System.out.println(pp.toString());
				//System.out.println("\n\n\n\n\n");
				
				TypeChecker tc = new TypeChecker();
       	 		tc.check(syntaxTree);
	
        		if (tc.hasError()) {
					writer.write("Error type-checking file.\n");
            		writer.write(tc.errorReport() + "\n");
            		System.out.println(tc.errorReport());
					writer.close();
					     //System.exit(-4);
        		}
				else {
					writer.write("Crux Program has no type errors.\n");
	        		//System.out.println("Crux Program has no type errors.");
				}

				CodeGen cg = new CodeGen(tc);
        		cg.generate(syntaxTree);
        		if (cg.hasError()) {
            		System.out.println("Error generating code for file " + fileName);
            		System.out.println(cg.errorReport());
            		System.exit(-5);
        		}
        		
        		String asmFilename = fileName.replace(".crx", ".asm");
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

				writer.close();
				
				String masterOutput = test_dir + outfile.getName();
				File origFile = new File(masterOutput);
				boolean compare1and2 = FileUtils.contentEquals(origFile, outfile);
			
//				if (!compare1and2) {
//					System.err.println("Files were not the same: " + outfile.getName());
//				}else
//					System.out.println("Files " + fileName + " are the same.");
//				}

				System.exit(0);	
		
			} catch (IOException e) {
				System.err.println("Error processing : " + fileName);
				e.printStackTrace();
			}
		}
	}
}
