package it.uninsubria.dista.BigInteger.Polynomials;

import java.math.*;
import java.util.List;
import java.util.LinkedList;

public class Polynomial {

	private List<BigInteger> coefficients;
	
	public Polynomial() {
		this.coefficients = new LinkedList<BigInteger>();
	}
	
	public Polynomial(List<BigInteger> coefficients) {
		this.coefficients = new LinkedList<BigInteger>(coefficients);
	}
	
	public Polynomial(BigInteger monomial) {
		this.coefficients = new LinkedList<BigInteger>();
		this.coefficients.add(monomial);
	}
	
	public int degree() {
		return coefficients.size()-1;
	}
	
	public BigInteger getCoefficient(int degree) throws NegativeDegreeException, ExcessiveDegreeException {
		if (degree < 0) throw new NegativeDegreeException();
		if (degree > this.degree()) throw new ExcessiveDegreeException();
		return coefficients.get(this.degree()-degree);
	}
	
	public void setCoefficient(BigInteger coefficient, int degree) throws NegativeDegreeException, ExcessiveDegreeException {
		if (degree < 0) throw new NegativeDegreeException();
		if (degree > this.degree()) throw new ExcessiveDegreeException();
		
		coefficients.set(this.degree()-degree, coefficient);
	}
	
	public BigInteger evaluate(BigInteger value) {
		BigInteger evaluation = new BigInteger("0");

		try {
			for (int i=0; i<=this.degree(); i++) {
				BigInteger coefficient = this.getCoefficient(i);
				evaluation = evaluation.add(coefficient.multiply(value.pow(i)));
			}
		} catch (NegativeDegreeException e) {
			// This should never happen
			e.printStackTrace();
		} catch (ExcessiveDegreeException e) {
			// This should never happen
			e.printStackTrace();			
		}
		
		return evaluation;
	}
	
	public Polynomial multiply(Polynomial polynomial) throws NegativeDegreeException, ExcessiveDegreeException {
	
		List<BigInteger> coefficients = new LinkedList<BigInteger>();
		int m = this.degree();
		int n = polynomial.degree();
		for (int i=0; i<=(m+n); i++) 
			coefficients.add(new BigInteger("0"));
		
		Polynomial result = new Polynomial(coefficients);
		
		for (int i=0; i<=m; i++)
			for (int j=0; j<=n; j++) {
				BigInteger oldCoefficient = result.getCoefficient(i+j);
				BigInteger addValue = this.getCoefficient(i).multiply(polynomial.getCoefficient(j));
				result.setCoefficient(oldCoefficient.add(addValue), i+j);
			}
			
		this.coefficients.clear();
		this.coefficients = result.getCoefficients();
		
		return this;
	}

	
	public Polynomial convMultiply(Polynomial polynomial) throws NegativeDegreeException, ExcessiveDegreeException {
		
		List<BigInteger> coefficients = new LinkedList<BigInteger>();
		int m = this.degree();
		int n = polynomial.degree();
		int w = (m+n);
		for (int i=0; i<=w; i++) 
			coefficients.add(new BigInteger("0"));
		
		Polynomial result = new Polynomial(coefficients);
		
		// Implementation of the convolution function
		for (int i=0; i<=w; i++) {
			for (int j=Math.max(0, i-n); j<=Math.min(i, m); j++) {
/*				BigInteger a = this.coefficients.get(j);
				BigInteger b = polynomial.coefficients.get(i-j);
				BigInteger c = a.multiply(b);
				BigInteger d = result.coefficients.get(i);
				
				result.coefficients.set(i, d.add(c));
*/				result.coefficients.set(i, result.coefficients.get(i).add( this.coefficients.get(j).multiply(polynomial.coefficients.get(i-j)) ));
			}
		}
		
		
			
		this.coefficients.clear();
		this.coefficients = result.getCoefficients();
		
		return this;

		
	}
	
	public List<BigInteger> getCoefficients() {
		return this.coefficients;
	}
	
	public void setCoefficients(List<BigInteger> coefficients) {
		this.coefficients = new LinkedList<BigInteger>(coefficients);
	}
	
	@Override
	public String toString() {
		String output = "";
		for (BigInteger coeff : coefficients) {
			output += coeff.toString()+" ";
		}
		return "[ "+output+"]";
	}
}


/*
package it.uninsubria.dista.bignumbers.classes;
import it.uninsubria.dista.bignumbers.exceptions.*;

import java.util.List;
import java.util.LinkedList;

public class BigNumberPolynomial {

	public BigNumber getCoefficient(int degree) throws NegativeDegreeException, OutOfBoundsDegreeException {
		if (degree < 0) throw new NegativeDegreeException();
		if (degree > this.degree()) throw new OutOfBoundsDegreeException();
		return polynomial.get(this.degree()-degree);
	}
	
	public void setCoefficient(BigNumber coefficient, int degree) throws NegativeDegreeException, OutOfBoundsDegreeException {
		if (degree < 0) throw new NegativeDegreeException();
		if (degree > this.degree()) throw new OutOfBoundsDegreeException();
		
		polynomial.set(this.degree()-degree, coefficient);
	}
	
	// TODO
	public BigNumber evaluate(BigNumber value) {
		BigNumber result = polynomial.get(polynomial.size()-1);
		
		return result;
	}
	

}

*/