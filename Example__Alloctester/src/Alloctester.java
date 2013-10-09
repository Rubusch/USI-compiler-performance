import java.util.ArrayList;
import java.util.List;


public class Alloctester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<Integer> aaa = new ArrayList<Integer>();
		for( int idx=0; idx < 99999; ++idx){
			Integer tmp = idx;
			if(tmp % 10 == 0) aaa.add(tmp);
		}
		System.out.println( "READY\n" );
	}
}
