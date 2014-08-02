/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

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
import java.io.Writer;
import org.apache.commons.io.FileUtils;

public class CompilerTest {
    public static final String studentName = "John Palomo";
    public static final String studentID = "60206611";
    public static final String uciNetID = "jmpalomo";
	
	public static void main(String[] args)
	{
		String test_dir = "/Users/Palomo/Documents/School/Graduate/S_2014/CS_142A_Compilers_and_Interpreters/ProjectStubs/tests/";
		File file = new File(test_dir);

		File[] files = file.listFiles();

		for (File f: files){
			f = new File("/Users/Palomo/Documents/School/Graduate/S_2014/CS_142A_Compilers_and_Interpreters/ProjectStubs/tests/test06.crx");
			String fileName = null;
			try {
				if(f.getName().endsWith(".crx")) {
	
				fileName = f.getName();
				String numbers = (fileName.substring(fileName.length() - 6)).substring(0, 2);

				Scanner s = new Scanner(new FileReader(f.getAbsolutePath()));
	
				File outfile= new File("/Users/Palomo/Desktop/out/test" + numbers + ".out");
				Writer writer = new FileWriter(outfile);
	
				Parser p = new Parser(s);
        		p.parse();

        		if (p.hasError()) {
            		writer.write("Error parsing file.\n");
					writer.write(p.errorReport());
            		System.out.println(p.errorReport());

        			System.out.println(p.parseTreeReport());
        		}
				else {
					writer.write(p.parseTreeReport());
        			System.out.println(p.parseTreeReport());
				}
				writer.write("\n"); 

				writer.close();
				
				String masterOutput = test_dir + outfile.getName();
				File origFile = new File(masterOutput);
				boolean compare1and2 = FileUtils.contentEquals(origFile, outfile);
	
				if(!compare1and2) {
					//System.err.println("Files were not the same: " + outfile.getName());
				}else
					System.out.println(fileName);
				System.exit(0);
			}
		} catch (IOException e) {
				System.err.println("Error processing : " + fileName);
				e.printStackTrace();
		}
		}
	}
}
