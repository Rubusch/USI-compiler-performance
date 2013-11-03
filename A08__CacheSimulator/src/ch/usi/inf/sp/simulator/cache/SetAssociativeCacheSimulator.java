package ch.usi.inf.sp.simulator.cache;

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

	private int[] tags; // XXX
	private boolean[] validBits; // XXX
	
	private long hitCount; // XXX
	private long missCount; // XXX

	/**
	 * 
	 * @param bitsForSet
	 * @param bitsForByteInLine
	 * @param numberOfWays
	 */
	public SetAssociativeCacheSimulator(int bitsForSet, int bitsForByteInLine, int numberOfWays) {
		this.bitsForSet= bitsForSet;
		this.bitsForByteInLine = bitsForByteInLine;
		this.numberOfWays = numberOfWays;

		this.initHitCounts();
		this.initMissCount();

		final int numberOfLines = this.getNumberOfBytesInLine(); // TODO check
		tags = new int[numberOfLines]; // TODO check
		validBits = new boolean[numberOfLines]; // TODO check
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
/*
		Tester.db("getNumberOfWays() " + String.valueOf(this.getNumberOfWays()) ); // XXX
		Tester.db("getNumberOfBitsForByteInLine() " + String.valueOf(this.getNumberOfBitsForByteInLine()) ); // XXX
		Tester.db("getNumberOfSets() " + String.valueOf(this.getNumberOfSets()) ); // XXX
//*/
		return this.getNumberOfWays() * (1<<this.getNumberOfBitsForByteInLine()) * this.getNumberOfSets();
	}

	@Override
	public boolean handleMemoryAccess(int address) {
		System.out.printf("address: 0x%08x (%d)\n", address, address);

		final int line = (address>>bitsForByteInLine) & ((1<<bitsForByteInLine)-1); // TODO
		Tester.db("\t(address>>bitsForByteInLine) & ((1<<bitsForByteInLine)-1)");
		Tester.db("\t(" + String.valueOf(address) + " >> " + String.valueOf(bitsForByteInLine) + ") & ((1 << " + String.valueOf(bitsForByteInLine) + ")-1) = " + String.valueOf(line));
		System.out.printf("line:    0x%08x (%d)\n", line, line);

		final int set = address>>>(bitsForByteInLine + bitsForSet);
		Tester.db("\taddress>>>(bitsForByteInLine + bitsForSet)");
		Tester.db("\t" + String.valueOf(address) + " >>>( " + String.valueOf(bitsForByteInLine) + " + " + String.valueOf(bitsForSet) + " ) = " + String.valueOf(set) );
		System.out.printf("set:     0x%08x (%d)\n", set, set);

		final int tag = address>>>(bitsForByteInLine + bitsForByteInLine + bitsForSet); // TODO check
		Tester.db("\taddress>>>(bitsForByteInLine + bitsForByteInLine + bitsForSet)");
		Tester.db("\t" + String.valueOf(address) + " >>>( " + String.valueOf( bitsForByteInLine) + " + " + String.valueOf(bitsForByteInLine) + " + " + String.valueOf(bitsForSet) + ") = " + String.valueOf(tag));
		System.out.printf("tag:     0x%08x (%d)\n", tag, tag);
/*
		final int line = (address>>bitsForByteInLine)&((1<<bitsForLine)-1);
		System.out.printf("line:    0x%08x (%d)\n", line, line);

		final int tag = address>>>(bitsForLine+bitsForByteInLine);
		System.out.printf("tag:     0x%08x (%d)\n", tag, tag);

		if (tags[line]==tag && validBits[line]) {
			hitCount++;
		} else {
			tags[line] = tag;
			validBits[line] = true;
			missCount++;
		}
//*/
		this.missCount++; // TODO
		
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
