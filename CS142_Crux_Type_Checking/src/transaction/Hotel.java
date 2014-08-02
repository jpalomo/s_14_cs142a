/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transaction;

/**
 *
 * @author Palomo
 */
public class Hotel extends TableRow {
	private String location;
	private int price = 0;
	private int numRooms = 0;
	private int numAvail = 0;

	public Hotel(String location, int price, int numRooms, int numAvail) {
		this.location = location;
		this.price = price;
		this.numRooms = numRooms;
		this.numAvail = numAvail;
	}

	public Hotel(String location){
		this.location = location;
	}

	public Hotel() {
		super();
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getNumRooms() {
		return numRooms;
	}

	public void setNumRooms(int numRooms) {
		this.numRooms = numRooms;
	}

	public int getNumAvail() {
		return numAvail;
	}

	public void setNumAvail(int numAvail) {
		this.numAvail = numAvail;
	}
	
}