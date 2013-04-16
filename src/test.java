import it.uninsubria.dista.BigInteger.Polynomials.Polynomial;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class test {

	private final static int avgContacts = 130;
	
	public static void main(String[] args) throws Exception {

		Polynomial poly1 = new Polynomial(new BigInteger("1")), poly2 = new Polynomial();
		List<BigInteger> coeff = new LinkedList<BigInteger>();

		coeff.add(new BigInteger("1"));
		coeff.add(new BigInteger("1"));
		poly2 = new Polynomial(coeff);
		
		Calendar start = Calendar.getInstance();
		for (int i=0; i<avgContacts; i++) { 
			
			poly2.setCoefficient(new BigInteger(64, new Random()), 0);	
			//poly1.multiply(poly2);
			poly1.convMultiply(poly2);
		}
		Calendar stop = Calendar.getInstance();
		System.out.println("Costruzione di un polinomio con grado "+poly1.degree()+": "+(stop.getTimeInMillis()-start.getTimeInMillis())+"ms");
		
		start = Calendar.getInstance();
		poly1.evaluate(new BigInteger(64, new Random()));
		stop = Calendar.getInstance();
		System.out.println("Valutazione del polinomio : "+(stop.getTimeInMillis()-start.getTimeInMillis())+"ms");

		for (;;) {
			poly2.setCoefficients(poly1.getCoefficients());
			
			start = Calendar.getInstance();
			
			for (int i=0; i<avgContacts; i++) {
				Calendar innerStart = Calendar.getInstance();
				System.out.print(Calendar.getInstance().getTimeInMillis()+" sto moltiplicando un poly di grado "+poly1.degree()+" per un poly di grado "+poly2.degree());
				//poly1.multiply(poly2);
				poly1.convMultiply(poly2);
				Calendar innerStop = Calendar.getInstance();
				System.out.println("\t"+(innerStop.getTimeInMillis()-innerStart.getTimeInMillis())+"ms");
				System.gc();
				innerStart = Calendar.getInstance();
				poly1.evaluate(new BigInteger(64, new Random()));
				innerStop = Calendar.getInstance();
				System.out.println("\t\t Valutazione: "+(innerStop.getTimeInMillis()-innerStart.getTimeInMillis())+"ms");
				
			}
			stop = Calendar.getInstance();
			System.out.println("Costruzione di un polinomio con grado "+poly2.degree()+": "+(stop.getTimeInMillis()-start.getTimeInMillis())+"ms");
			
			start = Calendar.getInstance();
			poly1.evaluate(new BigInteger(64, new Random()));
			stop = Calendar.getInstance();
			System.out.println("Valutazione del polinomio : "+(stop.getTimeInMillis()-start.getTimeInMillis())+"ms");
		}
		
	}
}
