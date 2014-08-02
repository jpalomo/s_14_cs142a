package tests;
import crux.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class Compiler {
    public static String studentName = "TODO: Your Name";
    public static String studentID = "TODO: Your 8-digit id";
    public static String uciNetID = "TODO: uci-net id";
    
    public static void main(String[] args)
    {
        String sourceFilename = "/Users/Palomo/Documents/School/Graduate/S_2014/CS_142A_Compilers_and_Interpreters/ProjectStubs/tests/test07.crx"; 
        
        Scanner s = null;
        try {
            s = new Scanner(new FileReader(sourceFilename));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error accessing the source file: \"" + sourceFilename + "\"");
            System.exit(-2);
        }

        Parser p = new Parser(s);
        ast.Command syntaxTree = p.parse();
        if (p.hasError()) {
            System.out.println("Error parsing file " + sourceFilename);
            System.out.println(p.errorReport());
            System.exit(-3);
        }
        
        ast.PrettyPrinter pp = new ast.PrettyPrinter();
        syntaxTree.accept(pp);
        System.out.println(pp.toString());
    }
}
    
