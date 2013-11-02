package ch.usi.inf.sp.simulator.cache;

/**
 * 
 * @author Lothar Rubusch
 *
 */
public class Tester {

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

	public static void db(String msg){
		System.out.println("DEBUG:\t" + msg );
	}

	public static void die(){
		die("STOP");
	}

	public static void die(String msg){
		System.out.println(msg);
		System.exit(0);
	}

	
	private void runTests(){
//*
		DirectMappedCacheSimulatorTest cache = new DirectMappedCacheSimulatorTest();

		db("testInit()");
		cache.testInit();
		db("");

//		db("testOneLineOnce()");
//		cache.testOneLineOnce();
//		db("");

//		db("testAccessOneLineTwice()");
//		cache.testAccessOneLineTwice();
//		db("");

		db("testAccessAllBytesInOneLine()");
		cache.testAccessAllBytesInOneLine();
		db("");

//		db("testAccessEachLineOnce()");
//		cache.testAccessEachLineOnce();
//		db("");

//		db("testAccessSameLineThroughAliases()");
//		cache.testAccessSameLineThroughAliases();
//		db("");
		/*/
		SetAssociativeCacheSimulatorTest cache = new SetAssociativeCacheSimulatorTest();

		db("testInit()");
		cache.testInit();
		db("");

		db("testOneLineOnce()");
		cache.testOneLineOnce();
		db("");

		db("testAccessOneLineTwice()");
		cache.testAccessOneLineTwice();
		db("");

//		db("testAccessAllBytesInOneLine()");
//		cache.testAccessAllBytesInOneLine();
//		db("");

//		db("testAccessEachSetOnce()");
//		cache.testAccessEachSetOnce();
//		db("");

//		db("testAccessEachSlotInOneSetOnce()");
//		cache.testAccessEachSlotInOneSetOnce();
//		db("");

//		db("testAccessAllBytesInOneSet()");
//		cache.testAccessAllBytesInOneSet();
//		db("");
//*/
		db("READY.\n");
	}
}
