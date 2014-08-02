/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transaction;

/**
 *
 * @author Palomo
 */
public class Car extends TableRow {
	private String location;
	private int price = 0;
	private int numCars = 0;
	private int numAvail = 0;

	public Car(String location, int price, int numCars, int numAvail) {
		this.location = location;
		this.price = price;
		this.numCars = numCars;
		this.numAvail = numAvail;
	}

	public Car(String location){
		this.location = location;
	}

	public Car() {
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

	public int getNumCars() {
		return numCars;
	}

	public void setNumCars(int numCars) {
		this.numCars = numCars;
	}

	public int getNumAvail() {
		return numAvail;
	}

	public void setNumAvail(int numAvail) {
		this.numAvail = numAvail;
	}
}
