package it.uninsubria.dista.PathFinderService;
import it.uninsubria.dista.PathFinderService.Polynomials.Polynomial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.LinkedList;


public class testPFS {

	public static BufferedWriter output;
	
	public static void main(String[] args) {

		try {

			output = new BufferedWriter(new FileWriter(new File(args[0]+".result")), 32768);
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/PFS","root","root");
			Statement state = connection.createStatement();

			state.executeUpdate("DROP TABLE IF EXISTS polynomials");
			String create = "CREATE TABLE IF NOT EXISTS polynomials (uid VARCHAR(10000) NOT NULL";
			for (int i=1; i<=UserData.MAX_DEPTH; i++) {
				create += ", polyLv"+i+" LONGTEXT";
			}
			create += ");";
			
			state.executeUpdate(create);
			connection.close();
			
			PathFinder PFS = PathFinder.getInstance();

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
					PFS.addUserData(new UserData(userId, directs));
				}
			}
			
			System.out.println("\n\nTempo totale per la lettura del dataset :"+(System.currentTimeMillis()-startExecTest));
			output.write("\n\nTempo totale per la lettura del dataset :"+(System.currentTimeMillis()-startExecTest));
			output.flush();
			output.close();
//			System.out.print(PFS);
			System.exit(0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
}
