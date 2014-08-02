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
//import transaction.ResourceManager;
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
//public class Database {
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
//	private static Database db = null;
//	
//	private static Map<String, Table> database; 
//	private static final String DATA_DIR;
//	private static final String DB_PATH;
//
//	//private static final HashMap<String, Integer> tableOrderingMap; //map integers starting at 1 to the number of tables
//
//
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
//	public static Database recover(String resourceName){
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
//							db.createInitialDatabaseTable(resourceName);
//							return db; 
//						}
//
//						boolean tablesRecovered = recoverTables(currentVersion, resourceName);
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
//					db.createInitialDatabaseTable(resourceName);
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
//	private static boolean recoverTables(int currentVersion, String resourceName){
//		FileInputStream tableFile;
//
//		//create the table string (TableName_Version.ser)
//		StringBuilder tableSB = new StringBuilder(DATA_DIR);
//		//tableSB.append(tableName).append("_").append(currentVersion).append(Table.tableExt);
//		
//		try {
////				isEmptyFile = true;	
////			} 
//
//			if(resourceName.equals(ResourceManager.RMINameFlights)){
//				tableSB.append(FLIGHTS).append("_").append(currentVersion).append(Table.tableExt); 
//				File file = new File(tableSB.toString());
//
//				if(file.length() <= 0) {
//					db.createInitialDatabaseTable(resourceName);
//					return true;
//				}
//				
//				tableFile = new FileInputStream(tableSB.toString());
//				ObjectInputStream in = new ObjectInputStream(tableFile);
//				Object tableObj = in.readObject();
//				in.close();
//
//				Map<String, Flight> flights = new HashMap<String, Flight>();	
//			
//				flights = (Map<String, Flight>) tableObj;	
//
//				T_Flights flightsTable = new T_Flights(flights);
//				database.put(FLIGHTS, flightsTable);
//			}
//			else if(resourceName.equals(ResourceManager.RMINameCars)){
//				tableSB.append(CARS).append("_").append(currentVersion).append(Table.tableExt); 
//				File file = new File(tableSB.toString());
//
//				if(file.length() <= 0) {
//					db.createInitialDatabaseTable(resourceName);
//					return true;
//				}
//				
//				tableFile = new FileInputStream(tableSB.toString());
//				ObjectInputStream in = new ObjectInputStream(tableFile);
//				Object tableObj = in.readObject();
//				in.close();
//				Map<String, Car> cars = new HashMap<String, Car>();	
//
//				cars = (Map<String, Car>) tableObj;	
//
//				T_Cars carsTable = new T_Cars(cars);
//				database.put(CARS, carsTable);
//			}
//			else if(resourceName.equals(ResourceManager.RMINameCustomers)){
//				tableSB.append(CUSTOMERS).append("_").append(currentVersion).append(Table.tableExt); 
//				File file = new File(tableSB.toString());
//
//				if(file.length() <= 0) {
//					db.createInitialDatabaseTable(resourceName);
//					return true;
//				}
//
//				tableFile = new FileInputStream(tableSB.toString());
//				ObjectInputStream in = new ObjectInputStream(tableFile);
//				Object tableObj = in.readObject();
//				in.close();
//				HashSet<String> customers = (HashSet<String>) tableObj;	
//
//				customers = (HashSet<String>) tableObj;	
//				
//				T_Customers customersTable = new T_Customers(customers);
//				database.put(CUSTOMERS, customersTable);
//			}
//			else if(resourceName.equals(ResourceManager.RMINameRooms)){
//				tableSB.append(HOTELS).append("_").append(currentVersion).append(Table.tableExt); 
//				File file = new File(tableSB.toString());
//
//				if(file.length() <= 0) {
//					db.createInitialDatabaseTable(resourceName);
//					return true;
//				}
//				
//				tableFile = new FileInputStream(tableSB.toString());
//				ObjectInputStream in = new ObjectInputStream(tableFile);
//				Object tableObj = in.readObject();
//				in.close();
//				Map<String, Hotel> hotels = new HashMap<String, Hotel>();	
//
//				hotels = (Map<String, Hotel>) tableObj;
//				
//				T_Hotels hotelsTable = new T_Hotels(hotels);
//				database.put(HOTELS, hotelsTable);
//			}
//			else if(resourceName.equals(ResourceManager.RMINameReservations)){
//				tableSB.append(RESERVATIONS).append("_").append(currentVersion).append(Table.tableExt); 
//				File file = new File(tableSB.toString());
//
//				if(file.length() <= 0) {
//					db.createInitialDatabaseTable(resourceName);
//					return true;
//				}
//
//				tableFile = new FileInputStream(tableSB.toString());
//				ObjectInputStream in = new ObjectInputStream(tableFile);
//				Object tableObj = in.readObject();
//				in.close();
//				Map<String, List<Reservation>> reservations = new HashMap<String, List<Reservation>>();	
//
//				reservations = (Map<String, List<Reservation>>) tableObj;	
//
//				T_Reservations reservationsTable = new T_Reservations(reservations);
//				database.put(RESERVATIONS, reservationsTable);
//			}
//
//		} catch (IOException ex) {
//			Logger.getLogger(T_Flights.class.getName()).log(Level.SEVERE, null, ex);
//			return false;
//		} catch (ClassNotFoundException ex) {
//			Logger.getLogger(T_Flights.class.getName()).log(Level.SEVERE, null, ex);
//			return false;
//		}
//		
//		return true;
//	}
//
//	private void createInitialDatabaseTable(String resourceName){
//		//if any table doesnt exists, the file need to be created
//		currentVersion++;
//		if(resourceName.equals(ResourceManager.RMINameFlights)){
//			database.put(FLIGHTS, T_Flights.createInstance(getTableFileName(DATA_DIR, FLIGHTS, currentVersion)));
//		}
//		else if(resourceName.equals(ResourceManager.RMINameCars)){
//			database.put(CARS, T_Cars.createInstance(getTableFileName(DATA_DIR, CARS, currentVersion)));
//		}
//		else if(resourceName.equals(ResourceManager.RMINameCustomers)){
//			database.put(CUSTOMERS, T_Customers.createInstance(getTableFileName(DATA_DIR, CUSTOMERS, currentVersion)));
//		}
//		else if(resourceName.equals(ResourceManager.RMINameRooms)){
//			database.put(HOTELS, T_Hotels.createInstance(getTableFileName(DATA_DIR, HOTELS, currentVersion)));
//		}
//		else if(resourceName.equals(ResourceManager.RMINameReservations)){
//			database.put(RESERVATIONS, T_Reservations.createInstance(getTableFileName(DATA_DIR, RESERVATIONS, currentVersion)));
//		}
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
//	private void setDatabase(Map<String, Table> tableHash){
//		this.database = tableHash;
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
//
//	public  Map<String, List<Reservation>> getAllReservations(){
//		T_Reservations tble = (T_Reservations) database.get(RESERVATIONS);
//		return tble.getReservations();
//	}
//}