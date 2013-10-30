package ch.usi.inf.sp.simulator.cache;

import ch.usi.inf.sp.simulator.cache.ISetAssociativeCacheSimulator;
import junit.framework.TestCase;


public class SetAssociativeCacheSimulatorTest extends TestCase {

	public void testInit() {
		final ISetAssociativeCacheSimulator c = new SetAssociativeCacheSimulator(3, 4, 5);
		assertEquals(0, c.getHitCount());
		assertEquals(0, c.getMissCount());
		assertEquals(3, c.getNumberOfBitsForSet());
		assertEquals(4, c.getNumberOfBitsForByteInLine());
		assertEquals(5, c.getNumberOfWays());
		assertEquals(2*2*2, c.getNumberOfSets());
		assertEquals(2*2*2*2, c.getNumberOfBytesInLine());
		assertEquals(5*(2*2*2*2)*(2*2*2), c.getCacheSizeInBytes());
	}
	
	public void testOneLineOnce() {
		final ISetAssociativeCacheSimulator c = new SetAssociativeCacheSimulator(6, 6, 8);
		c.handleMemoryAccess(0);
		assertEquals(0, c.getHitCount());
		assertEquals(1, c.getMissCount());
	}
	
	public void testAccessOneLineTwice() {
		final ISetAssociativeCacheSimulator c = new SetAssociativeCacheSimulator(6, 6, 8);
		c.handleMemoryAccess(0);
		c.handleMemoryAccess(0);
		assertEquals(1, c.getHitCount());
		assertEquals(1, c.getMissCount());
	}

	public void testAccessAllBytesInOneLine() {
		final ISetAssociativeCacheSimulator c = new SetAssociativeCacheSimulator(6, 6, 8);
		c.handleMemoryAccess(0); // miss
		for (int a=0; a<c.getNumberOfBytesInLine(); a++) {
			c.handleMemoryAccess(a); // hit
		}
		assertEquals(c.getNumberOfBytesInLine(), c.getHitCount());
		assertEquals(1, c.getMissCount());
	}

	public void testAccessEachSetOnce() {
		final ISetAssociativeCacheSimulator c = new SetAssociativeCacheSimulator(6, 6, 8);
		for (int s=0; s<c.getNumberOfSets(); s++) {
			final int a = s<<c.getNumberOfBitsForByteInLine();
			c.handleMemoryAccess(a); // miss			
		}
		assertEquals(0, c.getHitCount());
		assertEquals(c.getNumberOfSets(), c.getMissCount());
	}

	public void testAccessEachSlotInOneSetOnce() {
		final ISetAssociativeCacheSimulator c = new SetAssociativeCacheSimulator(6, 6, 8);
		for (int w=0; w<c.getNumberOfWays(); w++) {
			// use a different tag for each way
			final int tag = w;
			// use same set (set 0) each time
			final int a = tag<<(c.getNumberOfBitsForSet()+c.getNumberOfBitsForByteInLine());
			c.handleMemoryAccess(a); // miss			
		}
		assertEquals(0, c.getHitCount());
		assertEquals(c.getNumberOfWays(), c.getMissCount());
	}

	public void testAccessAllBytesInOneSet() {
		final ISetAssociativeCacheSimulator c = new SetAssociativeCacheSimulator(6, 6, 8);
		for (int b=0; b<c.getNumberOfBytesInLine(); b++) {
			for (int w=0; w<c.getNumberOfWays(); w++) {
				// use a different tag for each way
				final int tag = w;
				// use same set (set 0) each time
				final int a = (tag<<(c.getNumberOfBitsForSet()+c.getNumberOfBitsForByteInLine())) | b;
				final boolean hit = c.handleMemoryAccess(a); // miss or hit
			}
		}
		assertEquals(c.getNumberOfWays(), c.getMissCount());
		assertEquals(c.getNumberOfWays()*(c.getNumberOfBytesInLine()-1), c.getHitCount());
	}

}
