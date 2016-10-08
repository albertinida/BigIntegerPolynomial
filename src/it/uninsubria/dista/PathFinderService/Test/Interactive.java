package it.uninsubria.dista.PathFinderService.Test;
import it.uninsubria.dista.PathFinderService.UserData;
import it.uninsubria.dista.PathFinderService.Polynomials.Polynomial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* 
 * Questo package si chiama 'Test' non per caso .. 
 * 
 * Ci sono svariate possibili migliorie sia in questo package che negli altri package della struttura.
 * Iniziate a pensare di inserire, sempre se volete continuare lo sviluppo da qui, una classe Config dove andate
 * ad inserire valori e metodi utili a tutto il package (e.g. Config.Init() che carica i valori e i driver, 
 * Config.getDb() che restituisce il canale di comunicazione per il DB, etc..);
 * 
 * Valutate, minuziosamente e approfonditamente su carta, la possibilità non solo di propagare le viste locali del grafo,
 * ma anche di poterle aggiornare !
 * Quando A e B stringono amicizia si scambiano le liste dei contatti; tutti i contatti comuni pre-esistenti, però, vengono
 * riportati più e più volte nei due polinomi, rendendo più lunga e complicata la fase di moltiplicazione.
 * Una delle valutazioni che, a suo tempo, erano state fatte, riguardava la possibilità di sfruttare la divisione tra polinomi
 * per trovare le soluzioni comuni tra due polinomi (e.g. PL1_A / PL1_B -> PL1§_A, dove PL1§_A rappresenta i contatti di A
 * ancora non presenti tra i contatti di B (e quindi poi propago PL1§_A e non PL1_A), e viceversa.
 */


public class Interactive {

	public static BufferedWriter output;
	private final static ExecutorService executor = Executors.newCachedThreadPool();
	
	// TODO: Lanciare e gestire delle eccezioni, e.g., utente già creato, errore nella creazione, etc..
	private static void userInsertion(BigInteger uid) {
		
		new UserData(uid, new Polynomial(new BigInteger("1")));
	}
	
	// TODO: Lanciare e gestire delle eccezioni, e.g., utenti non presenti, errore nella creazione, etc..
	// NOTE: A questo punto si può già stabilire se una relazione è già presente o meno tra due utenti, 
	//	per evitare che venga creata più volte 
	private static void friendshipCreation(BigInteger firstUid, BigInteger secondUid) {

		LinkedList<BigInteger> friendshipPolynomial = new LinkedList<BigInteger>();
		Polynomial tmpPoly;
		LinkedList<Polynomial> firstPropagation,secondPropagation;
		
		try {
			
			friendshipPolynomial.add(new BigInteger("1"));
			friendshipPolynomial.add(new BigInteger("1"));
	
			UserData first = new UserData(firstUid);
			UserData second = new UserData(secondUid);
	
			// Aggiungo il contatto 'second' a PL1_first
			friendshipPolynomial.set(1, new BigInteger("-"+secondUid));
			tmpPoly = new Polynomial(friendshipPolynomial);
			tmpPoly = first.getPolynomial(1).threadedConvolution(tmpPoly, executor);
			first.setPolynomial(1, tmpPoly);

			// Aggiungo il contatto 'first' a PL1_second
			friendshipPolynomial.set(1, new BigInteger("-"+firstUid));
			tmpPoly = new Polynomial(friendshipPolynomial);
			tmpPoly = second.getPolynomial(1).threadedConvolution(tmpPoly, executor);
			second.setPolynomial(1, tmpPoly);

			// Calcolo delle propagazioni
			firstPropagation = new LinkedList<Polynomial>();
			secondPropagation = new LinkedList<Polynomial>();
			for (int i=2; i<UserData.MAX_DEPTH; i++) {

				firstPropagation.add( first.getPolynomial(i).threadedConvolution(second.getPolynomial(i-1), executor) );
				secondPropagation.add( second.getPolynomial(i).threadedConvolution(first.getPolynomial(i-1), executor) );
			}
			
			// Propagazioni
			for (int i=2; i<UserData.MAX_DEPTH; i++) {
				first.setPolynomial(i, firstPropagation.get(i-2));
				second.setPolynomial(i, secondPropagation.get(i-2));
			}
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	// NOTE: Fatta una piccola modifica, adesso prova entrambe le direzioni.
	private static boolean friendshipEvaluation(BigInteger requestorUid, BigInteger ownerUid, Integer depth) {

		BigInteger eval;
		UserData requestor = new UserData(requestorUid);
		UserData owner = new UserData(ownerUid);
		
		eval = owner.getPolynomial(depth).evaluate(requestorUid);
		if (eval.equals(BigInteger.ZERO)) {
			
			return true;
		}
		
		eval = requestor.getPolynomial(depth).evaluate(ownerUid);
		if (eval.equals(BigInteger.ZERO)) {
			
			return true;
		}
		
		return false;
	}
	
	public static void main(String[] args) {

		String query;
		
		try {

			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/PFS","root","root");
			Statement state = connection.createStatement();

			// Inserisco un drop non condizionato in modo da poter variare il valore di UserData.MAX_DEPTH tra un run e l'altro
			query = "DROP TABLE IF EXISTS polynomials"; 
			state.executeUpdate(query);
			
			query = "CREATE TABLE IF NOT EXISTS polynomials (row_id BIGINT NOT NULL AUTO_INCREMENT UNIQUE KEY, uid VARCHAR(10000)";
			for (int i=1; i<=UserData.MAX_DEPTH; i++) {
				query += ", polyLv"+i+" LONGTEXT";
			}
			query += ");";
			state.executeUpdate(query);
			connection.close();
			
			System.out.println("Path Finder Service Instantiated");
			
			System.out.println("Test interattivo per il PFS memorizzato nella tabella PFS.polynomials");
			System.out.println("Creazione utente   -> : 'crte:<userId>'");
			System.out.println("Istanziazione rel. -> : 'frnd:<userId>,<userId>'");
			System.out.println("Valutazione relaz. -> : 'eval:<requestorId>,<ownerId>,<depth>'");
			System.out.println("Uscita      	   -> : 'exit'");
			System.out.println();
			System.out.print("PFS# ");

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line;
			
			while ((line = br.readLine()) != null) {
				System.out.print("PFS# ");
				line = line.replaceAll("\\s+", "");

				
				if (line.matches(".*crte.*")) {
					
					String uid = line.replaceAll(".*:", "");
					userInsertion(new BigInteger(uid));
					
					continue;
				}
				if (line.matches(".*frnd.*")) {
					String[] uids = line.replaceAll(".*:", "").split(",");
					friendshipCreation(new BigInteger(uids[0]), new BigInteger(uids[1]));

					continue;
				}
				if (line.matches(".*eval.*")) {

					String[] uids = line.replaceAll(".*:", "").split(",");
					boolean result = friendshipEvaluation(new BigInteger(uids[0]), new BigInteger(uids[1]), Integer.parseInt(uids[2]));

					System.out.println(result);
					System.out.print("PFS# ");
					
					continue;
				}
				if (line.matches(".*exit.*")) {

					System.out.println("Program exit");
					System.exit(0);
				} 
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
