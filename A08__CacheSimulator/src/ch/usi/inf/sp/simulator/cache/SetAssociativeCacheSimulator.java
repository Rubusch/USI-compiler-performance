package ch.usi.inf.sp.simulator.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Lothar Rubusch
 *
 */
public class SetAssociativeCacheSimulator implements
		ISetAssociativeCacheSimulator {

	/*
// TODO correction

	cache will be a list of size 2^bitsForSet
	cache elements are lists of size 2^bitsForWay

	a line corresponds to a way in the set

	offset // byte will be 2^offset number of bytes of data (dcache) or instruction (icache)

	there will be a hashmap to check if a tag is already in one of the ways (then update) of a way (line)
	 */
// TODO

	private int bitsForTag;
	private int bitsForSet;
	private int bitsForWay;
	private int bitsForByteInLine;

	private int numberOfWays;

	private int cacheSizeInBytes;

/*
	private int[][] tags;
/*/
	// key: tags, value: line // way in memory
	private Map tags;
//*/

	private boolean[][] validBits;

	private long hitCount;
	private long missCount;

/*
	private List<String> memory;
	private int memory_SIZE = 10;
/*/
	private List<List<Integer>> cache;
//*/

	/**
	 * 
	 * @param bitsForSet
	 * @param bitsForByteInLine
	 * @param numberOfWays
	 */
	public SetAssociativeCacheSimulator(int bitsForSet, int bitsForByteInLine, int numberOfWays) {
		this.bitsForSet= bitsForSet;
		this.bitsForByteInLine = bitsForByteInLine;

		int tmp = numberOfWays;
		this.bitsForWay = 0;
		for( int cnt=0; tmp > 1;++cnt){
			tmp = tmp >>> 1;
			this.bitsForWay++;
		}

		this.numberOfWays = numberOfWays;
		this.initHitCounts();
		this.initMissCount();

		final int numberOfSets = this.getNumberOfSets();
/*
		tags = new int[numberOfSets][numberOfWays];
/*/
		tags = new HashMap<String, String>();
//*/
		validBits = new boolean[numberOfSets][numberOfWays];
//		memory = new ArrayList<String>();
		
		cache = new ArrayList<List<Integer>>();
		int cacheSize = 1<<bitsForSet;
		int elementSizeInByte = 1<<this.bitsForWay;
		for( int i=0; i<cacheSize; ++i){
			List<Integer> element = new ArrayList<Integer>();
			for( int j=0;j<elementSizeInByte;++j){
				element.add(0);
			}
			cache.add(element);
		}

		// debug
		Tester.db("\tbitsForSet\t\t" + bitsForSet + "\tnumberOfSets\t\t" + getNumberOfSets());
		Tester.db("\tbitsForWays\t\t" + bitsForWay + "\tnumberOfWays\t\t" + getNumberOfWays() );
//		Tester.db("\tmemory_SIZE\t\t" + this.memory_SIZE);
		Tester.db("\tbitsForByteInLine\t" + this.bitsForByteInLine);

		Tester.db("-");
	}
/* TODO rm
	public void setMemorySize( int memory_size ){
		this.memory_SIZE = memory_size;
	}
//*/
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
/* TODO rm
		if(memory.contains(element)){
			memory.remove(element);
		}
		memory.add(element);
//*/



/*
		// check and in case remove old elements
		if(memory_SIZE < memory.size()){
			Tester.db("lru - memory full, need to discard...");
			set = Integer.valueOf(memory.get(0).split(":")[0]);
			way = Integer.valueOf(memory.get(0).split(":")[1]);
			tags[set][way] = 0;
			validBits[set][way] = false;
			memory.remove(0);
		}
/*/
		// TODO
//*/
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
//		System.out.printf("address: 0x%08x (%d)\n", address, address);

		final int set = (address>>bitsForByteInLine) & ((1<<bitsForSet)-1);
		Tester.db("\t(address>>bitsForByteInLine) & ((1<<bitsForSet)-1)");
		Tester.db("\t(" + String.valueOf(address) + " >> " + String.valueOf(bitsForByteInLine) + ") & ((1 << " + String.valueOf(bitsForSet) + ")-1) = " + String.valueOf(set));
//		System.out.printf("set:    0x%08x (%d)\n", set, set);

		final int way = (address >>> (bitsForByteInLine + bitsForSet) ) & ((1 << bitsForWay)-1);
		Tester.db("\taddress>>>(bitsForByteInLine + bitsForSet) & ((1<<bitsForWay)-1)");
		Tester.db("\t" + String.valueOf(address) + " >>>( " + String.valueOf(bitsForByteInLine) + " + " + String.valueOf(bitsForSet) + " ) & ((1<< " + String.valueOf( bitsForWay ) + ")-1) = " + String.valueOf(way) );
//		System.out.printf("way:     0x%08x (%d)\n", way, way);

		final int tag = address>>>(bitsForByteInLine + bitsForSet + bitsForWay);
		Tester.db("\taddress>>>(bitsForByteInLine + bitsForSet + bitsForWay)");
		Tester.db("\t" + String.valueOf(address) + " >>>( " + String.valueOf( bitsForByteInLine) + " + " + String.valueOf(bitsForSet) + " + " + String.valueOf(bitsForWay) + ") = " + String.valueOf(tag));
//		System.out.printf("tag:     0x%08x (%d)\n", tag, tag);

		boolean ret = false;

/*
		if(tags[set][way] == tag && validBits[set][way]){
			hitCount++;
			ret = true;
		}else{
			tags[set][way] = tag;
			validBits[set][way] = true;
			missCount++;
			lru_update(set, way);
		}
/*/
		// TODO
//*/
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
