package transaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lockmgr.DeadlockException;
import lockmgr.LockManager;

/** 
 * Resource Manager for the Distributed Travel Reservation System.
 * 
 * Description: toy implementation of the RM, for initial testing
 */

public class ResourceManagerImpl extends UnicastRemoteObject implements ResourceManager {
	public static final String DATA_DIR;
	private static LockManager lm = new LockManager(); //lock manager used for all transactions

	private String resourceName = null;
	private Integer semaphore = 1;
	
	//Tables
	private Map<String, Flight> flights; 
	private Map<String, Customer> customers; 
	private Map<String, Car> cars; 
	private Map<String, Hotel> rooms; 

	public static final String FLIGHTS = "flights";
	public static final String CARS = "cars";
	public static final String RESERVATIONS = "reservations";
	public static final String HOTELS = "hotels";
	public static final String CUSTOMERS = "customers";
	public static final String DB_NAME = "travel.db"; //version pointer file for db
	public static final String INVALID_CUST_NAME = "Invalid customer name";
	public static final String INVALID_CAR_LOCATION =  "Invalid car location";
	public static final String INVALID_HOTEL_LOCATION =  "Invalid hotel location";
	public static final String INVALID_FLIGHT_NUM =  "Invalid flight number";


    private Integer xidCounter; //transaction numbering, this will be used to assigned xids

	private Map<Integer, Transaction> activeTrans;  //list of all active transactions
	private Map<Integer, Transaction> abortedTrans; //list of all aborted transactions

	private static final int NO_PRICE_CHANGE = -1;

	public static boolean dieBefore = false; 
	public static boolean dieAfter = false;

	static {
		//build the path to the data dir
		StringBuilder sb = new StringBuilder(System.getProperty("user.dir"));
		
		if(sb.toString().contains("NetBeansProjects") || sb.toString().contains("eclipse") || sb.toString().contains("Eclipse")){
			sb.append(File.separator).append("project").append(File.separator); //project dir
			sb.append("test.part1").append(File.separator).append("data").append(File.separator); //test.part1/data/
		}
		else {
			sb.append(File.separator).append("data").append(File.separator);
		}
	
		//initialize the global final string
		DATA_DIR = sb.toString();	
	}

    public static void main(String args[]) {
		System.setSecurityManager(new RMISecurityManager());

		String rmiName = System.getProperty("rmiName");
		if (rmiName == null || rmiName.equals("")) {
	    	rmiName = ResourceManager.DefaultRMIName;
		}

		String rmiRegPort = System.getProperty("rmiRegPort");
		if (rmiRegPort != null && !rmiRegPort.equals("")) {
		    rmiName = "//:" + rmiRegPort + "/" + rmiName;
		}

		try {
	   		//Project 2 ResourceManagerImpl obj = new ResourceManagerImpl(rmiName);
	   		ResourceManagerImpl obj = new ResourceManagerImpl();
	    	Naming.rebind(rmiName, obj);
	    	System.out.println("RM bound");
		} 
		catch (Exception e) {
	    	System.err.println("RM not bound:" + e);
	    	System.exit(1);
		}
    } 
    
	public ResourceManagerImpl() throws RemoteException{
		this("all");
	}
	
    public ResourceManagerImpl(String resourceName) throws RemoteException {
		this.resourceName = resourceName;
		File dataDir = new File(DATA_DIR);
		
		//create the output directory for the database files and pointer files if it doesnt exist
		if (!dataDir.exists()) {
			boolean createDataDir = dataDir.mkdir();
			if(!createDataDir){ //failed to create the data dir
				throw new RemoteException("Could not initialize resource manager.");
			}
		}

		//create the empty active and aborted transactions lists
		activeTrans = new HashMap<Integer, Transaction>();
		abortedTrans = new HashMap<Integer, Transaction>();

		//initialize the database
		boolean successfulRecover = recover();

		if(!successfulRecover){
			System.err.println("Error trying to recover the tables.");
			System.exit(-1);
		}
		xidCounter = 1;
	}

    // BEGIN: TRANSACTION INTERFACE
	@Override
    public int start() throws RemoteException {
		synchronized(xidCounter) {
			Transaction transaction = new Transaction(xidCounter);
			activeTrans.put(xidCounter, transaction);
			return xidCounter++;
		}
    }

	@Override
    public boolean commit(int xid) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		System.out.println("Committing xid: " + xid);
		validateTransaction(xid);
		commit(activeTrans.get(xid));
		return lm.unlockAll(xid);
    }

	@Override
    public void abort(int xid) throws RemoteException, InvalidTransactionException {
		Transaction trans = activeTrans.remove(xid);
		abortedTrans.put(xid, trans);
		lm.unlockAll(xid);
    }
    // END: TRANSACTION INTERFACE

	private void validateTransaction(int xid) throws TransactionAbortedException, InvalidTransactionException {
		if(abortedTrans.containsKey(xid)){
			throw new TransactionAbortedException(xid, "Transaction was previously aborted.");
		}

		if(!activeTrans.containsKey(xid)) {
			throw new InvalidTransactionException(xid, "Invalid transaction identifier.");
		}	
	}


    // BEGIN: ADMINISTRATIVE INTERFACE
	@Override
    public boolean addFlight(int xid, String flightNum, int numSeats, int price) throws RemoteException, TransactionAbortedException, InvalidTransactionException {

		//simple validations
		validate(xid, flightNum, INVALID_FLIGHT_NUM);
		try {

			String flightKey = FLIGHTS + flightNum;

			//lock the object
			boolean lockAcquired = lm.lock(xid, flightKey, LockManager.WRITE);

			if(!lockAcquired) {
				return false;
			}	
			
			//get the current transaction object to see if we have changed it
			Transaction trans = activeTrans.get(xid);

			//previously deleted flight, need to remove from deletes list since were readding it
			if(trans.deletes_list.containsKey(flightKey)){
				trans.deletes_list.remove(flightKey);
			}

			Flight flight = null;  
			if(trans.updates_list.containsKey(flightKey)){
				flight = (Flight) trans.updates_list.get(flightKey);
			}
			else{
				//Flight f1 = (Flight) database.query(Database.FLIGHTS, flightNum);	
				Flight f1 = flights.get(flightNum);
				if(f1 != null){
					flight = new Flight(f1.getFlightNum(), f1.getNumSeats(), f1.getNumAvail(), f1.getPrice());
				}
			}

			if(flight == null){ //wasnt in update list or database, must be new flight
				flight = new Flight(flightNum);
			}
	
			flight.setNumSeats(flight.getNumSeats() + numSeats);

			if(flight.getNumAvail() + numSeats < 0){
				return false;
			}

			flight.setNumAvail(flight.getNumAvail() + numSeats);

			if(price > 0) {
				flight.setPrice(price);
			}
				
			trans.updates_list.put(flightKey, flight);
			return true;
		} catch (DeadlockException e) {
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		}
    }

	@Override
	/**
	 * Checked
	 */
    public boolean deleteFlight(int xid, String flightNum) throws RemoteException, TransactionAbortedException, InvalidTransactionException {

		validate(xid, flightNum, INVALID_FLIGHT_NUM);

		boolean reservationExists = isReservationExist(xid, flightNum);

		if(reservationExists){
			return false;
		}

		try {
			//get the current transaction object to see if we have changed it
			Transaction trans = activeTrans.get(xid);
			String flightKey = FLIGHTS + flightNum;

			boolean lockAcquired = lm.lock(xid, flightKey, LockManager.WRITE);

			if(!lockAcquired){
				return false;
			}

			if(trans.updates_list.containsKey(flightKey)){
				trans.updates_list.remove(flightKey);
			}

			//trans.addDelete(new Flight(flightNum));
			trans.deletes_list.put(flightKey, new Flight(flightNum));

			return true;
		} catch (DeadlockException e) {
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		}
    }
		
	/**
	 * Checked
	 * @param xid
	 * @param resKey
	 * @return 
	 */
	private boolean isReservationExist(int xid, String resKey){
		Transaction trans = activeTrans.get(xid);
		boolean reservationExists;	

		Collection<TableRow> currentUpdates = trans.updates_list.values();

		for(TableRow row: currentUpdates) {
			if(row instanceof Customer) {
				Customer customer = (Customer) row;
				reservationExists = checkReservations(customer, resKey);

				if(reservationExists){
					return true;
				}
			}
		}

		for(Customer customer: customers.values()){
			reservationExists = checkReservations(customer, resKey);
			if(reservationExists){
				return true;
			}
		}

		return false;  //no reservations exist
	}

	private boolean checkReservations(Customer customer, String resKey){
		List<Reservation> customerReservations = customer.getReservations();

		if(customerReservations != null && customerReservations.size() > 0) {
			for(Reservation r: customerReservations){
				if(r.getResvKey().equals(resKey)){
					return true;
				}	
			}
		}	
		return false;  //no reservation matches the reskey
	}
	
	@Override
    public boolean addRooms(int xid, String location, int numRooms, int price) throws RemoteException, TransactionAbortedException, InvalidTransactionException {

		validate(xid, location, INVALID_HOTEL_LOCATION);
		
		try {
			//get the current transaction object to see if we have changed it
			Transaction trans = activeTrans.get(xid);
			String roomsKey = HOTELS + location;

			boolean lockAcquired = lm.lock(xid, roomsKey, LockManager.WRITE);

			if(!lockAcquired) {
				return false;
			}

			if(trans.deletes_list.containsKey(roomsKey) && numRooms > 0){
				trans.deletes_list.remove(roomsKey);
			}

			Hotel hotel = null;  
			if(trans.updates_list.containsKey(roomsKey)){
				hotel = (Hotel) trans.updates_list.get(roomsKey);
			}
			else{
				Hotel h1 = rooms.get(location);	
				if(h1 != null){
					hotel = new Hotel(location, h1.getPrice(), h1.getNumRooms(), h1.getNumAvail());
				}
			}

			//wasnt in update list or database, must be new hotel
			if(hotel == null){
				hotel = new Hotel(location);
			}
	
			hotel.setNumAvail(hotel.getNumAvail() + numRooms);
			hotel.setNumRooms(hotel.getNumRooms() + numRooms);
	
			if(price > 0) {
				hotel.setPrice(price);
			}
				
			trans.updates_list.put(roomsKey, hotel);

			return true;
		} catch (DeadlockException e) {
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		}
    }

	@Override
    public boolean deleteRooms(int xid, String location, int numRooms) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		validate(xid, location, INVALID_HOTEL_LOCATION);
		
		try {
			//get the current transaction object to see if we have changed it
			Transaction trans = activeTrans.get(xid);
			String roomsKey = HOTELS + location;

			boolean lockAcquired = lm.lock(xid, roomsKey, LockManager.WRITE);

			if(!lockAcquired) {
				return false;
			}

			Hotel hotel = null;  
			if(trans.updates_list.containsKey(roomsKey)){
				hotel = (Hotel) trans.updates_list.get(roomsKey);
			}
			else{
				Hotel h1 = rooms.get(location);	
				if(h1 != null){
					hotel = new Hotel(location, h1.getPrice(), h1.getNumRooms(), h1.getNumAvail());
				}
			}				

			//wasnt in update list or database, must be new hotel
			if(hotel == null){
				return false;	
			}
	
			if(hotel.getNumAvail() - numRooms < 0){
				return false;
			}

			hotel.setNumAvail(hotel.getNumAvail() - numRooms);
			trans.updates_list.put(roomsKey, hotel);

			return true;
		} catch (DeadlockException e) {
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		}
    }

	@Override
    public boolean addCars(int xid, String location, int numCars, int price) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
			
		validate(xid, location, INVALID_HOTEL_LOCATION);

		try {
			//get the current transaction object to see if we have changed it
			Transaction trans = activeTrans.get(xid);
			String carsKey = CARS + location;

			boolean lockAcquired = lm.lock(xid, carsKey, LockManager.WRITE);

			if(!lockAcquired) {
				return false;
			}

			if(trans.deletes_list.containsKey(carsKey) && numCars > 0){
				trans.deletes_list.remove(carsKey);
			}

			Car car = null;  
			if(trans.updates_list.containsKey(carsKey)){
				car = (Car) trans.updates_list.get(carsKey);
			}
			else{
				Car c1 = cars.get(location);	
				if(c1 != null){
					car = new Car(location, c1.getPrice(), c1.getNumCars(), c1.getNumAvail());
				}
			}

			//wasnt in update list or database, must be new flight
			if(car == null){
				car = new Car(location);
			}

			car.setNumCars(car.getNumCars() + numCars);
			car.setNumAvail(car.getNumAvail() + numCars);

			if(price > 0) {
				car.setPrice(price);
			}
			
			trans.updates_list.put(carsKey, car);
			return true;
		} catch (DeadlockException e) {
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		}
    }

	@Override
    public boolean deleteCars(int xid, String location, int numCars) throws RemoteException, TransactionAbortedException, InvalidTransactionException {

		validate(xid, location, INVALID_HOTEL_LOCATION);
		
		try {
				//get the current transaction object to see if we have changed it
			Transaction trans = activeTrans.get(xid);
			String carsKey = CARS + location;

			boolean lockAcquired = lm.lock(xid, carsKey, LockManager.WRITE);

			if(!lockAcquired) {
				return false;
			}

			Car car = null;  
			if(trans.updates_list.containsKey(carsKey)){
				car = (Car) trans.updates_list.get(carsKey);
			}
			else{
				Car c1 = cars.get(location);	
				if(c1 != null){
					car = new Car(location, c1.getPrice(), c1.getNumCars(), c1.getNumAvail());
				}
			}

			//wasnt in update list or database, must be new hotel
			if(car == null){
				return false;	
			}
	
			if(car.getNumAvail() - numCars < 0){
				return false;
			}

			car.setNumAvail(car.getNumAvail() - numCars);
			car.setNumCars(car.getNumCars() - numCars);
			
			trans.updates_list.put(carsKey, car);
			
			return true;
		} catch (DeadlockException e) {
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		}
    }

	@Override
    public boolean newCustomer(int xid, String custName) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		validate(xid, custName, INVALID_CUST_NAME);
		try {
			String custKey = CUSTOMERS + custName;
			boolean lockAcquired = lm.lock(xid, custKey, LockManager.WRITE);

			if(!lockAcquired) {
				return false;
			}

			if(customers.containsKey(custName)){
				return true;
			}

			Transaction trans = activeTrans.get(xid);

			Customer customer = new Customer(custName);
			trans.updates_list.put(custKey, customer);
			return true;
		} catch (DeadlockException e) {
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		}
    }

	@Override
    public boolean deleteCustomer(int xid, String custName) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		validate(xid, custName, INVALID_CUST_NAME);
		Transaction trans = activeTrans.get(xid);
		try {	
			String custKey = CUSTOMERS + custName;
			boolean custLockAcquired = lm.lock(xid, custKey, LockManager.WRITE);

			if(!custLockAcquired) {
				return false;
			}

			Customer customer = customers.get(custName);
			List<Reservation> customerReservations = customer.getReservations();
		
			//delete all the reservations for the customer => makes seats, rooms, cars available
			if(customerReservations != null && customerReservations.size() > 0) {
				for (Reservation r: customerReservations) {
					int resType = r.getResvType();
					switch (resType){
						case 1:
							//unreserve a flight seat
							addFlight(xid, r.getResvKey(), 1, NO_PRICE_CHANGE);
							break;
						case 2:
							//unreserve a hotel room
							addRooms(xid, r.getResvKey(), 1, NO_PRICE_CHANGE);
							break;
						case 3:
							//unreserve a car 
							addCars(xid, r.getResvKey(), 1, NO_PRICE_CHANGE);
							break;
						default:
							return false;
					}
				}	
			}
			
			//Now delete the customer after we have deleted the reservations
//			trans.addDelete(new Customer(custName)); //delete the customer entry
			trans.deletes_list.put(custKey, new Customer(custName));
			//trans.addDelete(new Reservation(custName)); //delete the reservations entries
			return true;	
		} catch (DeadlockException e) {
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		}
    }
    // END: ADMINISTRATIVE INTERFACE


    // QUERY INTERFACE
	@Override
    public int queryFlight(int xid, String flightNum) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		validate(xid, flightNum, INVALID_FLIGHT_NUM);
		try{
			String flightKey = FLIGHTS + flightNum;
			boolean lockAcquired = lm.lock(xid, flightKey, LockManager.READ);

			if(!lockAcquired) {
				return 0;
			}

			Flight flight = null;

			Transaction trans = activeTrans.get(xid);

			if(trans.deletes_list.containsKey(flightKey)){
				return 0;
			}

			//check local copy first
			if(trans.updates_list.containsKey(flightKey)) {
				flight = (Flight) trans.updates_list.get(flightKey);
				return flight.getNumAvail();
			}
		
			flight = flights.get(flightNum);
			if(flight != null) {
				return flight.getNumAvail();
			}
			return 0;
		} catch(DeadlockException de){
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		} 
    }

	@Override
    public int queryFlightPrice(int xid, String flightNum) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		validate(xid, flightNum, INVALID_FLIGHT_NUM);

		try{
			String flightKey = FLIGHTS + flightNum;
			boolean lockAcquired = lm.lock(xid, flightKey, LockManager.READ);

			if(!lockAcquired) {
				return 0;
			}

			Flight flight = null;
			Transaction trans = activeTrans.get(xid);

			if(trans.deletes_list.containsKey(flightKey)){
				return 0;
			}

			//check local copy first
			if(trans.updates_list.containsKey(flightKey)) {
				flight = (Flight) trans.updates_list.get(flightKey);
				return flight.getPrice();
			}
		
			flight = flights.get(flightNum);
			if(flight != null) {
				return flight.getPrice();
			}
		} catch(DeadlockException de) {
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		}
		return 0;
    }

	@Override
    public int queryRooms(int xid, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		validate(xid, location, INVALID_HOTEL_LOCATION);
		try{
			String roomsKey = HOTELS + location;
			boolean lockAcquired = lm.lock(xid, roomsKey, LockManager.READ);

			if(!lockAcquired) {
				return 0;
			}

			Hotel hotel = null;
			Transaction trans = activeTrans.get(xid);

			if(trans.deletes_list.containsKey(roomsKey)){
				return 0;
			}

			if(trans.updates_list.containsKey(roomsKey)){
				hotel = (Hotel)trans.updates_list.get(roomsKey);
				return hotel.getNumAvail();
			}

			hotel = rooms.get(location);
			if(hotel != null) {
				return hotel.getNumAvail();
			}
		} catch (DeadlockException de) {
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		} 
		return 0;
    }

	@Override
    public int queryRoomsPrice(int xid, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		validate(xid, location, INVALID_HOTEL_LOCATION);
		try {
			String roomsKey = HOTELS + location;
			boolean lockAcquired = lm.lock(xid, roomsKey, LockManager.READ);

			if(!lockAcquired) {
				return 0;
			}

			Hotel hotel = null;

			Transaction trans = activeTrans.get(xid);

			if(trans.deletes_list.containsKey(roomsKey)) {
				return 0;
			}

			if(trans.updates_list.containsKey(roomsKey)){
				hotel = (Hotel) trans.updates_list.get(roomsKey);
				return hotel.getPrice();
			}

			hotel = rooms.get(location);
			if(hotel != null) {
				return hotel.getPrice();
			}
		} catch(DeadlockException de){
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		} 
		return 0;	
    }

	@Override
    public int queryCars(int xid, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		validate(xid, location, INVALID_CAR_LOCATION);

		try {
			String carsKey = CARS + location;
			boolean lockAcquired = lm.lock(xid, carsKey, LockManager.READ);

			if(!lockAcquired) {
				return 0;
			}

			Car car = null;
			Transaction trans = activeTrans.get(xid);

			if(trans.deletes_list.containsKey(carsKey)){
				return 0;
			}

			if(trans.updates_list.containsKey(carsKey)){
				car = (Car) trans.updates_list.get(carsKey);
				return car.getNumAvail();
			}

			car = cars.get(location);
			if(car != null) {
				return car.getNumAvail();
			}
		} catch (DeadlockException de){
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		}
		return 0;
    }

	@Override
    public int queryCarsPrice(int xid, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		validate(xid, location, INVALID_CAR_LOCATION);
		try {
			String carsKey = CARS + location;
			boolean lockAcquired = lm.lock(xid, carsKey, LockManager.READ);

			if(!lockAcquired) {
				return 0;
			}

			Car car = null;

			Transaction trans = activeTrans.get(xid);

			if(trans.deletes_list.containsKey(carsKey)){
				return 0;
			}
			
			if(trans.updates_list.containsKey(carsKey)){
				car = (Car) trans.updates_list.get(carsKey);
				return car.getPrice();
			}

			car = cars.get(location);
			if(car != null) {
				return car.getPrice();
			}
		} catch (DeadlockException de){
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		}
		return 0;
    }
		

	@Override
    public int queryCustomerBill(int xid, String custName) throws RemoteException, TransactionAbortedException, InvalidTransactionException { 
		validate(xid, custName, INVALID_CUST_NAME);
		try { 
			String custKey = CUSTOMERS + custName;
			boolean custLockAcquired = lm.lock(xid, custKey, LockManager.READ);

			if(!custLockAcquired) { 
				return 0;
			}

			Customer customer = customers.get(custName);

			if(customer == null){
				return 0;
			}

			List<Reservation> customerReservations = customer.getReservations();
			
			int billTotal = 0;

			if(customerReservations == null || customerReservations.size() < 1) {
				return billTotal; //customer doesnt exist and/or no reservations for customer;
			}

			for(Reservation r: customerReservations) {
				int resType = r.getResvType();
				switch (resType){
					case 1:
						billTotal += queryFlightPrice(xid, r.getResvKey());
						break;
					case 2:
						billTotal += queryRoomsPrice(xid, r.getResvKey());
						break;
					case 3:
						billTotal += queryCarsPrice(xid, r.getResvKey());
						break;
					default:
						return 0;
				}
			}
			return billTotal;
		} catch (DeadlockException de) {
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		}
    }


    // RESERVATION INTERFACE
	@Override
    public boolean reserveFlight(int xid, String custName, String flightNum) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		validate(xid, flightNum, INVALID_FLIGHT_NUM);

		try {
			String flightKey = FLIGHTS + flightNum;
			boolean flightLockAcquired = lm.lock(xid, flightKey, LockManager.WRITE);
			
			if(!flightLockAcquired){  //if we couldnt get the flight lock
				return false;
			}

			Transaction trans = activeTrans.get(xid);

			String custKey = CUSTOMERS + custName;
			boolean customerLockAcquired = lm.lock(xid, custKey, LockManager.WRITE);
			if(!customerLockAcquired) {
				return false;
			}

			if(trans.deletes_list.containsKey(flightKey)){
				return false;
			}

			Flight flight = null;  
			if(trans.updates_list.containsKey(flightKey)){
				flight = (Flight) trans.updates_list.get(flightKey);
			}
			else{
				Flight f1 = flights.get(flightNum);	
				if(f1 != null){
					flight = new Flight(flightNum, f1.getNumSeats(), f1.getNumAvail(), f1.getPrice());
				}
			}
	
			//wasnt in update list or database, must be new flight
			if(flight == null){
				return false;
			}

			if(flight.getNumAvail() - 1 < 0){
				return false;
			}

			flight.setNumAvail(flight.getNumAvail() - 1);
				
			trans.updates_list.put(flightKey, flight);

			Customer customer;
			if(trans.updates_list.containsKey(custKey)){
				customer = (Customer) trans.updates_list.get(custKey);
			}
			else{
				customer = customers.get(custName); 
			}

			if(customer == null){
				customer = new Customer(custName);
			}

			customer.addReservation(new Reservation(Reservation.FLIGHT, flightNum));
					
			trans.updates_list.put(custKey, customer);
			return true;
		} catch (DeadlockException de) {
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		}
    }
 
	
	@Override
    public boolean reserveCar(int xid, String custName, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
			
		validate(xid, location, INVALID_CAR_LOCATION);
		
		try {
			String carKey = CARS + location;
			boolean carLockAcquired = lm.lock(xid, carKey , LockManager.WRITE);
			if(!carLockAcquired){  //if we couldnt get the car lock
				return false;
			}

			Transaction trans = activeTrans.get(xid);

			String custKey = CUSTOMERS + custName;
			boolean customerLockAcquired = lm.lock(xid, custKey, LockManager.WRITE);

			if(customerLockAcquired) {

				Car car = null;  
				if(trans.updates_list.containsKey(carKey)){
					car = (Car) trans.updates_list.get(carKey);
				}
				else{
					Car c1 = (Car) cars.get(location);	
					if(c1 != null){
						car = new Car(location, c1.getPrice(), c1.getNumCars(), c1.getNumAvail());
					}
				}
	
				//wasnt in update list or database, must be new flight
				if(car == null){
					return false;
				}

				if(car.getNumAvail() - 1 < 0){
					return false;
				}

				car.setNumAvail(car.getNumAvail() - 1);
				
				trans.updates_list.put(carKey, car);

				Customer customer;
				if(trans.updates_list.containsKey(custKey)){
					customer = (Customer) trans.updates_list.get(custKey);
				}
				else{
					customer = customers.get(custName); 
				}

				if(customer == null){
					customer = new Customer(custName);
				}

				customer.addReservation(new Reservation(Reservation.CAR, location));
				
				trans.updates_list.put(custKey, customer);
				return true;
			}
		} catch (DeadlockException de) {
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		}
		return false; 
    }

	@Override
    public boolean reserveRoom(int xid, String custName, String location) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		validate(xid, custName, INVALID_CUST_NAME);
		
		try {
			String hotelKey = HOTELS + location;
			boolean hotelLockAcquired = lm.lock(xid, hotelKey, LockManager.WRITE);

			if(!hotelLockAcquired){  //if we couldnt get the flight lock
				return false;
			}

			Transaction trans = activeTrans.get(xid);

			String custKey = CUSTOMERS + custName;
			boolean customerLockAcquired = lm.lock(xid, custKey, LockManager.WRITE);

			if(customerLockAcquired) {

				Hotel hotel = null;  
				if(trans.updates_list.containsKey(hotelKey)){
					hotel = (Hotel) trans.updates_list.get(hotelKey);
				}
				else{
					Hotel h1 = rooms.get(location);	
					if(h1 != null){
						hotel = new Hotel(location, h1.getPrice(), h1.getNumRooms(), h1.getNumAvail());
					}				
				}
	
				//wasnt in update list or database, must be new flight
				if(hotel == null){
					return false;
				}

				if(hotel.getNumAvail() - 1 < 0){
					return false;
				}

				hotel.setNumAvail(hotel.getNumAvail() - 1);
				trans.updates_list.put(hotelKey, hotel);

				Customer customer;
				if(trans.updates_list.containsKey(custKey)){
					customer = (Customer) trans.updates_list.get(custKey);
				}
				else{
					customer = customers.get(custName); 
				}

				if(customer == null){
					customer = new Customer(custName);
				}

				customer.addReservation(new Reservation(Reservation.ROOM, location));

				trans.updates_list.put(custKey, customer);
				return true;
			}
		} catch (DeadlockException de) {
			lm.unlockAll(xid);
			abortedTrans.put(xid, activeTrans.remove(xid));
			throw new TransactionAbortedException(xid, "xid: " + "Transaction resulted in a deadlock , Aborting...");
		}
		throw new RemoteException();
    }

    // TECHNICAL/TESTING INTERFACE
	@Override
    public boolean shutdown() throws RemoteException {
		//flush all contents in the shadow copies to disk
		System.exit(0);
		return true;
    }

	@Override
    public boolean dieNow() throws RemoteException {
		System.exit(1);
		// We won't ever get here since we exited above; but we still need it to please the compiler.
		return true; 
    }

	@Override
    public boolean dieBeforePointerSwitch() throws RemoteException {
		dieBefore = true;
		return true;
    }

	@Override
    public boolean dieAfterPointerSwitch() throws RemoteException {
		dieAfter = true;
		return true;
    }

	public void validate(int xid, String key, String message) throws InvalidTransactionException, TransactionAbortedException{
		validateTransaction(xid);
		if(key == null || key.length() < 1) {
			throw new InvalidTransactionException(xid, message + ": key");
		}
	}

	public void ClearLocks(int xid){
		lm.unlockAll(xid);
	}

	public static boolean getDieAfter() {
		return dieAfter;
	}

	public static boolean getDieBefore() {
		return dieBefore;
	}

	public void setResourceName(String resourceName){
		this.resourceName = resourceName;
	}

	public boolean recover(){
		File file = new File(DATA_DIR + DB_NAME);

		try{
			//pointer file exists, recover all the tables and init the db
			if(!file.exists()){
				return createInitialDatabaseTable();	
			}
		} catch(IOException ioe) {
			return false;
		}

		return recoverTables();
	}

	private Object getObjectFromInput(String tableName){
		StringBuilder tableNameSB = new StringBuilder(DATA_DIR);
		tableNameSB.append(tableName).append(Table.tableExt);

		File file = new File(tableNameSB.toString());
		
		Object tableObj = null;
		if(!file.exists()){
			return tableObj;
		}
		

		try{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(tableNameSB.toString()));
			tableObj = in.readObject();
			in.close();
		} catch(IOException ioe) {
			return false;
		} catch (ClassNotFoundException cnf){
			System.err.println("error in getObjectFromInput method: " + tableName);
			return false;
		}
		return tableObj;
	}

	private boolean recoverTables(){
		flights = new HashMap<String, Flight>();	
		Object mapObject = getObjectFromInput(FLIGHTS);
		if(mapObject != null){
			flights = (Map<String, Flight>) mapObject;	
		}

		cars = new HashMap<String, Car>();	
		mapObject = getObjectFromInput(CARS);
		if(mapObject != null){
			cars = (Map<String, Car>) mapObject;	
		}

		customers = new HashMap<String, Customer>();
		mapObject = getObjectFromInput(CUSTOMERS);
		if(mapObject != null){
			customers = (Map<String, Customer>) mapObject;	
		}

		rooms = new HashMap<String, Hotel>();	
		mapObject = getObjectFromInput(HOTELS);
		if(mapObject != null){
			rooms = (Map<String, Hotel>) mapObject;	
		}
		
		return true;
	}

	private boolean createInitialDatabaseTable() throws IOException {
		//if any table doesnt exists, the file need to be created
		if(resourceName.equals("all")){
			flights = new HashMap<String, Flight>();
			rooms = new HashMap<String, Hotel>();
			customers = new HashMap<String, Customer>();
			cars = new HashMap<String, Car>();
		}
		
		try {
			File travelDBFile = new File(DATA_DIR + "travel.db");
			boolean createDBFile = travelDBFile.createNewFile();

			if(createDBFile){
				FileWriter fw = new FileWriter(travelDBFile);
				fw.close();
			}
		} catch (IOException ioe) {
			System.err.println("Could not initialize the database");
			throw new IOException();
		} 
		return true;
	}

	public boolean commit(Transaction transaction) throws TransactionAbortedException {
		System.out.println("begin: commit function"); 
		synchronized(semaphore){
			System.out.println("begin: flights copy");
			Map<String, Flight> flightsCopy = new HashMap<String, Flight>();
			flightsCopy.putAll(flights);
			System.out.println("end: flights copy");
		
			System.out.println("begin: customer copy");
			Map<String, Customer> customersCopy = new HashMap<String, Customer>(); 
			customersCopy.putAll(customers);
			System.out.println("end: customer copy");
	
			System.out.println("begin: cars copy");
			Map<String, Car> carsCopy = new HashMap<String, Car>(); 
			carsCopy.putAll(cars);
			System.out.println("end: cars copy");

			System.out.println("begin: rooms copy");
			Map<String, Hotel> roomsCopy = new HashMap<String, Hotel>(); 
			roomsCopy.putAll(rooms);
			System.out.println("end: rooms copy");
	
			System.out.println("before updates");
			boolean hadChanges = false;
			List<TableRow> updates = transaction.getUpdates();
			if(updates != null && updates.size() > 0) {
				hadChanges = true;
				for(TableRow update: updates) {
					if(update instanceof Flight){
						Flight f = (Flight) update;
						flightsCopy.put(f.getFlightNum(), f);
					}
					else if(update instanceof Car) {
						Car c = (Car) update;
						carsCopy.put(c.getLocation(), c);
					}
					else if(update instanceof Customer){
						Customer c = (Customer) update;
						customersCopy.put(c.getCustName(), c);
					}
					else if(update instanceof Hotel){
						Hotel h = (Hotel) update;
						roomsCopy.put(h.getLocation(), h);
					}
				}
			} 
			System.out.println("after updates");
	
			System.out.println("before deletes");
			List<TableRow> deletes = transaction.getDeletes();
			if(deletes != null && deletes.size() > 0) {
				hadChanges = true;
				for(TableRow update: deletes) {
					if(update instanceof Flight){
						Flight f = (Flight) update;
						flightsCopy.remove(f.getFlightNum());
					}
					else if(update instanceof Customer){
						Customer c = (Customer) update; 
						customers.remove(c.getCustName());
					}
				}
			}
			System.out.println("after deletes");
	
			if(dieBefore){
				System.out.println("Die before pointer switch now executing.");	
				System.exit(-1);
			}
	
			if(!hadChanges){
				return true;
			}
			
			flights = flightsCopy;
			cars = carsCopy;
			customers = customersCopy;			
			rooms = roomsCopy;
	
			try{
				System.out.println("begin: writing tables to disk.");
				Table.writeTable(flights, getTableFileName(FLIGHTS));
				Table.writeTable(cars, getTableFileName(CARS));
				Table.writeTable(customers, getTableFileName(CUSTOMERS));
				Table.writeTable(rooms, getTableFileName(HOTELS));
				System.out.println("end: writing tables to disk.");
			} catch(IOException ioe){
				System.err.println("Error persisting tables during commit");
			}
	
			if(dieAfter){
				System.out.println("Die after pointer switch now executing.");	
				System.exit(-1);
			}
			System.out.println("end: commit function"); 
			return true;
		}
	}

	private static String getTableFileName(String tableName) {
		StringBuilder tableNameSB = new StringBuilder(DATA_DIR);
		tableNameSB.append(tableName).append(Table.tableExt);
		return tableNameSB.toString();
	}

	/**
	 * DEBUGGING UTILITIES BELOW THIS COMMENT, NOT PART OF PROJECT
	 */

	public static void clearTables(){
		Table.removeTableFile(getTableFileName(FLIGHTS));
		Table.removeTableFile(getTableFileName(CARS));
		Table.removeTableFile(getTableFileName(CUSTOMERS));
		Table.removeTableFile(getTableFileName(HOTELS));
		Table.removeTableFile(DATA_DIR + "travel.db");
	}

	public void printTables() throws IOException{
		System.out.print("\f");
		System.out.println("########################### Printing Table: " + FLIGHTS + " ###########################");
		printFlights();
		System.out.println("\n\n\n");
		System.out.println("########################### Printing Table: " + CUSTOMERS + " ###########################");
		printCustomers();
		System.out.println("\n\n\n");
		System.out.println("########################### Printing Table: " + CARS + " ###########################");
		printCars();
		System.out.println("\n\n\n");
		System.out.println("########################### Printing Table: " + HOTELS + " ###########################");
		printRooms();
		System.out.println("\n\n\n");
	}	

	public void printFlights() {
		if(flights == null || flights.size() <= 0 || flights.keySet().size() <= 0){
			System.out.println("No flights found in the table.");
			return;
		} 
		for(String flightNum : flights.keySet()) {
			Flight flight = flights.get(flightNum);
			System.out.print("Flight Num : " + flight.getFlightNum());
			System.out.printf("\t\t%s: %s", "Num Seats: ", flight.getNumSeats());
			System.out.printf("\t\t%s: %s", "Avail Seats: ", flight.getNumAvail());
			System.out.printf("\t\t%s: %s\n", "Price p/Seat: ", flight.getPrice());
		}
	}

	public void printCustomers() {
		if(customers == null || customers.size() <= 0){
			System.out.println("No customers found in the table.");
			return;
		}
		for(String custName : customers.keySet()) {
			Customer customer = customers.get(custName);
			List<Reservation> reservations = customer.getReservations();
			System.out.println("Customer: " + custName + " has " + reservations.size() + " reservations");
			if(reservations != null && reservations.size() >  0){
				for(Reservation r: reservations){
					System.out.printf("\t\t%s: %s", "ResvType: ", r.getResvType());
					System.out.printf("\t\t%s: %s\n", "ResvKey: ", r.getResvKey());				
				} 
			}
			else{
				System.out.println("\t\tCustomer has no reservations.");
			}
		}
	}

	public void printCars() {
		if(cars == null || cars.size() <= 0 || cars.keySet().size() <= 0){
			System.out.println("No cars found in the table.");
			return;
		}
		for(String location : cars.keySet()) {
			Car car = cars.get(location);
			System.out.print("Car Location: " + car.getLocation());
			System.out.printf("\t\t%s: %s", "Num Cars: ", car.getNumCars());
			System.out.printf("\t\t%s: %s", "Avail Cars: ", car.getNumAvail());
			System.out.printf("\t\t%s: %s\n", "Price p/Car: ", car.getPrice());
		}
	}

	public void printRooms() {
		if(rooms == null || rooms.size() <= 0 || rooms.keySet().size() <= 0){
			System.out.println("No hotels found in the table.");
			return;
		}
		for(String location : rooms.keySet()) {
			Hotel hotel = rooms.get(location);
			System.out.print("Hotel Location: " + hotel.getLocation());
			System.out.printf("\t\t%s: %s", "Num Rooms: ", hotel.getNumRooms());
			System.out.printf("\t\t%s: %s", "Avail Rooms: ", hotel.getNumAvail());
			System.out.printf("\t\t%s: %s\n", "Price p/Room: ", hotel.getPrice());
		} 
	}
}