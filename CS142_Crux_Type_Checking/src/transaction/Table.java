/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author Palomo
 */
public class Table implements Serializable {
	public static final String tableExt = ".tbl";

	/**
	 * This method will write the new table out to disk.  We attach the xid with to the table
	 * name to ensure atomic commits.  Every transaction whether it updates or deletes will
	 * rewrite the whole database, but only update the objects that it modifies.
	 * 
	 * @param table
	 * @param xid
	 * @return
	 * @throws IOException 
	 */
	public static boolean writeTable(Map table, String fileName) throws IOException {
		FileOutputStream fileOut = null;

		try {
			//create the table with the associated xid
			fileOut = new FileOutputStream(fileName);
	  		ObjectOutputStream out = new ObjectOutputStream(fileOut);
	  		out.writeObject(table);
		}
		catch (Exception e) {
			System.out.println(e);
		    return false;
		}
		finally{
			if (fileOut != null) {
				fileOut.close();
			}
		}
		return true;
	}

	public static int removeTableFile(String fileName){
		File file = new File(fileName);
		file.delete();
		return 0;
	}
} 
