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
/*
//	public static void func(int [] arg0 ){ //, String arg1, String arg2){
//		;
//	}
//*/
	// assignment test case
	public static void main( final String[] args){
		for( int i=1; i<=2; ++i){
			final long[] is = new long[i * 10];
			final Object[] os = new Object[i * 10];
			final int[][][] mis = new int[i * 100][i * 10][i];
			final Object[][] mos = new Object[i * 100][i * 10];

			// DEBUG tests
			System.out.println("DEBUG: first dimension size of MULTIANEWARRAY: " + mis.length); // DEBUG: print out the first dimension - should be i * 100

			// DEBUG initialization
			System.out.print("DEBUG: init is...");
			for( int cnt=0; cnt<i*10; ++cnt){
				is[cnt] = Long.valueOf(cnt);
			}
			System.out.println("ok");

			System.out.print("DEBUG: init os...");
			for( int cnt=0; cnt<i*10; ++cnt){
				os[cnt] = String.valueOf(cnt);
			}
			System.out.println("ok");

			System.out.print("DEBUG: init mis...");
			for (int cnt=0; cnt<i*100; ++cnt) {
				for (int dnt=0; dnt<i*10; ++dnt) {
					for (int ent=0; ent<i; ++ent) {
						mis[cnt][dnt][ent] = 777;
					}
				}
			}
			System.out.println("ok");

			System.out.print("DEBUG: init mos...");
			for (int cnt=0; cnt<i*100; ++cnt) {
				for (int dnt=0; dnt<i*10; ++dnt) {
					mos[cnt][dnt] = String.valueOf(777);
				}
			}
			System.out.println("ok");
		}

		System.out.println("READY.");
	}
}
