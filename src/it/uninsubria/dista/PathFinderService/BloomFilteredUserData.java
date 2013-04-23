package it.uninsubria.dista.PathFinderService;

import it.uninsubria.dista.BloomFilters.BloomFilter;
import it.uninsubria.dista.PathFinderService.Polynomials.BloomFilteredPolynomial;
import it.uninsubria.dista.PathFinderService.Polynomials.Polynomial;

import java.math.BigInteger;

public class BloomFilteredUserData {

	public final static int MAX_DEPTH = 4;

	private BigInteger userId;
	private BloomFilteredPolynomial[] polynomials = new BloomFilteredPolynomial[MAX_DEPTH];
	
	public BloomFilteredUserData(BigInteger userId, BloomFilteredPolynomial directContacts) {
		this.userId = userId;
		this.polynomials[0] = new BloomFilteredPolynomial(directContacts);
		
		for (int i=1; i<MAX_DEPTH; i++) {
			this.polynomials[i] = new BloomFilteredPolynomial(new BigInteger("1"));
		}
	}
	
	public BigInteger getUserId() {
		return this.userId;
	}
	
	public BloomFilteredPolynomial getPolynomial(int level) {
		return this.polynomials[level];
	}
	
	public BloomFilteredPolynomial[] getPolynomials() {
		return this.polynomials;
	}
}
