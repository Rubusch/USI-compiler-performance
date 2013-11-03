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
	private SetAssociativeCacheSimulatorTest cache;

	/**
	 * @param args
	 */
	public static void main(String[] args){
		Tester tester = new Tester();

		if(0 == args.length){
			tester.runSetAssociativeCacheTests();
			return;
		}else{
			tester.runDataSet(args[0]);
		}
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

		db("READY.\n");
	}

	private void runDataSet(String traceFileName){
		try {
			cache = new SetAssociativeCacheSimulatorTest();
			read(traceFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
//				System.out.println("ia=" + Integer.toHexString(instructionAddress) + ", da="
//						+ Integer.toHexString(dataAddress) + ", write=" + isWrite);

				// process data
				if( isWrite ){
					
				}
			}
		}
		br.close();
	}
}
