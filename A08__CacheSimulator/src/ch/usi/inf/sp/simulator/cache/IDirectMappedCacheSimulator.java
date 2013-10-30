package ch.usi.inf.sp.simulator.cache;


/**
 * This interface represents a simulator for a direct-mapped cache.
 * 
 * @deprecated Use the SetAssociativeCacheSimulator instead.
 * @author Matthias.Hauswirth@usi.ch
 */
public interface IDirectMappedCacheSimulator {

	public int getNumberOfBitsForTag();
	public int getNumberOfBitsForLine();
	public int getNumberOfBitsForByteInLine();
	
	public int getNumberOfLines();
	public int getNumberOfBytesInLine();
	
	/**
	 * Handle a memory access to an individual byte at the given 32-bit address.
	 * We don't distinguish between loads or stores.
	 */
	public void handleMemoryAccess(final int address);
	
	public long getHitCount();
	public long getMissCount();

}
