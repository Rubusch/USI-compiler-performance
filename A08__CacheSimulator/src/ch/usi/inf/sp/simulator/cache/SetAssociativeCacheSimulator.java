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

	private List<String> memory;
	private int memory_MAXSIZE = 10;

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

		memory = new ArrayList<String>();
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

	/*
	 * tools
	 */
	private void lru_update(int set, int way){
		// append to stack
		String element = String.valueOf(set) + ":" + String.valueOf(way);
//		Tester.db("lru - element "+ element); // XXX
		if(memory.contains(element)){
//			Tester.db("lru - update element"); // XXX
			memory.remove(element);
		}
		memory.add(element);

		// check and in case remove old elements
		if(memory_MAXSIZE < memory.size()){
			Tester.db("lru - memory full, need to discard...");
			set = Integer.valueOf(memory.get(0).split(":")[0]);
			way = Integer.valueOf(memory.get(0).split(":")[1]);
			
//			Tester.db("lru - memory full, set " + String.valueOf(set) + ", way " + String.valueOf(way)); // XXX
			tags[set][way] = 0;
			validBits[set][way] = false;
			memory.remove(0);
		}
	}

	/*
	 * interface
	 */
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

		boolean ret = false;

		if(tags[set][way] == tag && validBits[set][way]){ // TODO 2d array
			hitCount++;
			ret = true;
		}else{
			tags[set][way] = tag; // TODO 2d array
			validBits[set][way] = true; // 2d array
			missCount++;

			lru_update(set, way);
		}

		return ret;
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
