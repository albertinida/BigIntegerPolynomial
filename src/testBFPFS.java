import it.uninsubria.dista.PathFinderService.BloomFilteredPathFinder;
import it.uninsubria.dista.PathFinderService.PathFinder;
import it.uninsubria.dista.PathFinderService.BloomFilteredUserData;
import it.uninsubria.dista.PathFinderService.Polynomials.BloomFilteredPolynomial;
import it.uninsubria.dista.PathFinderService.Polynomials.Polynomial;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedList;


public class testBFPFS {

	
	public static void main(String[] args) {
		BloomFilteredPathFinder PFS = BloomFilteredPathFinder.getInstance();

		LinkedList<BigInteger> coeff = new LinkedList<BigInteger>();
		coeff.add(new BigInteger("1"));
		coeff.add(new BigInteger("1"));

		try {

			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			String line;
			while ((line = br.readLine()) != null) {

				if (line.indexOf('#') > -1) {
					String[] data = line.split("@");

					BigInteger userId = new BigInteger(data[0]);
					BloomFilteredPolynomial directs = new BloomFilteredPolynomial(new BigInteger("1"));

					String[] contactIds = data[1].split("#");
					for (String contactId : contactIds) {
						coeff.set(1, new BigInteger("-"+contactId));
						BloomFilteredPolynomial poly = new BloomFilteredPolynomial(coeff);
						directs.threadedConvolution(poly, PFS.getExecutor());
					}
					PFS.addUserData(new BloomFilteredUserData(userId, directs));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.print(PFS);
		System.exit(0);
	}
}
