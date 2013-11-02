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

	private int[] tags; // XXX
	private boolean[] validBits; // XXX
	
	private long hitCount; // XXX
	private long missCount; // XXX

	/**
	 * 
	 * @param bitsForSet
	 * @param j
	 * @param k
	 */
	public SetAssociativeCacheSimulator(int bitsForSet, int j, int k) {
		this.bitsForTag = bitsForSet; // TODO check
		this.bitsForSet = j; // TODO check
		this.bitsForByteInLine = k; // TODO check

		final int numberOfLines = 1<<bitsForByteInLine; // TODO check
		tags = new int[numberOfLines]; // TODO check
		validBits = new boolean[numberOfLines]; // TODO check
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfBytesInLine() {
		return 1<<bitsForByteInLine;
	}

	@Override
	public int getCacheSizeInBytes() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean handleMemoryAccess(int address) {
		// TODO Auto-generated method stub
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
