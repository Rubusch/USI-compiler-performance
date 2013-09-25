import java.util.HashMap;


public class L01 {
	private static HashMap< String, Integer > hm;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		hm = new HashMap< String, Integer >();

// TODO why this separate assignment?
		final HashMap< String, Integer > mymap;

/*
		// working
		mymap = hm;
		System.out.println( "set to working" );
/*/
		// NPE
		System.out.println( "set to NPE");
//*/
		mymap.put("keyA", new Integer( 111 ));
		mymap.put("keyB", new Integer( 222 ));
		mymap.put("keyC", new Integer( 333 ));

// TODO print

		System.out.println("\nREADY.");
	}

}
