/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transaction;

/**
 *
 * @author Palomo
 */
public class Reservation extends TableRow{
	public static final int FLIGHT = 1;
	public static final int ROOM = 2;
	public static final int CAR = 3;
	private int resvType; //1:flight  2:hotel room  3:car
	private String resvKey; //primary key

	public Reservation(int resvType, String resvKey) {
		this.resvType = resvType;
		this.resvKey = resvKey; 
	}

	public int getResvType() {
		return resvType;
	}

	public String getResvKey() {
		return resvKey;
	}

	public void setResvKey(String resvKey) {
		this.resvKey = resvKey;
	}
}