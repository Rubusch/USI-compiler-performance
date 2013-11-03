package ch.usi.inf.sp.simulator.cache;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Lothar Rubusch
 *
 */
// TODO Note that in a freshly instantiated SetAssociativeCacheSimulator, all cache lines are invalid.
// TODO After you implement your class, run the existing JUnit tests (SetAssociativeCacheSimulatorTest) and make sure your simulator passes all the test cases.
// TODO Then add some additional test cases. You get a bonus point for each submitted SetAssociativeCacheSimulator implementation (except yours) that the existing test cases don't break but that your additional test cases manage to break.
public class SetAssociativeCacheSimulator implements
		ISetAssociativeCacheSimulator {
	
	private int bitsForTag;
	private int bitsForSet;
	private int bitsForByteInLine;

	private int numberOfWays;

	private int cacheSizeInBytes;

	private int[][] tags;
	private boolean[][] validBits;

	private long hitCount;
	private long missCount;

	/**
	 * 
	 * @param bitsForSet
	 * @param bitsForByteInLine
	 * @param numberOfWays
	 */
	public SetAssociativeCacheSimulator(int bitsForSet, int bitsForByteInLine, int numberOfWays) {
		this.bitsForSet= bitsForSet;
		Tester.db("\tbitsForSet\t\t" + this.bitsForSet);

		this.bitsForByteInLine = bitsForByteInLine;
		Tester.db("\tbitsForByteInLine\t" + this.bitsForByteInLine);

		this.numberOfWays = numberOfWays;
		Tester.db("\tnumberOfWays\t\t" + this.getNumberOfWays());

		final int numberOfSets = this.getNumberOfSets();
		Tester.db("\tnumberOfSets\t\t" + numberOfSets);

		Tester.db("-");

		this.initHitCounts();
		this.initMissCount();

		tags = new int[numberOfSets][numberOfWays]; // TODO check

		validBits = new boolean[numberOfSets][numberOfWays];
	}

	/*
	 * init counts
	 */
	private void initHitCounts(){
		this.hitCount = 0;
	}

	private void initMissCount(){
		this.missCount = 0;
	}


	@Override
	public int getNumberOfBitsForTag() {
		return bitsForTag;
	}

	@Override
	public int getNumberOfBitsForSet() {
		return bitsForSet;
	}

	@Override
	public int getNumberOfBitsForByteInLine() {
		return bitsForByteInLine;
	}

	@Override
	public int getNumberOfSets() {
		return 1<<bitsForSet;
	}

	@Override
	public int getNumberOfWays() {
		return numberOfWays;
	}

	@Override
	public int getNumberOfBytesInLine() {
		return 1<<bitsForByteInLine;
	}

	@Override
	public int getCacheSizeInBytes() {
		return this.getNumberOfWays() * (1<<this.getNumberOfBitsForByteInLine()) * this.getNumberOfSets();
	}

	@Override
	public boolean handleMemoryAccess(int address) {
		System.out.printf("address: 0x%08x (%d)\n", address, address);

		final int set = (address>>bitsForByteInLine) & ((1<<bitsForByteInLine)-1); // TODO
		Tester.db("\t(address>>bitsForByteInLine) & ((1<<bitsForByteInLine)-1)");
		Tester.db("\t(" + String.valueOf(address) + " >> " + String.valueOf(bitsForByteInLine) + ") & ((1 << " + String.valueOf(bitsForByteInLine) + ")-1) = " + String.valueOf(set));
		System.out.printf("set:    0x%08x (%d)\n", set, set);

		final int way = (address>>>(bitsForByteInLine + bitsForSet) ) & ((1<<bitsForSet)-1);
		Tester.db("\taddress>>>(bitsForByteInLine + bitsForSet) & ((1<<bitsForSet)-1)");
		Tester.db("\t" + String.valueOf(address) + " >>>( " + String.valueOf(bitsForByteInLine) + " + " + String.valueOf(bitsForSet) + " ) & ((1<< " + String.valueOf( bitsForSet ) + ")-1) = " + String.valueOf(way) );
		System.out.printf("way:     0x%08x (%d)\n", way, way);

		final int tag = address>>>(bitsForByteInLine + bitsForByteInLine + bitsForSet); // TODO check
		Tester.db("\taddress>>>(bitsForByteInLine + bitsForByteInLine + bitsForSet)");
		Tester.db("\t" + String.valueOf(address) + " >>>( " + String.valueOf( bitsForByteInLine) + " + " + String.valueOf(bitsForByteInLine) + " + " + String.valueOf(bitsForSet) + ") = " + String.valueOf(tag));
		System.out.printf("tag:     0x%08x (%d)\n", tag, tag);

		if(tags[set][way] == tag && validBits[set][way]){ // TODO 2d array
			hitCount++;
			return true;
		}else{
			tags[set][way] = tag; // TODO 2d array
			validBits[set][way] = true; // 2d array
			missCount++;
		}
		return false;
	}

	@Override
	public long getHitCount() {
		return this.hitCount;
	}

	@Override
	public long getMissCount() {
		return this.missCount;
	}
}
