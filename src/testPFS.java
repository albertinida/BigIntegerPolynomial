import it.uninsubria.dista.PathFinderService.PathFinder;
import it.uninsubria.dista.PathFinderService.UserData;
import it.uninsubria.dista.PathFinderService.Polynomials.Polynomial;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedList;


public class testPFS {

	
	public static void main(String[] args) {
		PathFinder PFS = PathFinder.getInstance();

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
					Polynomial directs = new Polynomial(new BigInteger("1"));

					String[] contactIds = data[1].split("#");
					for (String contactId : contactIds) {
						coeff.set(1, new BigInteger("-"+contactId));
						Polynomial poly = new Polynomial(coeff);
						directs.threadedConvolution(poly, PFS.getExecutor());
					}
					PFS.addUserData(new UserData(userId, directs));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.print(PFS);
		System.exit(0);
	}
}
