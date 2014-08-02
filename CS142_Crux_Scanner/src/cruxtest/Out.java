/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cruxtest;

/**
 *
 * @author Palomo
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class Out{
    public static String studentName = "John Palomo";
    public static String studentID = "60206611";
    public static String uciNetID = "jmpalomo";
	
	public static void main(String[] args) throws IOException
	{
		String test_dir = "/Users/Palomo/Documents/School/Graduate/S14_CS_142A_Compilers_and_Interpreters/tests_public/";
		File file = new File(test_dir);

		File outfile= new File("/Users/Palomo/Desktop/" + "all.out");
		Writer writer = null;
		File[] files = file.listFiles();
		for (File f: files){

			String fileName = null;
			try {
				 writer = new FileWriter(outfile);

				if(f.getName().endsWith(".crx")) {
					fileName = f.getName();
					String numbers = (fileName.substring(fileName.length() - 6)).substring(0, 2);
	
					java.util.Scanner reader = new java.util.Scanner(new FileReader(f.getAbsolutePath()));
					
					writer.write("Begin: " + f.getName());
					System.out.println("Begin: " + f.getName());
	
					while(reader.hasNextLine()) {
	
						String line = reader.nextLine();
						System.out.println(line);
						writer.write(line);
						writer.write("\n");
					}
	
					File outputFile= new File(test_dir +  "test" + numbers + ".out");
					reader = new java.util.Scanner(new FileReader(outputFile));
	
					while(reader.hasNextLine()) {
						String line = reader.nextLine();
						writer.write(line);
						writer.write("\n");
					}
	
					writer.write("Begin: " + f.getName());
					writer.write("\n");
					writer.write("\n");
			}
		} catch (IOException e) {
			System.err.println("Error processing : " + fileName);
			e.printStackTrace();
		}
		finally {
							writer.close();
			}	
		}
	}
}
