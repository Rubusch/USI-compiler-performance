import java.util.HashMap;


public class L01 {
	private static HashMap< String, Integer > mymap;
	public static void main(String[] args) {
		mymap = new HashMap< String, Integer >();
		mymap.put("keyA", new Integer( 111 ));
		mymap.put("keyB", new Integer( 222 ));
/*
		// works normal
		Integer value = mymap.get("keyA");
/*/
		// throws NullPointerException
		Integer value = mymap.get("keyC");		
//*/
		System.out.println( "value: " + value.intValue() );

		System.out.println("\nREADY.");
	}

}
