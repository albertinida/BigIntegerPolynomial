package it.uninsubria.dista.PathFinderService;

import it.uninsubria.dista.PathFinderService.Polynomials.Polynomial;
import it.uninsubria.dista.PathFinderService.Test.Build;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.Driver;

/**
 * Rappresenta un utente della DSN, attraverso il suo uid e la sua vista locale del grafo anonimizzata
 */
public class UserData {

	/**
	 * Parametro che indica la massima profondità delle viste locali
	 */
	public final static int MAX_DEPTH = 7;

	/**
	 * uid dell'utente singolo
	 */
	private BigInteger userId;
	
	public UserData(BigInteger userId) {
		this.userId = userId;
	}
	
	/**
	 * Costrutture di istanza; attraverso l'uid e la vista anonimizzata dei contatti diretti salva le 
	 * informazioni su DB ed inizializza a 1 le viste più profonde dei polinomi.
	 * 
	 * @param userId
	 * @param directContacts
	 */
	public UserData(BigInteger userId, Polynomial directContacts) {
		
		boolean stored = false;
		while (!stored) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/PFS","root","root");
				Statement state = connection.createStatement();
				
				String insertion = "INSERT INTO polynomials SET uid="+userId;
				insertion += ", polyLv1='"+(new Polynomial(directContacts).toString())+"'";
				for (int i=2; i<=MAX_DEPTH; i++) {
					insertion += ", polyLv"+i+"='"+(new Polynomial(new BigInteger("1"))).toString()+"'";
				}
				insertion += ";";
	
				state.executeUpdate(insertion);
				
				this.userId = userId;
				
				connection.close();
				stored = true;
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
	
	public BigInteger getUserId() {
		return this.userId;
	}
	
	public Polynomial getPolynomial(int level) {
		while (true) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/PFS","root","root");
				Statement state = connection.createStatement();
				
				String select = "SELECT polyLv"+level+" FROM polynomials WHERE uid="+this.userId;
				
				ResultSet rs = state.executeQuery(select);
				
				String poly = "";
				while(rs.next()) {
					poly = (String)rs.getObject("polyLv"+level);
				}
				
				connection.close();
				return new Polynomial(poly);
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
	
	public void setPolynomial(int level, Polynomial polynomial) {
		while (true) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/PFS","root","root");
				Statement state = connection.createStatement();
	
				String update = "UPDATE polynomials SET polyLv"+level+"='"+polynomial.toString()+"'";
				update += " WHERE uid="+this.userId;
		
				state.executeUpdate(update);
				
				connection.close();
				return;
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
	
	public boolean exists() {
		while (true) {
			try {
				
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/PFS","root","root");
				Statement state = connection.createStatement();

				ResultSet rs = state.executeQuery("SELECT COUNT(uid) FROM polynomials WHERE uid="+this.userId);
				
				while (rs.next()) {
					int result = rs.getInt("count(uid)");
					if (result == 0) 
						return false;
					else
						return true;
				}
				
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
}
