package it.uninsubria.dista.PathFinderService.Polynomials;

import it.uninsubria.dista.BloomFilters.BloomFilter;
import it.uninsubria.dista.PathFinderService.Exceptions.ExcessiveDegreeException;
import it.uninsubria.dista.PathFinderService.Exceptions.NegativeDegreeException;
import it.uninsubria.dista.PathFinderService.Exceptions.UnsupportedIntersectionOperation;

import java.math.*;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class BloomFilteredPolynomial {

	private List<BigInteger> coefficients;
	private BloomFilter<BigInteger> bloomFilter;
	
	public BloomFilteredPolynomial() {
		this.coefficients = new LinkedList<BigInteger>();
		this.bloomFilter = new BloomFilter<BigInteger>(0.001, 1000);
	}
	
	public BloomFilteredPolynomial(BloomFilteredPolynomial polynomial) {
		this.coefficients = new LinkedList<BigInteger>(polynomial.getCoefficients());
		
		this.bloomFilter = polynomial.getBloomFilter();
	}
	
	public BloomFilteredPolynomial(int length) {
		this.coefficients = new LinkedList<BigInteger>();
		for (int i=0; i<length; i++) 
			coefficients.add(new BigInteger("0"));

		this.bloomFilter = new BloomFilter<BigInteger>(0.001, 1000);

	}

	public BloomFilteredPolynomial(List<BigInteger> coefficients) throws Exception {
		if (coefficients.size() > 2) throw new Exception();
		this.coefficients = new LinkedList<BigInteger>(coefficients);

		this.bloomFilter = new BloomFilter<BigInteger>(0.001, 1000);
		this.bloomFilter.add(coefficients.get(1).abs());
	}
	
	public BloomFilteredPolynomial(BigInteger monomial) {
		this.coefficients = new LinkedList<BigInteger>();
		this.coefficients.add(monomial);
		
		this.bloomFilter = new BloomFilter<BigInteger>(0.001, 1000);
		this.bloomFilter.add(monomial);
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
	
	public BloomFilter<BigInteger> getBloomFilter() {
		return this.bloomFilter;
	}
	
	public boolean bloomFilterContains(BigInteger value) {
		return this.bloomFilter.contains(value);
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

	public BloomFilteredPolynomial threadedConvolution(BloomFilteredPolynomial polynomial, ExecutorService executor) throws InterruptedException, ExecutionException {

		int m = this.degree();
		int n = polynomial.degree();
		int w = (m+n);
		
		BloomFilteredPolynomial result = new BloomFilteredPolynomial(w+1);
		ArrayList<Future<BigInteger>> threads = new ArrayList<Future<BigInteger>>();
		
		for (int i=0; i<=w; i++) {
			threads.add(executor.submit(new BloomFilteredConvolutionThread(this, polynomial, i)));
		}
		
		for (int i=0; i<=w; i++) {
			result.setCoefficientByIndex(threads.get(i).get(), i);
		}
		try {
			this.bloomFilter.intersect(polynomial.bloomFilter);
		} catch (UnsupportedIntersectionOperation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
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

class BloomFilteredConvolutionThread implements Callable<BigInteger> {

	private BloomFilteredPolynomial poly1, poly2;
	private int valueToCompute;
	
	public BloomFilteredConvolutionThread(BloomFilteredPolynomial poly1, BloomFilteredPolynomial poly2, int valueToCompute) {
		this.poly1 = poly1;
		this.poly2 = poly2;
		this.valueToCompute = valueToCompute;
	}
	
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