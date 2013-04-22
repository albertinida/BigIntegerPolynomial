package it.uninsubria.dista.PathFinderService.Polynomials;

import it.uninsubria.dista.PathFinderService.Exceptions.ExcessiveDegreeException;
import it.uninsubria.dista.PathFinderService.Exceptions.NegativeDegreeException;

import java.math.*;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Polynomial {

	private List<BigInteger> coefficients;
	
	public Polynomial() {
		this.coefficients = new LinkedList<BigInteger>();
	}
	
	public Polynomial(Polynomial polynomial) {
		this.coefficients = new LinkedList<BigInteger>(polynomial.getCoefficients());
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
	
	public BigInteger getCoefficient(int degree) {
		return coefficients.get(this.degree()-degree);
	}
	
	public void setCoefficient(BigInteger coefficient, int degree) {
		coefficients.set(this.degree()-degree, coefficient);
	}
	
	public BigInteger getCoefficientByIndex(int index) {
		return this.coefficients.get(index);
	}
	
	public void setCoefficientByIndex(BigInteger coefficient, int index) {
		this.coefficients.set(index, coefficient);
	}
	
	public BigInteger evaluate(BigInteger value) {

		BigInteger evaluation = new BigInteger("0");
		BigInteger power = new BigInteger("1");

		for (int i=0; i<=this.degree(); i++) {
			power = power.multiply(value);
			evaluation = evaluation.add(this.getCoefficient(i).multiply(value.pow(i)));
		}
		
		return evaluation;
	}
	
	public BigInteger hornerEvaluate(BigInteger value) {
		
		BigInteger evaluation = new BigInteger("0");
		
		for (int i=0; i<this.coefficients.size(); i++) {
			evaluation = (evaluation.multiply(value)).add(this.getCoefficientByIndex(i));
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

	
	public Polynomial convolution(Polynomial polynomial) throws NegativeDegreeException, ExcessiveDegreeException {
		
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
				result.coefficients.set(i, result.coefficients.get(i).add( this.coefficients.get(j).multiply(polynomial.coefficients.get(i-j)) ));
			}
		}
			
		this.coefficients.clear();
		this.coefficients = result.getCoefficients();
		
		return this;
	}
	
	public Polynomial threadedConvolution(Polynomial polynomial, ExecutorService executor) throws InterruptedException, ExecutionException {

		List<BigInteger> coefficients = new LinkedList<BigInteger>();
		int m = this.degree();
		int n = polynomial.degree();
		int w = (m+n);
		for (int i=0; i<=w; i++) 
			coefficients.add(new BigInteger("0"));
		
		Polynomial result = new Polynomial(coefficients);
		ArrayList<Future<BigInteger>> threads = new ArrayList<Future<BigInteger>>();
		
		for (int i=0; i<=w; i++) {
			threads.add(executor.submit(new ConvolutionThread(this, polynomial, i)));
		}
		
		for (int i=0; i<=w; i++) {
			result.setCoefficientByIndex(threads.get(i).get(), i);
		}
		
		this.coefficients.clear();
		this.coefficients = result.getCoefficients();
		
		return result;
	}
	
	public List<BigInteger> getCoefficients() {
		return this.coefficients;
	}
	
	public void setCoefficients(List<BigInteger> coefficients) {
		this.coefficients = new LinkedList<BigInteger>(coefficients);
	}
	
	public int length() {
		return this.coefficients.size();
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

class ConvolutionThread implements Callable<BigInteger> {

	private Polynomial poly1, poly2;
	private int valueToCompute;
	
	public ConvolutionThread(Polynomial poly1, Polynomial poly2, int valueToCompute) {
		this.poly1 = poly1;
		this.poly2 = poly2;
		this.valueToCompute = valueToCompute;
	}
	
	@Override
	public BigInteger call() throws Exception {

		int m = poly1.degree();
		int n = poly2.degree();
		int i = this.valueToCompute;
		
		BigInteger result = new BigInteger("0");
		
		for (int j=Math.max(0, i-n); j<=Math.min(i, m); j++) {
			result = result.add( poly1.getCoefficientByIndex(j).multiply(poly2.getCoefficientByIndex(i-j)));
		}

		return result;
	}
	
	
}