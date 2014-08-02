/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transaction;

/**
 *
 * @author Palomo
 */
public class Flight extends TableRow {
	private String flightNum;
	private int numSeats = 0;
	private int price = 0;
	private int numAvail = 0;

	public Flight(String flightNum, int numSeats, int numAvail, int price) {
		this.flightNum = flightNum;
		this.numSeats = numSeats;
		this.price = price;
		this.numAvail = numAvail;
	}

	public Flight(String flightNum) {
		this.flightNum = flightNum;
	}

	public String getFlightNum() {
		return flightNum;
	}

	public void setFlightNum(String flightNum) {
		this.flightNum = flightNum;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getNumSeats() {
		return numSeats;
	}

	public void setNumSeats(int numSeats) {
		this.numSeats = numSeats;
	}

	public int getNumAvail() {
		return numAvail;
	}

	public void setNumAvail(int numAvail) {
		this.numAvail = numAvail;
	}	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Flight_Num: " + this.flightNum);
		sb.append("\nTotal_Seats: " + this.numSeats);
		sb.append("\nAvail_Seats: " + this.numAvail);
		return sb.toString();
	}
}