/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Palomo
 */
public class Transaction {
		
	private int xid; 
	public HashMap<String, TableRow> updates_list;
	public HashMap<String, TableRow> deletes_list;

	public Transaction(int xid){
		this.xid = xid;
		updates_list = new HashMap<String, TableRow>();
		deletes_list = new HashMap<String, TableRow>();
	}

	public List<TableRow> getUpdates() {
		System.out.println("Transaction has " + updates_list.values().size() + " updates.");
		return new ArrayList<TableRow>(updates_list.values());
	}

	public List<TableRow> getDeletes() {
		System.out.println("Transaction has " + updates_list.values().size() + " deletes.");
		return new ArrayList<TableRow>(deletes_list.values());
	}

	public int getXid() {
		return xid;
	}
}
