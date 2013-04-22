package it.uninsubria.dista.PathFinderService;

import it.uninsubria.dista.PathFinderService.Polynomials.Polynomial;

import java.math.BigInteger;

public class UserData {

	public final static int MAX_DEPTH = 4;

	private BigInteger userId;
	private Polynomial[] polynomials = new Polynomial[MAX_DEPTH];
	
	public UserData(BigInteger userId, Polynomial directContacts) {
		this.userId = userId;
		this.polynomials[0] = new Polynomial(directContacts);

		for (int i=1; i<MAX_DEPTH; i++) 
			this.polynomials[i] = new Polynomial(new BigInteger("1"));

	}
	
	public BigInteger getUserId() {
		return this.userId;
	}
	
	public Polynomial getPolynomial(int level) {
		return this.polynomials[level];
	}
	
	public Polynomial[] getPolynomials() {
		return this.polynomials;
	}
}
