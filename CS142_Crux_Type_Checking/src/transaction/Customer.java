/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transaction;

import java.util.ArrayList;

/**
 *
 * @author Palomo
 */
public class Customer extends TableRow {
	private String custName;
	ArrayList<Reservation> reservations;

	public Customer(String customerName) {
		this.custName = customerName;
		this.reservations = new ArrayList<Reservation>();
	}

	public Customer() {
		super();
	} 

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	} 

	public ArrayList<Reservation> getReservations() {
		return reservations;
	}

	public void setReservations(ArrayList<Reservation> reservations) {
		this.reservations = reservations;
	}

	public void addReservation(Reservation resveration){
		reservations.add(resveration);
	}
}
