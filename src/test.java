import it.uninsubria.dista.BigInteger.Polynomials.Polynomial;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class test {

	private final static int avgContacts = 150;
	private final static BigInteger evaluate = new BigInteger("4294967296");
	private static ExecutorService executor = Executors.newFixedThreadPool(10);
	// private static ExecutorService executor = Executors.newCachedThreadPool();

	
	public static void main(String[] args) throws Exception {

		Polynomial poly1 = new Polynomial(new BigInteger("1")), poly2 = new Polynomial();
		List<BigInteger> coeff = new LinkedList<BigInteger>();

		coeff.add(new BigInteger("1"));
		coeff.add(new BigInteger("1"));
		poly2 = new Polynomial(coeff);
		coeff.clear();
		
		Calendar start = Calendar.getInstance();
		for (int i=0; i<avgContacts; i++) { 
			
			poly2.setCoefficient(new BigInteger(64, new Random()), 0);	
			//poly1.multiply(poly2);
			//poly1.convolution(poly2);
			poly1.threadedConvolution(poly2, executor);
		}
		Calendar stop = Calendar.getInstance();
		System.out.print("Grado :"+poly1.degree()+"\t\tCostruzione: "+(stop.getTimeInMillis()-start.getTimeInMillis())+"ms");
		
		start = Calendar.getInstance();
		//poly1.evaluate(new BigInteger(64, new Random()));
		poly1.hornerEvaluate(evaluate);
		stop = Calendar.getInstance();
		System.out.println("\t\tValutazione: "+(stop.getTimeInMillis()-start.getTimeInMillis())+"ms");

		System.out.println();
		System.out.println("COSTRUZIONE PER MOLTIPLICAZIONI SUCCESSIVE");
		System.out.println();
		
		
		poly2.setCoefficients(poly1.getCoefficients());
		for (;;) {
			// MOLTIPLICAZIONE
			start = Calendar.getInstance();
			//poly1.multiply(poly2);
			//poly1.convolution(poly2);
			poly1.threadedConvolution(poly2, executor);
			stop = Calendar.getInstance();
			System.out.print("Grado :"+poly1.degree()+"\t\tCostruzione: "+(stop.getTimeInMillis()-start.getTimeInMillis())+"ms");
			
			// VALUTAZIONE
			start = Calendar.getInstance();
			//poly1.evaluate(new BigInteger(64, new Random()));
			poly1.hornerEvaluate(evaluate);
			stop = Calendar.getInstance();
			System.out.println("\t\tValutazione: "+(stop.getTimeInMillis()-start.getTimeInMillis())+"ms");
		}
	}
}
