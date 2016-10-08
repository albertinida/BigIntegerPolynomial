package it.uninsubria.dista.PathFinderService.Test;
import it.uninsubria.dista.PathFinderService.PathFinderService;
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
import java.sql.Statement;
import java.util.LinkedList;


public class Evaluation {

	public static BufferedWriter output;
	
	public static void main(String[] args) {

		try {
			
			System.out.println("Test di valutazione per il PFS memorizzato nella tabella polynomials");
			System.out.println("Inserisci la valutazione, in una riga, nel formato 'eval:<requestorId>,<ownerId>,<depth>'");
			System.out.println();
			System.out.println("Inserisci 'exit' per uscire");
			System.out.print("PFS# ");
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line;
			
			while ((line = br.readLine()) != null) {

				if (line.matches(".*eval.*")) {
					line = line.replace(" ", "");
					String[] command = (line.split(":")[1]).split(",");
					if (command.length != 3) {
						System.err.print("Comando errato");
						continue;
					} else {
						System.out.println("Valutazione della regola "+command[2]+ " sul path "+command[1]+"->"+command[0]);
						
						UserData owner = new UserData(new BigInteger(command[1]));
						long start = System.currentTimeMillis();
						BigInteger evaluation = owner.getPolynomial(Integer.parseInt(command[2])).evaluate(new BigInteger(command[0]));
						if (evaluation.equals(BigInteger.ZERO)) {
							System.out.print("Valutazione positiva. ");
						} else {
							System.err.print("Valutazione negativa. ");
						}
						
						System.out.println("Tempo richiesto: "+(System.currentTimeMillis()-start)+"ms");
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
