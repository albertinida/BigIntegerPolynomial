package it.uninsubria.dista.PathFinderService;

import it.uninsubria.dista.PathFinderService.Exceptions.MalformedPolynomialException;
import it.uninsubria.dista.PathFinderService.Polynomials.Polynomial;

import java.math.BigInteger;
import java.util.ArrayList;
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
	
	public void addUserData(UserData ud) throws InterruptedException, ExecutionException {
		
/**/	System.out.println();
/**/	System.out.println("Inserimento dell'utente "+ud.getUserId()+". Inserimento numero "+table.size());
		
		try {
			for (int i=1; i<UserData.MAX_DEPTH; i++)
				if ((ud.getPolynomial(i).length() != 1) && !(ud.getPolynomial(i).getCoefficient(0).equals(BigInteger.ONE))) 
					throw new MalformedPolynomialException();
		} catch (MalformedPolynomialException exc) {
			exc.printStackTrace();
			System.out.println("Malformed Polynomial received");
			return;
		}
		
/**/	long insertTime = System.currentTimeMillis();
		boolean[] nextLevelUpdated = new boolean[table.size()+1];
/**/	int eval = 0, conv = 0;		
		for (int i=0; i<table.size(); i++) {
			eval ++;
			if (table.get(i).getPolynomial(0).evaluate(ud.getUserId()).equals(BigInteger.ZERO)) {
				conv++;
				table.get(i).getPolynomial(1).threadedConvolution(ud.getPolynomial(0), executor);
				ud.getPolynomial(1).threadedConvolution(table.get(i).getPolynomial(0), executor);
				nextLevelUpdated[i] = true;
			}
		}
/**/	System.out.println("aggiornamento del livello 1 :"+(System.currentTimeMillis()-insertTime)+"ms");
/**/	System.out.println("\t"+table.size()+" cicli");
/**/	System.out.println("\t"+eval+" valutazioni");
/**/	System.out.println("\t"+conv*2+" convoluzioni");		table.add(ud);
		
		int maxCascade = Math.min(UserData.MAX_DEPTH, table.size());
		for (int level=1; level<maxCascade-1; level++) {
/**/		insertTime = System.currentTimeMillis();
			boolean[] thisLevelUpdated = nextLevelUpdated.clone();
/**/		eval = 0; conv = 0;
			for (int i=0; i<table.size()-1; i++) {
				if (thisLevelUpdated[i] == true) {
					for (int j=i+1; j<table.size(); j++) {
						
						UserData user1 = table.get(i);
						UserData user2 = table.get(j);
/**/					eval++;
						if (user1.getPolynomial(level).evaluate(user2.getUserId()).equals(BigInteger.ZERO)) {
/**/						conv++;
							user1.getPolynomial(level+1).threadedConvolution(user2.getPolynomial(level), executor);
							user2.getPolynomial(level+1).threadedConvolution(user1.getPolynomial(level), executor);
							
							nextLevelUpdated[i] = true;
							nextLevelUpdated[j] = true;
						} 
					}
				}
			}
/**/		System.out.println("aggiornamento del livello "+(level+1)+": "+(System.currentTimeMillis()-insertTime)+"ms");
/**/		System.out.println("\t"+Math.pow(table.size(), level)+" cicli");
/**/		System.out.println("\t"+eval+" valutazioni");
/**/		System.out.println("\t"+conv*2+" convoluzioni");		
		}
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
