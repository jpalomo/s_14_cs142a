///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package rm.tests;
//
//import java.io.File;
//import java.rmi.RemoteException;
//import transaction.InvalidTransactionException;
//import transaction.ResourceManagerImpl;
//import transaction.TransactionAbortedException;
//
///**
// *
// * @author Palomo
// */
//public class PopulateData {
//
//		ResourceManagerImpl rm;
//		int numToCreate = 10;
//
//		public static void main(String args[]) throws RemoteException, TransactionAbortedException, InvalidTransactionException, InterruptedException {
//				//PopulateData pd = new PopulateData();
//				//pd.flights();
//				//pd.cars();
//
//				String currentDir = System.getProperty("user.dir");
//				//System.out.println(System.getProperty("user.dir"));
//				StringBuilder sb = new StringBuilder(System.getProperty("user.dir"));
//				sb.append(File.separator + "project");
//				sb.append(File.separator + "test.part1");
//				sb.append(File.separator + "data");
//				System.out.println(sb.toString());
//
//				File dataDir = new File(sb.toString());
//
//				if (!dataDir.exists()) {
//					boolean createDataDir = dataDir.mkdir();
//
//					if(!createDataDir){
//						throw new RemoteException("Could not initialize resource manager.");
//					}
//				}
//
//				System.out.println("data dir doesnt exist");
//				System.out.println(System.getProperty("user.dir"));
//
//		}
//
//		public void flights() throws RemoteException, InvalidTransactionException, TransactionAbortedException {
//				writeFlights();
//				//getRM().commit(10);
//				deleteFlights();
//				readFlights();
//		}
//
//		public void cars() throws RemoteException, InvalidTransactionException, TransactionAbortedException, InterruptedException {
//				writeCars();
//				readCars();
//				deleteCars();
//		}
//
//		public PopulateData() throws RemoteException {
//				rm = new ResourceManagerImpl();
//		}
//
//		public void writeFlights() throws RemoteException, TransactionAbortedException, InvalidTransactionException {
//				for (int i = 0; i < numToCreate; i++) {
//						String flightNum = String.valueOf(i);
//						rm.addFlight(10, flightNum, i + 50, 100 * i);
//				}
//		}
//
//		public void readFlights() throws RemoteException, TransactionAbortedException, InvalidTransactionException {
//				System.out.print("\n\nReading flights....\n");
//				for (int i = 0; i < numToCreate; i++) {
//						String flightNum = String.valueOf(i);
//						rm.queryFlight(i, flightNum);
//						System.out.printf("%s\t%d\n", "Flight_" + flightNum + " price: ", rm.queryFlight(i, flightNum));
//				}
//		}
//
//		public void deleteFlights() throws RemoteException, TransactionAbortedException, InvalidTransactionException {
//				System.out.print("\n\nDeleting flights....\n");
//				for (int i = 0; i < numToCreate; i++) {
//						String flightNum = String.valueOf(i);
//						if (!rm.deleteFlight(i, flightNum)) {
//								System.out.print("\n### Could not delete flight: " + flightNum + "###\n");
//						}
//				}
//		}
//
//		public void writeCars() throws RemoteException, TransactionAbortedException, InvalidTransactionException {
//				char loc = 'a';
//				for (int i = 0; i < numToCreate; i++) {
//						String location = String.valueOf(loc);
//						rm.addCars(10, location, i + 50, 100 * i);
//						loc++;
//				}
//		}
//
//		public void readCars() throws RemoteException, TransactionAbortedException, InvalidTransactionException {
//				System.out.print("\n\nReading cars....\n");
//				char loc = 'a';
//				for (int i = 0; i < numToCreate; i++) {
//						String location = String.valueOf(loc);
//						rm.queryCars(i, location);
//						System.out.printf("%s\t%d\n", "Location_" + location + " numAvail: ", rm.queryCars(i, location));
//						loc++;
//				}
//		}
//
//		public void deleteCars() throws RemoteException, TransactionAbortedException, InvalidTransactionException, InterruptedException {
//				System.out.print("\n\nDeleting cars....\n");
//				char loc = 'a';
//				for (int i = 0; i < numToCreate; i++) {
//						String location = String.valueOf(loc);
//						rm.deleteCars(i, location, 1);
//						loc++;
//				}
//		}
//
//		public ResourceManagerImpl getRM() {
//				return rm;
//
//		}
//}
