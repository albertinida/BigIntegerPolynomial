package it.uninsubria.dista.PathFinderService;

import it.uninsubria.dista.PathFinderService.Polynomials.Polynomial;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.Driver;

public class UserData {

	public final static int MAX_DEPTH = 7;

	private BigInteger userId;
	
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
					testPFS.output.write("Occurred Exception "+e.getClass()+"\n");
					Thread.sleep(1000);
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
					testPFS.output.write("Occurred Exception "+e.getClass()+"\n");
					Thread.sleep(1000);
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
					testPFS.output.write("Occurred Exception "+e.getClass()+"\n");
					Thread.sleep(1000);
				} catch (Exception sleep) {
				}
			}
		}
	}
}
