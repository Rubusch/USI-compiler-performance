package ch.usi.inf.sp.simulator.cache;

/**
 * 
 * @author Lothar Rubusch
 *
 */
public class Tester {

	public Tester(){
		runTests();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args){
		Tester tester = new Tester();
		tester.runTests();
	}

	/*
	 * utils
	 */

	private void db(String msg){
		System.out.println("DEBUG: " + msg);
	}

	private void runTests(){
		//*
				DirectMappedCacheSimulatorTest cache = new DirectMappedCacheSimulatorTest();
				cache.testInit();
				cache.testOneLineOnce();
				cache.testAccessOneLineTwice();
				cache.testAccessAllBytesInOneLine();
				cache.testAccessEachLineOnce();
				cache.testAccessSameLineThroughAliases();
		/*/
				
		//*/
				db("READY.\n");
	}
}
