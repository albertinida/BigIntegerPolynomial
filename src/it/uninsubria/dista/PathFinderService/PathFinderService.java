package it.uninsubria.dista.PathFinderService;

import it.uninsubria.dista.PathFinderService.Exceptions.MalformedPolynomialException;
import it.uninsubria.dista.PathFinderService.Polynomials.Polynomial;
import it.uninsubria.dista.PathFinderService.Test.Build;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementa il path finder service, come specificato e descritto in http://dl.acm.org/citation.cfm?id=2557574
 */
public class PathFinderService {

	/**
	 * Gestore del thread pool per il calcolo della convoluzione con tecnica multi-thread
	 */
	private final ExecutorService executor = Executors.newFixedThreadPool(10);
	
	/**
	 * La tabella in cui viene memorizzato localmente un utente
	 */
	private ArrayList<UserData> table = new ArrayList<UserData>();
	
	/**
	 * Pattern singleton. Una sola istanza di PathFinderService
	 */
	private static PathFinderService singleton = null;
	
	/**
	 * @return Restituisce l'istanza caricata a runtime
	 */
	public static PathFinderService getInstance() {
		if (singleton == null) {
			singleton = new PathFinderService();
		}
		return singleton;
	}
	
	/**
	 * @return Ricostruisce un PFS precedentemente salvato su DB
	 */
	public static PathFinderService restore() {
		
		singleton = getInstance();
		singleton.table = new ArrayList<UserData>();
		
		while (true) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/PFS","root","root");
				Statement state = connection.createStatement();

				ResultSet rs = state.executeQuery("SELECT uid FROM polynomials");
				
				while (rs.next()) {
					singleton.table.add(new UserData(new BigInteger(rs.getString("uid"))));
				}
				
				return singleton;
	
			} catch (Exception e) {
				try {
					System.out.println("Occurred Exception "+e.getClass());
					Build.output.write("Occurred Exception "+e.getClass()+"\n");
					Thread.sleep(500);
				} catch (Exception sleep) {
				}
			}
		}
	}
	
	/**
	 * @return Restituisce l'executor di gestione dei thread
	 */
	public ExecutorService getExecutor() {
		return this.executor;
	}
	
	/**
	 * Aggiunge un nuovo UserData alla tabella rappresentativa del grafo sociale.
	 * Conseguentemente all'aggiunta, viene effettuata la propagazione dei polinomi.
	 * 
	 * @param ud
	 * @param print
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IOException
	 */
	public void addUserData(UserData ud, boolean print) throws InterruptedException, ExecutionException, IOException {
		
		Build.output.write("Inserimento dell'utente "+ud.getUserId()+". Inserimento numero "+table.size()+"\n");
		
		try {
			for (int i=2; i<=UserData.MAX_DEPTH; i++)
				if ((ud.getPolynomial(i).length() != 1) && !(ud.getPolynomial(i).getCoefficient(0).equals(BigInteger.ONE))) 
					throw new MalformedPolynomialException();	
		} catch (MalformedPolynomialException exc) {
			exc.printStackTrace();
			System.out.println("Malformed Polynomial received");
			System.exit(1);
		}
		
		long startInsert = System.currentTimeMillis();
		int conv = 0;
		for (int i=0; i<table.size(); i++) {
			UserData userInTable = table.get(i);
			if (userInTable.getPolynomial(1).evaluate(ud.getUserId()).equals(BigInteger.ZERO)) {
				for (int j=1; j<UserData.MAX_DEPTH; j++) {
					conv += 2;
					userInTable.setPolynomial(j+1, userInTable.getPolynomial(j+1).threadedConvolution(ud.getPolynomial(j), executor));
					ud.setPolynomial(j+1, ud.getPolynomial(j).threadedConvolution(userInTable.getPolynomial(j), executor));
				}
			}
		}
		table.add(ud);
		
		Build.output.write("Inserimento n.ro "+table.size()+" dell'utente "+ud.getUserId()+"\n");
		Build.output.write("\tEseguite "+conv+" moltiplicazioni\n");
		Build.output.write("\tTempo richiesto dall'inserimento: "+(System.currentTimeMillis()-startInsert));
		Build.output.flush();
	}
	
	@Override
	public String toString() {
		String output = "";
		
		for (UserData user : table) {
			output += "User: "+user.getUserId().toString()+"\n";
			for (int i=0; i<UserData.MAX_DEPTH; i++) {
				Polynomial level = user.getPolynomial(i);
				output +="Level "+i+": "+level.toString()+"\n";
			}
			
			output += "\n\n";
		}
		
		return output;
	}
}
