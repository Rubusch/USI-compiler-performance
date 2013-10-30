package ch.usi.inf.sp.simulator.cache;

/**
 * 
 * @author Lothar Rubusch
 *
 */
// TODO Note that in a freshly instantiated SetAssociativeCacheSimulator, all cache lines are invalid.
// TODO After you implement your class, run the existing JUnit tests (SetAssociativeCacheSimulatorTest) and make sure your simulator passes all the test cases.
public class SetAssociativeCacheSimulator implements
		ISetAssociativeCacheSimulator {

	public SetAssociativeCacheSimulator(int i, int j, int k) {
		// TODO Auto-generated constructor stub
	}


	@Override
	public int getNumberOfBitsForTag() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfBitsForSet() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfBitsForByteInLine() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfSets() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfWays() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfBytesInLine() {
		// TODO Auto-generated method stub
		return 0;
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getMissCount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
