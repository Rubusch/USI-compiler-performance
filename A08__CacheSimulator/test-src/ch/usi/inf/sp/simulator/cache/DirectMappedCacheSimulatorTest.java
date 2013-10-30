package ch.usi.inf.sp.simulator.cache;

import ch.usi.inf.sp.simulator.cache.DirectMappedCacheSimulator;
import ch.usi.inf.sp.simulator.cache.IDirectMappedCacheSimulator;
import junit.framework.TestCase;


public class DirectMappedCacheSimulatorTest extends TestCase {

	public void testInit() {
		final IDirectMappedCacheSimulator c = new DirectMappedCacheSimulator();
		assertEquals(0, c.getHitCount());
		assertEquals(0, c.getMissCount());
	}
	
	public void testOneLineOnce() {
		final IDirectMappedCacheSimulator c = new DirectMappedCacheSimulator();
		c.handleMemoryAccess(0);
		assertEquals(0, c.getHitCount());
		assertEquals(1, c.getMissCount());
	}
	
	public void testAccessOneLineTwice() {
		final IDirectMappedCacheSimulator c = new DirectMappedCacheSimulator();
		c.handleMemoryAccess(0);
		c.handleMemoryAccess(0);
		assertEquals(1, c.getHitCount());
		assertEquals(1, c.getMissCount());
	}

	public void testAccessAllBytesInOneLine() {
		final IDirectMappedCacheSimulator c = new DirectMappedCacheSimulator();
		c.handleMemoryAccess(0); // miss
		for (int a=0; a<c.getNumberOfBytesInLine(); a++) {
			c.handleMemoryAccess(a); // hit
		}
		assertEquals(c.getNumberOfBytesInLine(), c.getHitCount());
		assertEquals(1, c.getMissCount());
	}

	public void testAccessEachLineOnce() {
		final IDirectMappedCacheSimulator c = new DirectMappedCacheSimulator();
		for (int l=0; l<c.getNumberOfLines(); l++) {
			final int a = l<<c.getNumberOfBitsForByteInLine();
			c.handleMemoryAccess(a); // miss			
		}
		assertEquals(0, c.getHitCount());
		assertEquals(c.getNumberOfLines(), c.getMissCount());
	}

	public void testAccessSameLineThroughAliases() {
		final IDirectMappedCacheSimulator c = new DirectMappedCacheSimulator();
		for (int i=0; i<100; i++) {
			final int a = i<<(c.getNumberOfBitsForLine()+c.getNumberOfBitsForByteInLine());
			c.handleMemoryAccess(a); // miss			
		}
		assertEquals(0, c.getHitCount());
		assertEquals(100, c.getMissCount());
	}

}
