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

public class PathFinder {

	private final ExecutorService executor = Executors.newFixedThreadPool(10);
	
	private ArrayList<UserData> table = new ArrayList<UserData>();
	
	private static PathFinder singleton = null;
	
	public static PathFinder getInstance() {
		if (singleton == null) {
			singleton = new PathFinder();
		}
		return singleton;
	}
	
	public static PathFinder restore() {
		
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
	
	public ExecutorService getExecutor() {
		return this.executor;
	}
	
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
		
		/*
		long insertTime = System.currentTimeMillis();
		long totalTime = System.currentTimeMillis();
		boolean[][] updates = new boolean[UserData.MAX_DEPTH][table.size()+1];
		for (int i=0; i<UserData.MAX_DEPTH; i++) {
			Arrays.fill(updates[i], false);
		}
		
		// evaluates every already saved line with the new received UserData.
		// if the evaluation is successful, the UserData.level2 is updated, and the 
		// same is done with level2 of curren table line
		
		
		int eval = 0, conv = 0, cycle = 0;		
		for (int i=0; i<table.size(); i++) {
			eval ++;
			if (table.get(i).getPolynomial(1).evaluate(ud.getUserId()).equals(BigInteger.ZERO)) {
				conv+=2;
				
				table.get(i).setPolynomial(2, table.get(i).getPolynomial(2).threadedConvolution(ud.getPolynomial(1), executor));
				ud.setPolynomial(2, ud.getPolynomial(2).threadedConvolution(table.get(i).getPolynomial(1), executor));
				updates[1][i] = true;
			}
		}

		Build.output.write("aggiornamento del livello 1: "+(System.currentTimeMillis()-insertTime)+"ms\tvalutazioni: "+eval+" moltiplicazioni: "+conv+"\n");
		
		table.add(ud);
		
		
		int maxCascade = Math.min(UserData.MAX_DEPTH, table.size());
		for (int level=2; level<=maxCascade; level++) {
			insertTime = System.currentTimeMillis();
			eval = 0; conv = 0;
			// for every line in the table, i evaluate with the following lines' userId, only if the current line has been updated
			for (int i=0; i<table.size()-1; i++) {
				if (updates[level-1][i] == true) {
					for (int j=i+1; j<table.size(); j++) {
						if (i!=j) {
							UserData user1 = table.get(i);
							UserData user2 = table.get(j);
							eval++;
							if (user1.getPolynomial(level).evaluate(user2.getUserId()).equals(BigInteger.ZERO)) {
								conv+=2;
								user1.setPolynomial(level+1, user1.getPolynomial(level+1).threadedConvolution(user2.getPolynomial(level), executor));
								user2.setPolynomial(level+1, user2.getPolynomial(level+1).threadedConvolution(user1.getPolynomial(level), executor));
								
								updates[level][i] = true;
								updates[level][j] = true;
							}
						}
					}
				} 
			}
			Build.output.write("aggiornamento del livello "+level+": "+(System.currentTimeMillis()-insertTime)+"ms\tvalutazioni: "+eval+" moltiplicazioni: "+conv+"\n");
		}
		Build.output.write("\ttempo richiesto dall'aggiornamento: "+(System.currentTimeMillis()-totalTime)+"ms\n");
		Build.output.flush();
		*/
		
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
