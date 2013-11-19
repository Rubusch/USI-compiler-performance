package ch.usi.inf.sp.simulator.cache;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Lothar Rubusch
 *
 */
public class Tester {
	private SetAssociativeCacheSimulator cache;

	/**
	 * @param args
	 */
	public static void main(String[] args){
		Tester tester = new Tester();

		if(0 == args.length){
			tester.runSetAssociativeCacheTests();
			return;
		}else{
			// Intel Core 2 L1 data cache
			tester.runDataSet(args[0], 6, 6, 8, 32);

			// Twice as many ways
			tester.runDataSet(args[0], 6, 6, 16, 64);

			// Twice as many sets
			tester.runDataSet(args[0], 7, 6, 8, 64);

			// Twice as large cache lines
			tester.runDataSet(args[0], 6, 7, 8, 64);
		}
		db("READY.\n");
	}

	/*
	 * utils
	 */

	public static void db(String msg){
// XXX uncomment for debugging 
//		System.out.println("DEBUG:\t" + msg );
	}

	public static void die(){
		die("STOP");
	}

	public static void die(String msg){
		System.out.println(msg);
		System.exit(0);
	}

	/*
	 * tests
	 */
	private void runDirectMappedCacheTests(){
		db("Direct Mapped Cache");

		DirectMappedCacheSimulatorTest cache = new DirectMappedCacheSimulatorTest();

		db("testInit()");
		cache.testInit();
		db("");

		db("testOneLineOnce()");
		cache.testOneLineOnce();
		db("");

		db("testAccessOneLineTwice()");
		cache.testAccessOneLineTwice();
		db("");

		db("testAccessAllBytesInOneLine()");
		cache.testAccessAllBytesInOneLine();
		db("");

		db("testAccessEachLineOnce()");
		cache.testAccessEachLineOnce();
		db("");

		db("testAccessSameLineThroughAliases()");
		cache.testAccessSameLineThroughAliases();
		db("");
	}

	private void runSetAssociativeCacheTests(){
		db("Set Associative Cache");

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

		db("testAccessAllBytesInOneLine()");
		cache.testAccessAllBytesInOneLine();
		db("");

		db("testAccessEachSetOnce()");
		cache.testAccessEachSetOnce();
		db("");

		db("testAccessEachSlotInOneSetOnce()");
		cache.testAccessEachSlotInOneSetOnce();
		db("");

		db("testAccessAllBytesInOneSet()");
		cache.testAccessAllBytesInOneSet();
		db("");
	}

	private void runDataSet(String traceFileName, int bitsForSet, int bitsForByteInLine, int numberOfWays, int memory_size ){
		cache = new SetAssociativeCacheSimulator( bitsForSet, bitsForByteInLine, numberOfWays);

		int cache_size = memory_size * 1000 / (2^bitsForSet * 2^bitsForByteInLine);

		try {
			read(traceFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("---");
		System.out.println("Result:");
		System.out.println("numberOfBitsForSet\t\t" + bitsForSet);
		System.out.println("numberOfBitsForByteInLine\t" + bitsForByteInLine);
		System.out.println("numberOfWays\t\t\t" + numberOfWays);
		System.out.println("memory_size\t\t\t\t" + memory_size);
		System.out.println("---");
		System.out.println("hit count: " + cache.getHitCount() + ", miss count: " + cache.getMissCount() );
		System.out.println("---");
		double totalAccess = cache.getHitCount() + cache.getMissCount();
		double hitRate = ((double) cache.getHitCount()) / totalAccess;
		double missRate = ((double) cache.getMissCount()) / totalAccess;
		System.out.println("hit rate: " + String.valueOf(hitRate) + ", miss rate " + String.valueOf(missRate) );
		System.out.println("===\n\n");
	}


	public void read(String traceFileName) throws IOException {
		final BufferedReader br = new BufferedReader(new FileReader(traceFileName));
		final Pattern pattern = Pattern.compile("0x([0-9a-fA-F]+): ([RW]) 0x([0-9a-fA-F]+)");
		String line;
		while ((line = br.readLine()) != null) {
			final Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				final String instructionAddressString = matcher.group(1);
				// use Long.parseLong because Integer.parseInt uses _signed_ ints (thus
				// only accepts values up to +2^31-1)
				final int instructionAddress = (int) Long.parseLong(instructionAddressString, 16);
				final String readWriteString = matcher.group(2);
				final boolean isWrite = readWriteString.charAt(0) == 'W';
				final String dataAddressString = matcher.group(3);
				// use Long.parseLong because Integer.parseInt uses _signed_ ints (thus
				// only accepts values up to +2^31-1)

				final int dataAddress = (int) Long.parseLong(dataAddressString, 16);

				cache.handleMemoryAccess(dataAddress);
			}
		}
		br.close();
	}
}
