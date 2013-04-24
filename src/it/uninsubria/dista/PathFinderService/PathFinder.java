package it.uninsubria.dista.PathFinderService;

import it.uninsubria.dista.PathFinderService.Exceptions.MalformedPolynomialException;
import it.uninsubria.dista.PathFinderService.Polynomials.Polynomial;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
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
	
	public ExecutorService getExecutor() {
		return this.executor;
	}
	
	public void addUserData(UserData ud) throws InterruptedException, ExecutionException, IOException {
		
//	System.out.println();
//	System.out.println("Inserimento dell'utente "+ud.getUserId()+". Inserimento numero "+table.size());
			
//		testPFS.output.write("Inserimento dell'utente "+ud.getUserId()+". Inserimento numero "+table.size()+"\n");
//		testPFS.output.flush();
		
		try {
			for (int i=2; i<=UserData.MAX_DEPTH; i++)
				if ((ud.getPolynomial(i).length() != 1) && !(ud.getPolynomial(i).getCoefficient(0).equals(BigInteger.ONE))) 
					throw new MalformedPolynomialException();	
		} catch (MalformedPolynomialException exc) {
			exc.printStackTrace();
			System.out.println("Malformed Polynomial received");
			System.exit(1);
		}
		
		long insertTime = System.currentTimeMillis();
		long totalTime = System.currentTimeMillis();
		boolean[] nextLevelUpdated = new boolean[table.size()+1];
		Arrays.fill(nextLevelUpdated, false);

		/**
		 * evaluates every already saved line with the new received UserData.
		 * if the evaluation is successful, the UserData.level2 is updated, and the 
		 * same is done with level2 of curren table line
		 */
		int eval = 0, conv = 0, cycle = 0;		
		for (int i=0; i<table.size(); i++) {
			eval ++;
			if (table.get(i).getPolynomial(1).evaluate(ud.getUserId()).equals(BigInteger.ZERO)) {
				conv+=2;
				
				table.get(i).setPolynomial(2, table.get(i).getPolynomial(2).threadedConvolution(ud.getPolynomial(1), executor));
				ud.setPolynomial(2, ud.getPolynomial(2).threadedConvolution(table.get(i).getPolynomial(1), executor));
				//table.get(i).getPolynomial(2).threadedConvolution(ud.getPolynomial(1), executor);
				//ud.getPolynomial(2).threadedConvolution(table.get(i).getPolynomial(1), executor);
				nextLevelUpdated[i] = true;
			}
		}
//	System.out.print("aggiornamento del livello 1 :"+(System.currentTimeMillis()-insertTime)+"ms");
//	System.out.println("\tcicli: "+table.size()+", valutazioni: "+eval+" moltiplicazioni: "+conv);

//		testPFS.output.write("aggiornamento del livello 1: "+(System.currentTimeMillis()-insertTime)+"ms");
//		testPFS.output.write("\tvalutazioni: "+eval+" moltiplicazioni: "+conv+"\n");
//		testPFS.output.flush();

		table.add(ud);
		
		int maxCascade = Math.min(UserData.MAX_DEPTH, table.size());
		for (int level=2; level<=maxCascade; level++) {
			insertTime = System.currentTimeMillis();
			boolean[] thisLevelUpdated = nextLevelUpdated.clone();
			Arrays.fill(nextLevelUpdated, false);
			eval = 0; conv = 0;
			/**
			 * for every line in the table, i evaluate with the following lines' userId,
			 * only if the current line has been updated
			 */
			for (int i=0; i<table.size()-1; i++) {
				if (thisLevelUpdated[i] == true) {
					for (int j=i+1; j<table.size(); j++) {
						UserData user1 = table.get(i);
						UserData user2 = table.get(j);
						eval++;
						if (user1.getPolynomial(level).evaluate(user2.getUserId()).equals(BigInteger.ZERO)) {
							conv+=2;
							user1.getPolynomial(level+1).threadedConvolution(user2.getPolynomial(level), executor);
							user2.getPolynomial(level+1).threadedConvolution(user1.getPolynomial(level), executor);
							
							nextLevelUpdated[i] = true;
							nextLevelUpdated[j] = true;
						} 
					}
				} 
			}
//		System.out.print("aggiornamento del livello "+level+": "+(System.currentTimeMillis()-insertTime)+"ms");
//		System.out.println("\tvalutazioni: "+eval+" moltiplicazioni: "+conv);

//			testPFS.output.write("aggiornamento del livello "+level+": "+(System.currentTimeMillis()-insertTime)+"ms");
//			testPFS.output.write("\tvalutazioni: "+eval+" moltiplicazioni: "+conv+"\n");
//			testPFS.output.flush();
		}
//	System.out.println("\ttempo richiesto dall'aggiornamento: "+(System.currentTimeMillis()-totalTime)+"ms\n");
//		testPFS.output.write("\ttempo richiesto dall'aggiornamento: "+(System.currentTimeMillis()-totalTime)+"ms\n");
//		testPFS.output.flush();
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
