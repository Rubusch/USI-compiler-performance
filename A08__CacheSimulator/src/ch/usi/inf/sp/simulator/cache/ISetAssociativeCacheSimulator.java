package ch.usi.inf.sp.simulator.cache;


/**
 * Implementations of this interface are simulators of a set-associative cache.
 * 
 * The most important function they provide is boolean handleMemoryAccess(int address).
 * The simulator will be used as follows:
 * 
 * <pre>
 * final int[] addressTrace = ...
 * final SetAssociativeCacheSimulator simulator = ...
 * for (final int address : addressTrace) {
 *   boolean hit = simulator.handleMemoryAccess(address);
 * }
 * final long hits = simulator.getHitCount();
 * final long misses = simulator.getMissCount();
 * </pre>
 * 
 * The simulator operates on 32-bit addresses.
 * The illustration below shows the use of the address bits for a Core 2 L1D cache
 * (8-way set-associative, 64-byte line size, 32 kB total size, thus 64 sets).
 * 
 * <pre>
 *  3 3 2 2 2 2 2 2 2 2 2 2 1 1 1 1 1 1 1 1 1 1
 *  1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |T|T|T|T|T|T|T|T|T|T|T|T|T|T|T|T|T|T|T|T|S|S|S|S|S|S|B|B|B|B|B|B|
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 * +---------------------------------------+-----------+-----------+
 *                  tag                        set          byte
 * </pre>
 * 
 * The following three characteristics have to be configurable using by setting the corresponding arguments to the constructor
 * SetAssociativeCacheSimulator(int numberOfBitsForSet, int numberOfBitsForByteInLine, int numberOfWays):
 * <p>
 * <ul>
 * <li>the number of sets, by giving the number of address bits to use for addressing the <b>set</b> within the cache
 * <li>the line size, by giving the number of address bits to use for addressing a <b>byte</b> within a line
 * <li>the number of ways (the number of lines within the set)
 * </ul>
 * 
 * Every access to the cache represents an access to just one byte of memory (not to an entire word).
 * This means that there are no issues with accesses straddling a cache line boundary.
 * Moreover, we do not distinguish between loads and stores;
 * there is no need to worry about dirty bits and writing cache lines back to memory.
 * <p>
 * Any associative cache needs a replacement policy (for deciding which slot in a set to replace on a miss).
 * This cache simulator has to implement LRU (least-recently used) replacement.
 * <p>
 * The simulator has to be able to correctly simulate at least 1'000'000'000'000 (1 trillion) accesses.
 * This means that the hit and miss counters (and maybe other internal structures) have to be large enough.
 * It is fine if the simulator does not work correctly anymore after 1 trillion accesses.
 * <p>
 * Moreover, the simulator has to correctly count compulsory misses 
 * (misses when a given cache line is accessed for the very first time).
 * For this, it may need to maintain some kind of valid bits.
 * <p>
 * Since fully-associative caches and direct-mapped caches are special cases of set-associative caches,
 * this class can also be used to simulate fully-associative or direct-mapped caches.
 * <ul>
 * <li>To create a fully associative cache simulator,
 * set the numberOfBitsForSet to 0 (there is a single set)
 * and the numberOfWays to the number of slots the cache has in total.
 * <li>To create a direct mapped cache simulator, set the numberOfWays to 1.
 * </ul>
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public interface ISetAssociativeCacheSimulator {

	public int getNumberOfBitsForTag();
	public int getNumberOfBitsForSet();
	public int getNumberOfBitsForByteInLine();

	public int getNumberOfSets();
	public int getNumberOfWays();
	public int getNumberOfBytesInLine();
	public int getCacheSizeInBytes();
	
	/**
	 * Handle a memory access to an individual byte at the given 32-bit address.
	 * We don't distinguish between loads or stores.
	 * @return whether this access hit or missed in this cache
	 */
	public boolean handleMemoryAccess(int address);
	
	public long getHitCount();
	public long getMissCount();

}
