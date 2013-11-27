/*
 * several test cases for instrumentation of calls:
 * 
 * NEWARRAY
 * ANEWARRAY
 * MULTIANEWARRAY
 */
public class Application {
/*
	// common instrumentation "hello world" application
	public static void main(String[] args) {
		sayHi();
	}

	private static void sayHi(){
		System.out.println("Hello");
	}
/* /
	// jagged example (non-jagged)
	public static void main(String[] argv) {
		int twoD[][] = new int[4][];
		twoD[0] = new int[5];
		twoD[1] = new int[5];
		twoD[2] = new int[5];
		twoD[3] = new int[5];
	}
/* /
	// jagged test case
	public static void main(String args[]) {
		int twoD[][] = new int[4][];
		twoD[0] = new int[1];
		twoD[1] = new int[2];
		twoD[2] = new int[3];
		twoD[3] = new int[4];

		for (int i = 0; i < 4; i++){
			for (int j = 0; j < i + 1; j++) {
				twoD[i][j] = i + j;
			}
		}
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < i + 1; j++)
				System.out.print(twoD[i][j] + " ");
			System.out.println();
		}
	}

/*/
	// assignment test case
	public static void main( final String[] args){
		for( int i=1; i<2; ++i){
			final long[] is = new long[i * 10];
			final Object[] os = new Object[i * 10];
			final int[][][] mis = new int[i * 10][i * 10][i];
			final Object[][] mos = new Object[i * 10][i * 10];
		}
		System.out.println("READY.");

//		logNewArray( "foo", 7);
//		logNewArray( 7, "foo");
	}
//*/

/*
//	public static void logNewArray( String message, int size ){
	public static void logNewArray( int size, String message ){
		System.out.println(message + ", " + String.valueOf( size ));
	}
//*/
}
