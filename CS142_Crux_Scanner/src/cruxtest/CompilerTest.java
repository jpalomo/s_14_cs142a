/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cruxtest;

/**
 *
 * @author Palomo
 */
import crux.Scanner;
import crux.Token;
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
		String test_dir = "/Users/Palomo/Documents/School/Graduate/S_2014/CS_142A_Compilers_and_Interpreters/ProjectStubs/tests";
		File file = new File(test_dir);

		File[] files = file.listFiles();

		for (File f: files){
		f = new File("/Users/Palomo/Documents/School/Graduate/S_2014/CS_142A_Compilers_and_Interpreters/ProjectStubs/tests/test06.crx");
			String fileName = null;
			try {
				if(f.getName().endsWith(".crx")) {
	
				fileName = f.getName();
				System.out.println(fileName);
				String numbers = (fileName.substring(fileName.length() - 6)).substring(0, 2);

				Scanner s = new Scanner(new FileReader(f.getAbsolutePath()));
				String tokenString = null;
				Token t = s.next();
				Token.Kind kind = t.getKind();
	
				File outfile= new File("/Users/Palomo/Desktop/out/test" + numbers + ".out");
				Writer writer = new FileWriter(outfile);
	
				while (kind != Token.Kind.EOF ) {
					tokenString = t.toString();
	
					if(tokenString != null && !tokenString.equals("")) {
						System.out.println(tokenString);
						writer.write(tokenString);
						writer.write("\n");
					}
					t = s.next();
					kind = t.getKind(); 
					if(kind == Token.Kind.EOF){
							System.out.println("");
					}
				}
				tokenString = t.toString();
				//System.out.println(tokenString);
				writer.write(tokenString +"\n");
				writer.close();
				
				String masterOutput = test_dir + outfile.getName();
				File origFile = new File(masterOutput);
				boolean compare1and2 = FileUtils.contentEquals(origFile, outfile);
	
				if(!compare1and2) {
					System.err.println("Files were not the same: " + outfile.getName());
				}
				System.exit(0);
			}
		} catch (IOException e) {
				System.err.println("Error processing : " + fileName);
				e.printStackTrace();
		}
		}
	}
}
