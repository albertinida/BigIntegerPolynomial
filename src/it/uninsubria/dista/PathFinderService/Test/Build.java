package it.uninsubria.dista.PathFinderService.Test;
import it.uninsubria.dista.PathFinderService.PathFinder;
import it.uninsubria.dista.PathFinderService.UserData;
import it.uninsubria.dista.PathFinderService.Polynomials.Polynomial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;


public class Build {

	public static BufferedWriter output;
	
	public static void main(String[] args) {

		PathFinder PFS = null;
		try {

			output = new BufferedWriter(new FileWriter(new File(args[0]+".result")), 32768);
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/PFS","root","root");
			Statement state = connection.createStatement();

			ResultSet rs = state.executeQuery("show tables;");
			boolean empty = true;
			while (rs.next()) {
				if (rs.getString("Tables_in_PFS").equals("polynomials")) {
					empty = false;
				}
			}
			if (empty) {

				String create = "CREATE TABLE IF NOT EXISTS polynomials (row_id BIGINT NOT NULL AUTO_INCREMENT UNIQUE KEY, uid VARCHAR(10000)";
				for (int i=1; i<=UserData.MAX_DEPTH; i++) {
					create += ", polyLv"+i+" LONGTEXT";
				}
				create += ");";
				
				state.executeUpdate(create);
				connection.close();
				
				PFS = PathFinder.getInstance();
	
				LinkedList<BigInteger> coeff = new LinkedList<BigInteger>();
				coeff.add(new BigInteger("1"));
				coeff.add(new BigInteger("1"));
				
				BufferedReader br = new BufferedReader(new FileReader(args[0]));
				String line;
				
				long startExecTest = System.currentTimeMillis();
				
				while ((line = br.readLine()) != null) {
	
					if (line.indexOf('#') > -1) {
						String[] data = line.split("@");
	
						BigInteger userId = new BigInteger(data[0]);
						Polynomial directs = new Polynomial(new BigInteger("1"));
	
						String[] contactIds = data[1].split("#");
						for (String contactId : contactIds) {
							coeff.set(1, new BigInteger("-"+contactId));
							Polynomial poly = new Polynomial(coeff);
							directs.threadedConvolution(poly, PFS.getExecutor());
						}
						PFS.addUserData(new UserData(userId, directs), true);
					}
				}
				
				System.out.println("\n\nTempo totale per la lettura del dataset: "+(System.currentTimeMillis()-startExecTest)+"ms");
				output.write("\n\nTempo totale per la lettura del dataset: "+(System.currentTimeMillis()-startExecTest)+"ms");
				output.flush();
				output.close();
			} else {
				
				PFS = PFS.restore();
			}

			
			System.out.println("Path Finder Service Instantiated");
			
			System.out.println("Test interattivo per il PFS memorizzato nella tabella PFS.polynomials");
			System.out.println("Valutazione -> : 'eval:<requestorId>,<ownerId>,<depth>'");
			System.out.println("Inserimento -> : 'ins:<userId>@[<contactId>#]'");
			System.out.println("Uscita      -> : 'exit'");
			System.out.println();
			System.out.print("PFS# ");
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line;
			
			while ((line = br.readLine()) != null) {
				line = line.replace(" ", "");

				if (line.matches(".*eval.*")) {
					String[] command = (line.split(":")[1]).split(",");
					if (command.length != 3) {
						System.out.print("Comando errato");
						continue;
					} else {
						System.out.println("Valutazione della regola "+command[2]+ " sul path "+command[1]+"->"+command[0]);
						
						UserData owner = new UserData(new BigInteger(command[1]));
						long start = System.currentTimeMillis();
						BigInteger evaluation = owner.getPolynomial(Integer.parseInt(command[2])).evaluate(new BigInteger(command[0]));
						if (evaluation.equals(BigInteger.ZERO)) {
							System.out.print("Valutazione positiva. ");
						} else {
							System.out.print("Valutazione negativa. ");
						}
						
						System.out.println("Tempo richiesto: "+(System.currentTimeMillis()-start)+"ms");
					}
				} else if (line.matches(".*ins.*")) {
					String[] data = (line.split(":")[1]).split("@");
					if (!(new UserData(new BigInteger(data[0]))).exists()) {
						
					} else {
						System.out.println("Utente già presente nel database");
					}
				} else if (line.matches(".*exit.*")) {
					System.out.println("Program exit");
					System.exit(0);
				} 
				
				System.out.print("PFS# ");
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
