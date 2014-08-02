/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cruxtest;

import java.io.File;

/**
 *
 * @author Palomo
 */
public class CRUXTEST {

		/**
		 * @param args the command line arguments
		 */
		public static void main(String[] args) {
			File file  = new File("/Users/Palomo/Documents/School/Graduate/S14_CS_142A_Compilers_and_Interpreters/Week1/tests_public");
			
			File[] files = file.listFiles();

			for (File f: files) {
				if f.getName().endsWith(".crux")
			}

				
				// TODO code application logic here
		}
}
