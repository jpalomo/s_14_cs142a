/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rm.tests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import transaction.InvalidTransactionException;
import transaction.ResourceManager;
import transaction.ResourceManagerImpl;
import transaction.TransactionAbortedException;

/**
 *
 * @author Palomo
 */
public class TransactionTests {
	public static String[] customers = {"John", "Lindsay", "Bob", "Ryan", "Katie", "Tripper"};	

	public static void main(String args[]) throws RemoteException, TransactionAbortedException, InvalidTransactionException, FileNotFoundException, IOException {
			
		ResourceManagerImpl.clearTables();
		ResourceManagerImpl rm = new ResourceManagerImpl();
		//bstcmt(rm);
		//testSaddcmtdelcmt(rm);
		//LdeadLock(rm);
		//Stoomanyrsv(rm);
		//sbill(rm);

		LdelCus(rm);
		//Lbillres(rm);
		System.exit(1);

		}

		public static void LdelCus(ResourceManagerImpl rm) throws RemoteException, TransactionAbortedException, InvalidTransactionException, IOException{

			int xid =  rm.start();
			boolean returnVal = rm.addCars(xid, "SFO", 300, 30);
			assert returnVal == true;

			returnVal = rm.newCustomer(xid, "John");	
			assert returnVal == true;

			returnVal = rm.commit(xid);
			assert returnVal == true;
			rm.printTables();

			xid =  rm.start();
			returnVal = rm.reserveCar(xid,"John","SFO");
			assert returnVal == true;

			returnVal = rm.commit(xid);
			assert returnVal == true;
			rm.printTables();

			xid =  rm.start();

			int xid2 =  rm.start();

			returnVal = rm.deleteCustomer(xid,"John");
			assert returnVal == true;

			int queryCars = rm.queryCars(xid2, "SFO");

			returnVal = rm.commit(xid);
			assert returnVal == true;
			rm.printTables();

			assert queryCars == 300;
			rm.dieNow();
		}
		
		public static void bstcmt(ResourceManagerImpl rm) throws RemoteException, TransactionAbortedException, InvalidTransactionException{
			int xid = rm.start();
			boolean returnValue = rm.commit(xid);
			System.out.println(returnValue);

		}

		public static void Fdieb4self(ResourceManagerImpl rm) throws RemoteException, TransactionAbortedException, InvalidTransactionException{

			int xid = rm.start();
			rm.addFlight(xid, "347", 100, 310);
			rm.addRooms(xid, "Stanford", 200, 150);
			rm.addCars(xid, "SFO", 300, 30);
			rm.newCustomer(xid, "John");	
			rm.commit(xid);

			xid = rm.start();
			rm.addFlight(xid, "347", 100, 620);
			rm.addRooms(xid, "Stanford", 200, 300);
			rm.addCars(xid, "SFO", 300, 60);
			rm.dieBeforePointerSwitch();	
			try{

				rm.commit(xid);
			}catch(RemoteException e){
				
				System.out.println("Caught remote exception");
			}

			
			xid = rm.start();
			int value = rm.queryFlight(xid, "347");
			System.out.println(value);
			value = rm.queryFlightPrice(xid, "347");
			System.out.println(value);
			value = rm.queryRooms(xid, "Stanford");
			System.out.println(value);
			value = rm.queryRoomsPrice(xid, "Stanford");
			System.out.println(value);
		}
	

		public static void LdeadLock(ResourceManagerImpl rm) throws RemoteException, TransactionAbortedException, InvalidTransactionException, IOException{
			int xid = rm.start();
			rm.addFlight(xid, "347", 100, 310);
			rm.addRooms(xid, "Stanford", 200, 150);
			rm.addCars(xid, "SFO", 300, 30);
			rm.newCustomer(xid, "John");	
			rm.commit(xid);

			xid = rm.start();
			int xid2 = rm.start();
			rm.addFlight(xid2, "347", 100, 620);
			rm.addRooms(xid, "Stanford", 200, 300);
			try {
			rm.queryRooms(xid2, "Stanford");
			}catch(TransactionAbortedException e){
				System.out.println("Caught deadlock");
			}
			rm.commit(xid);
			rm.printTables();

			xid = rm.start();
			int value = rm.queryFlight(xid, "347");
			System.out.println(value);
			value = rm.queryFlightPrice(xid, "347");
			System.out.println(value);
			value = rm.queryRooms(xid, "Stanford");
			System.out.println(value);
			value = rm.queryRoomsPrice(xid, "Stanford");
			System.out.println(value);

		}

		public static void Stoomanyrsv(ResourceManagerImpl rm) throws RemoteException, TransactionAbortedException, InvalidTransactionException, IOException{
			int xid = rm.start();
			rm.addFlight(xid, "347", 1, 310);
			rm.addRooms(xid, "Stanford", 1, 150);
			rm.addCars(xid, "SFO", 1, 30);
			rm.newCustomer(xid, "John");
			rm.newCustomer(xid, "Bob");
			rm.commit(xid);
			rm.printTables();	

			xid = rm.start();
			boolean returnVal = rm.reserveFlight(xid,"John","347");
			returnVal = rm.reserveRoom(xid,"John","Stanford");
			returnVal = rm.reserveCar(xid,"John","SFO");
			returnVal = rm.commit(xid);
			rm.printTables();	

			xid = rm.start();
			returnVal = rm.reserveFlight(xid,"Bob","347");
			returnVal = rm.reserveRoom(xid,"Bob","Stanford");
			returnVal = rm.reserveCar(xid,"Bob","SFO");
			returnVal = rm.commit(xid);
			rm.printTables();	

		}

		
	public static void Saddcmtdelrsv(ResourceManagerImpl rm) throws RemoteException, TransactionAbortedException, InvalidTransactionException, IOException{
			int xid = rm.start();
			rm.addFlight(xid, "347", 100, 310);
			rm.addRooms(xid, "Stanford", 200, 150);
			rm.addCars(xid, "SFO", 300, 30);
			rm.newCustomer(xid, "John");	
			rm.commit(xid);
			rm.printTables();	

			xid = rm.start();
			rm.deleteFlight(xid, "347");
			rm.deleteRooms(xid, "Stanford", 200);
			rm.deleteCars(xid, "SFO", 300);

			boolean returnVal = rm.reserveFlight(xid,"John","347");
			returnVal = rm.reserveRoom(xid,"John","Stanford");
			returnVal = rm.reserveCar(xid,"John","SFO");
			returnVal = rm.commit(xid);
			rm.printTables();	
	}


	public static void testSaddcmtdelcmt(ResourceManagerImpl rm) throws RemoteException, TransactionAbortedException, InvalidTransactionException, IOException{
		int xid = rm.start();
		rm.addRooms(xid, "Stanford", 200, 150);
		rm.addCars(xid, "SFO", 300, 30);
		rm.commit(xid);
		rm.printTables();
			
		xid = rm.start();
		rm.deleteRooms(xid, "Stanford", 5);
		rm.deleteCars(xid, "SFO", 5);
		rm.commit(xid);
		rm.printTables();
	}
	public static void Lbillres(ResourceManagerImpl rm) throws RemoteException, TransactionAbortedException, InvalidTransactionException{
		int xid = rm.start();
		boolean returnVal = rm.addRooms(xid, "Stanford", 200, 150);
		assert returnVal == true;

		returnVal = rm.addCars(xid, "SFO", 300, 30);
		assert returnVal == true;

		returnVal = rm.newCustomer(xid, "John");
		assert returnVal == true;

		returnVal = rm.commit(xid);
		assert returnVal == true;

		xid = rm.start();
		returnVal = rm.reserveRoom(xid,"John","Stanford");
		assert returnVal == true;

		returnVal = rm.commit(xid);
		assert returnVal == true;

		xid = rm.start();

		int xid2 = rm.start();
		int bill = rm.queryCustomerBill(xid, "John");
		assert bill == 150;

		returnVal = rm.reserveCar(xid2, "John", "SFO");
		assert returnVal == true;

		rm.commit(xid);

		rm.dieNow();

	}

	public static void testBaddabtrd(ResourceManagerImpl rm) throws RemoteException, TransactionAbortedException, InvalidTransactionException, IOException{
		int xid = rm.start();
		rm.addFlight(xid, "347", 100, 310);
		rm.addRooms(xid, "Stanford", 200, 150);
		rm.addCars(xid, "SFO", 300, 30);
		rm.newCustomer(xid, "John");
		rm.commit(xid);
		rm.printTables();
		

		xid = rm.start();
		rm.addFlight(xid, "347", 100, 310);
		rm.addRooms(xid, "Stanford", 200, 150);
		rm.addCars(xid, "SFO", 300, 30);
		rm.abort(xid);
		rm.printTables();
		
		xid = rm.start();
		int avail = rm.queryFlight(xid, "347");
		System.out.println(avail);
		avail = rm.queryFlightPrice(xid, "347");
		System.out.println(avail);
		avail = rm.queryRooms(xid, "Stanford");
		System.out.println(avail);
		avail = rm.queryRoomsPrice(xid, "Stanford");
		System.out.println(avail);
		avail = rm.queryCars(xid, "SFO");
		System.out.println(avail);
		avail = rm.queryCarsPrice(xid, "SFO");
		System.out.println(avail);
		avail = rm.queryCustomerBill(xid, "John");
		System.out.println(avail);


	}

	public static void testBaddcmtrd(ResourceManagerImpl rm) throws RemoteException, TransactionAbortedException, InvalidTransactionException, IOException{
		int xid = rm.start();
		rm.addFlight(xid, "347", 100, 310);
		rm.addRooms(xid, "Stanford", 200, 150);
		rm.addCars(xid, "SFO", 300, 30);
		rm.newCustomer(xid, "John");
		rm.commit(xid);
		rm.printTables();
		
		xid = rm.start();
		int avail = rm.queryFlight(xid, "347");
		System.out.println(avail);
		avail = rm.queryFlightPrice(xid, "347");
		System.out.println(avail);
		avail = rm.queryRooms(xid, "Stanford");
		System.out.println(avail);
		avail = rm.queryRoomsPrice(xid, "Stanford");
		System.out.println(avail);
		avail = rm.queryCars(xid, "SFO");
		System.out.println(avail);
		avail = rm.queryCarsPrice(xid, "SFO");
		System.out.println(avail);
		avail = rm.queryCustomerBill(xid, "John");
		System.out.println(avail);
	}


	public static void sbill(ResourceManagerImpl rm) throws RemoteException, TransactionAbortedException, InvalidTransactionException, IOException{
		int xid = rm.start();
		boolean returnVal = rm.addFlight(xid, "347", 100, 310);
		returnVal = rm.addRooms(xid, "Stanford", 200, 150);
		returnVal = rm.addCars(xid,"SFO",300,30);
		returnVal = rm.newCustomer(xid,"John");
		rm.commit(xid);
		rm.printTables();

		xid = rm.start();
		returnVal = rm.reserveFlight(xid,"John","347");
		returnVal = rm.reserveRoom(xid,"John","Stanford");
		returnVal = rm.reserveCar(xid,"John","SFO");
		//returnVal = rm.commit(xid);
		rm.commit(xid);
		rm.printTables();

		xid = rm.start();
		int bill = rm.queryCustomerBill(xid,"John");
		System.out.println("Bill for john " + bill);
		rm.commit(xid);
	}
}