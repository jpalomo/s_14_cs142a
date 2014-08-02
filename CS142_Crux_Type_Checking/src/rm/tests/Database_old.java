///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package rm.tests;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Scanner;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import transaction.Car;
//import transaction.Customer;
//import transaction.Flight;
//import transaction.Hotel;
//import transaction.Reservation;
//import transaction.ResourceManagerImpl;
//import transaction.T_Cars;
//import transaction.T_Customers;
//import transaction.T_Flights;
//import transaction.T_Hotels;
//import transaction.T_Reservations;
//import transaction.Table;
//import transaction.TableRow;
//import transaction.Transaction;
//import transaction.TransactionAbortedException;
//
///**
// *
// * @author Palomo
// */
//public class Database_old {
//
//	//Strings representing the table names
//	public static final String FLIGHTS = "flights";
//	public static final String CARS = "cars";
//	public static final String RESERVATIONS = "reservations";
//	public static final String HOTELS = "hotels";
//	public static final String CUSTOMERS = "customers";
//	public static final String DB_NAME = "travel.db"; //version pointer file for db
//
//	private static Integer currentVersion = 0;	
//	private static Database_old db = null;
//	
//	private static Map<String, Table> database; 
//	private static final String DATA_DIR;
//	private static final String DB_PATH;
//
//	private static final List<String> tableOrdering; //internal order of the tables
//	//private static final HashMap<String, Integer> tableOrderingMap; //map integers starting at 1 to the number of tables
//
//	static {
//		tableOrdering = Arrays.asList(CUSTOMERS, FLIGHTS, CARS, RESERVATIONS, HOTELS);	
//		db = new Database_old();
//		database = new HashMap<String, Table>(); 
//		DATA_DIR = ResourceManagerImpl.DATA_DIR;
//		DB_PATH = new StringBuilder(DATA_DIR).append(DB_NAME).toString();
//	} 
//
//	public Database_old() {
//		super();
//	}
//
//	public Database_old(Map<String, Table> db){
//		this.database = db;
//	}
//
//	/**
//	 * This method recovers the database from a previous state or creates a new
//	 * database.  If the database size less than 5, either the database has never been 
//	 * initialized by which we need to create the empty table files and set the 
//	 * current version of the db.  Otherwise, the files are on disk, so we need to 
//	 * read the pointer file which indicates the last consistent version of the db
//	 * by reading the associated versioned table files.  If the recover method is
//	 * called when the db is initialized with all tables, the db pointer is returned.
//	 * 
//	 * @param dataDir
//	 * @return 
//	 */
//	public static Database_old recover(){
//		if(database.size() < 5) { //database will be less than 5 if called twice 
//			
//			//create a new instance
//			synchronized(db){
//				//see if the db file exists to get the current version
//				File file = new File(DB_PATH);
//
//				//pointer file exists, recover all the tables and init the db
//				if(file.exists()){
//					try {
//						Scanner scanner = new Scanner(file);
//						currentVersion = scanner.nextInt();
//						scanner.close();
//
//						if(currentVersion == 1){
//							db.createInitialDatabase();
//							return db; 
//						}
//
//						boolean tablesRecovered = recoverTables(currentVersion);
//
//						if(tablesRecovered){
//							db.setCurrentVersion(currentVersion);
//							db.setDatabase(database);
//						}		
//
//						return db;
//					}
//					catch(FileNotFoundException fnf){
//						//first time initializing the db
//						try {
//							FileWriter fw = new FileWriter(file);
//							fw.write(currentVersion);
//							fw.flush();
//							fw.close();
//						}
//						catch (IOException ioe) {
//							//cant initialize exit
//						}
//					}
//				}
//				else {
//					//first time initializing the database
//					db.createInitialDatabase();
//				}
//			}
//		}	
//		//return the already created db
//		return db;
//	}
//
//
//	/**
//	 * This method will read all the serialized files for the tables that are on disk.
//	 * @param currentVersion
//	 * @return 
//	 */
//	private static boolean recoverTables(int currentVersion){
//		FileInputStream tableFile;
//
//		for(int i = 0; i < tableOrdering.size(); i ++) {
//			String tableName = tableOrdering.get(i);
//
//			//create the table string (TableName_Version.ser)
//			StringBuilder tableSB = new StringBuilder(DATA_DIR);
//			tableSB.append(tableName).append("_").append(currentVersion).append(Table.tableExt);
//			
//			try {
//				File file = new File(tableSB.toString());
//
//				if(file.length() == 0) {
//					db.createInitialDatabase();
//					return true;
//				}
//				
//				boolean isEmptyFile = false;
//				if(file.length() <= 0) {
//					isEmptyFile = true;	
//				}
//				
//				tableFile = new FileInputStream(tableSB.toString());
//			
//				ObjectInputStream in = new ObjectInputStream(tableFile);
//				Object tableObj = in.readObject();
//				in.close();
//
//
//				if(tableName.equals(FLIGHTS)){
//					Map<String, Flight> flights = new HashMap<String, Flight>();	
//					
//					if(!isEmptyFile){
//						flights = (Map<String, Flight>) tableObj;	
//					}
//
//					T_Flights flightsTable = new T_Flights(flights);
//					database.put(FLIGHTS, flightsTable);
//				}
//				else if(tableName.equals(CARS)) {
//					Map<String, Car> cars = new HashMap<String, Car>();	
//
//					if (!isEmptyFile) {
//						cars = (Map<String, Car>) tableObj;	
//					}
//
//					T_Cars carsTable = new T_Cars(cars);
//					database.put(CARS, carsTable);
//				}
//				else if(tableName.equals(CUSTOMERS)) {
//					HashSet<String> customers = (HashSet<String>) tableObj;	
//
//					if(!isEmptyFile) {
//						customers = (HashSet<String>) tableObj;	
//					}
//					T_Customers customersTable = new T_Customers(customers);
//					database.put(CUSTOMERS, customersTable);
//				}
//				else if(tableName.equals(HOTELS)) {
//					Map<String, Hotel> hotels = new HashMap<String, Hotel>();	
//					if(!isEmptyFile) {
//						hotels = (Map<String, Hotel>) tableObj;
//					}	
//					T_Hotels hotelsTable = new T_Hotels(hotels);
//					database.put(HOTELS, hotelsTable);
//				}
//				else if(tableName.equals(RESERVATIONS)) {
//					Map<String, List<Reservation>> reservations = new HashMap<String, List<Reservation>>();	
//
//					if(!isEmptyFile){
//						reservations = (Map<String, List<Reservation>>) tableObj;	
//					}
//
//					T_Reservations reservationsTable = new T_Reservations(reservations);
//					database.put(RESERVATIONS, reservationsTable);
//				}
//				else {
//					continue;	
//				}
//
//			} catch (IOException ex) {
//				Logger.getLogger(T_Flights.class.getName()).log(Level.SEVERE, null, ex);
//				return false;
//			} catch (ClassNotFoundException ex) {
//				Logger.getLogger(T_Flights.class.getName()).log(Level.SEVERE, null, ex);
//				return false;
//			}
//		}
//		return true;
//	}
//
//	private void createInitialDatabase(){
//		//if any table doesnt exists, the file need to be created
//		currentVersion++;
//		database.put(CARS, T_Cars.createInstance(getTableFileName(DATA_DIR, CARS, currentVersion)));
//		database.put(FLIGHTS, T_Flights.createInstance(getTableFileName(DATA_DIR, FLIGHTS, currentVersion)));
//		database.put(CUSTOMERS, T_Customers.createInstance(getTableFileName(DATA_DIR, CUSTOMERS, currentVersion)));
//		database.put(HOTELS, T_Hotels.createInstance(getTableFileName(DATA_DIR, HOTELS, currentVersion)));
//		database.put(RESERVATIONS, T_Reservations.createInstance(getTableFileName(DATA_DIR, RESERVATIONS, currentVersion)));
//		
//		try {
//			File travelDBFile = new File(DB_PATH);
//			boolean createDBFile = travelDBFile.createNewFile();
//
//			if(createDBFile){
//				FileWriter fw = new FileWriter(travelDBFile);
//				fw.write(String.valueOf(currentVersion));
//				fw.close();
//			}
//		} catch (IOException ioe) {
//			--currentVersion;
//			System.err.println("Could not initialize the database");
//		} 
//	}
//
//	private static String getTableFileName(String dataDir, String tableName, int currentVersion){
//		StringBuilder sb = new StringBuilder(dataDir);
//		sb.append(File.separator).append(tableName).append("_").append(currentVersion).append(Table.tableExt);
//		return sb.toString();
//	}
//
//	public List<Reservation> queryCustReservations(String custName){
//		Table reservationsTable = database.get(RESERVATIONS);
//		List<Reservation> customerReservations = (List<Reservation>) reservationsTable.query(custName);
//
//		if(customerReservations == null){
//			return null;
//		}
//
//		return customerReservations;
//	}
//
//	public TableRow query(String tableName, String key){
//		TableRow tableRow = null;
//
//		if(tableName.equals(FLIGHTS)){
//			T_Flights flights = (T_Flights) database.get(FLIGHTS);
//			tableRow = (TableRow) flights.query(key);
//		}
//		else if(tableName.equals(CARS)){
//			T_Cars cars = (T_Cars) database.get(CARS);
//			tableRow = (TableRow) cars.query(key);
//		}
//		else if(tableName.equals(CUSTOMERS)){
//			T_Customers cust = (T_Customers) database.get(CUSTOMERS);
//			tableRow = (TableRow) cust.query(key);
//		}
//		else if(tableName.equals(RESERVATIONS)){
//			T_Reservations resv = (T_Reservations) database.get(RESERVATIONS);
//			tableRow = (TableRow) resv.query(key);
//		}
//		else if(tableName.equals(HOTELS)){
//			T_Hotels hotels = (T_Hotels) database.get(HOTELS);
//			tableRow = (TableRow) hotels.query(key);
//		}
//		return tableRow;
//	}
//
//	public boolean isReservationExists(int resType, String resKey){
//		//make sure there are no reservations for the flight we are trying to delete
//		T_Reservations reservationsTable = (T_Reservations) database.get(RESERVATIONS);
//		return reservationsTable.findReservation(resType, resKey);
//	}
//
//	public boolean commit(Transaction transaction) throws TransactionAbortedException{
//		int priorToWrite;
//		int followingWrite;
//		HashMap<String, Table> currentDB;
//
//		synchronized(currentVersion){
//			priorToWrite = currentVersion;	
//			currentDB = new HashMap<String, Table>();
//			currentDB.putAll(database); //exact replica at this point
//		}
//		
//		Table table = null;
//		boolean success = true;
//		
//		List<TableRow> updates = transaction.getUpdates();
//		if(updates != null && updates.size() > 0) {
//			for(TableRow update: updates) {
//				if(update instanceof Flight){
//					table = currentDB.get(FLIGHTS);
//					success = table.insertRow(update);
//				}
//				else if(update instanceof Car) {
//					table = currentDB.get(CARS);
//					success = table.insertRow(update);
//				}
//				else if(update instanceof Customer){
//					table = currentDB.get(CUSTOMERS);
//					success = table.insertRow(update);
//				}
//				else if(update instanceof Hotel){
//					table = currentDB.get(HOTELS);
//					success = table.insertRow(update);
//				}
//				else if(update instanceof Reservation){
//					table = currentDB.get(RESERVATIONS);
//					success = table.insertRow(update); 
//				}
//
//				if(!success){
//					throw new TransactionAbortedException(transaction.getXid(), "Failed to commit insertion and/or updates.");
//				}	
//		
//			}
//		} 
//
//		List<TableRow> deletes = transaction.getDeletes();
//		if(deletes != null && deletes.size() > 0) {
//			for(TableRow update: deletes) {
//				if(update instanceof Flight){
//					table = currentDB.get(FLIGHTS);
//					success = table.deleteRow(update);
//				}
//				else if(update instanceof Car) {
//					table = currentDB.get(CARS);
//					success = table.insertRow(update);
//				}
//				else if(update instanceof Customer){
//					table = currentDB.get(CUSTOMERS);
//					success = table.deleteRow(update);
//				}
//				else if(update instanceof Hotel){
//					table = currentDB.get(HOTELS);
//					success = table.insertRow(update);
//				}
//				else if(update instanceof Reservation){
//					table = currentDB.get(RESERVATIONS);
//					success = table.deleteRow(update); 
//				}
//
//				if(!success){
//					throw new TransactionAbortedException(transaction.getXid(), "Failed to commit deletes.");
//				}
//			}
//		}
//
//		if(ResourceManagerImpl.dieBefore){
//			System.exit(-1);
//		}
//		
//		synchronized(currentVersion){
//			followingWrite = currentVersion;	
//
//			if(priorToWrite == followingWrite) {
//				database = new HashMap<String, Table>();
//				database.putAll(currentDB);
//				try {
//					Collection<Table> dbTables = database.values();
//					for(Table tableToCommit: dbTables){
//						tableToCommit.commit(currentVersion + 1);
//					}
//
//					FileWriter fw = new FileWriter(new File(DB_PATH));
//					fw.write(String.valueOf(currentVersion + 1));
//					fw.close();
//					currentVersion++;
//
//					//remove previous versions now that file pointer has been updated
//					for(Table tableToRemove: dbTables){
//						tableToRemove.removeTableFile(currentVersion - 1);
//					}	
//
//					if(ResourceManagerImpl.getDieAfter()){
//						System.exit(-1);
//					}
//					
//					return true;
//				} catch (IOException ioe){
//					throw new TransactionAbortedException(transaction.getXid(), "Could not finish commit: " + ioe.getMessage());
//				}
//			}
//		}
//		throw new TransactionAbortedException(transaction.getXid(), "Could not finish commit.");
//	}
//
//	public int getCurrentVersion(){
//		return currentVersion;
//	}
//
//	private void setCurrentVersion(int currentVersion){
//		this.currentVersion = currentVersion;	
//	}
//
//	public static List<String> getTableOrdering(){
//		return tableOrdering;
//	}
//
//	private void setDatabase(Map<String, Table> tableHash){
//		this.database = tableHash;
//	}
//
//	/**
//	 * Utility method used for debugging. 
//	 * 
//	 * @param tableName 
//	 */
//	public void printTable(String tableName){
//		System.out.println("########################### Printing Table: " + tableName + " ###########################");
//		Table table = database.get(tableName);
//		table.printTable();
//		System.out.println("\n\n\n");
//	}
//
//	public Map<String, Table> getDataCopy(){
//		HashMap<String, Table> dbCopy = new HashMap<String, Table>();
//		synchronized(database){
//			dbCopy.putAll(database);
//		}
//		return dbCopy;
//	} 
//
//	public boolean insertLocal(TableRow update){
//		Table table;
//		if(update instanceof Flight){
//			table = database.get(FLIGHTS);
//			return table.insertRow(update);
//		}
//		else if(update instanceof Car) {
//			table = database.get(CARS);
//			return table.insertRow(update);
//		}
//		else if(update instanceof Customer){
//			table = database.get(CUSTOMERS);
//			return table.insertRow(update);
//			}
//		else if(update instanceof Hotel){
//			table = database.get(HOTELS);
//			return table.insertRow(update);
//		}
//		else if(update instanceof Reservation){
//			table = database.get(RESERVATIONS);
//			return table.insertRow(update); 
//		}
//		return false;
//	}
//
//	public boolean insertLocalDelete(TableRow update){
//		Table table;
//		if(update instanceof Flight){
//			table = database.get(FLIGHTS);
//			return table.deleteRow(update);
//		}
//		else if(update instanceof Car) {
//			table = database.get(CARS);
//		 	return table.insertRow(update);
//		}
//		else if(update instanceof Customer){
//			table = database.get(CUSTOMERS);
//			return table.deleteRow(update);
//		}
//		else if(update instanceof Hotel){
//			table = database.get(HOTELS);
//			return table.insertRow(update);
//		}
//		else if(update instanceof Reservation){
//			table = database.get(RESERVATIONS);
//			return table.deleteRow(update); 
//		}
//		return false;
//	}
//
//	public  Map<String, List<Reservation>> getAllReservations(){
//		T_Reservations tble = (T_Reservations) database.get(RESERVATIONS);
//		return tble.getReservations();
//	}
//}