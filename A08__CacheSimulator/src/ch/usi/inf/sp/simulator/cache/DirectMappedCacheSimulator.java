package ch.usi.inf.sp.simulator.cache;


/**
 * An implementation of a direct-mapped cache simulator.
 * 
 * @deprecated Write your own subclass of SetAssociativeCacheSimulator instead.
 * @author Matthias.Hauswirth@usi.ch
 */
public final class DirectMappedCacheSimulator implements IDirectMappedCacheSimulator {

	private final int bitsForLine;
	private final int bitsForByteInLine;
	private final int[] tags;
	private final boolean[] validBits;
	
	private long hitCount;
	private long missCount;
	

	/**
	 * bitsForLine = 9;       // 2^9 == 512 lines
	 * bitsForByteInByte = 6; // 2^6 ==  64 byte line size
	 */
	public DirectMappedCacheSimulator() {
		this(9, 6);
	}

	public DirectMappedCacheSimulator(final int bitsForLine, final int bitsForByteInLine) {
		this.bitsForLine = bitsForLine;
		this.bitsForByteInLine = bitsForByteInLine;
		final int numberOfLines = 1<<bitsForLine;
		tags = new int[numberOfLines];
		validBits = new boolean[numberOfLines];
	}
	
	public int getNumberOfBitsForTag() {
		return 32-bitsForLine-bitsForByteInLine;
	}
	
	public int getNumberOfBitsForLine() {
		return bitsForLine;
	}
	
	public int getNumberOfBitsForByteInLine() {
		return bitsForByteInLine;
	}
	
	public int getNumberOfLines() {
		return 1<<bitsForLine;
	}
	
	public int getNumberOfBytesInLine() {
		return 1<<bitsForByteInLine;
	}
	
	public void handleMemoryAccess(final int address) {
		System.out.printf("address: 0x%08x (%d)\n", address, address);

//		Tester.db("\taddress\t>> bitsForByteInLine"); // XXX
//		Tester.db("\t" + String.valueOf(address) + "\t>> " + String.valueOf(bitsForByteInLine) + "\t\t\t\t\t\t\t" + String.valueOf(address>>bitsForByteInLine)); // XXX

//		Tester.db("\t1<< bitsForLine"); // XXX
//		Tester.db("\t1<< "+ String.valueOf(bitsForLine) + "\t\t\t\t\t\t\t\t" + String.valueOf(1<<bitsForLine)); // XXX

		final int line = (address>>bitsForByteInLine)&((1<<bitsForLine)-1);

		Tester.db("\t(address\t>> bitsForByteInLine)\t& ((1<<bitsForLine)-1)");
		Tester.db("\t(" + String.valueOf(address) + "\t>> " + String.valueOf(bitsForByteInLine) + ")\t\t\t& ((" + String.valueOf(1<<bitsForLine) + ")-1)\t\t" + String.valueOf(line)); // XXX

		System.out.printf("line:    0x%08x (%d)\n", line, line);

//		Tester.die(); // XXX

		final int tag = address>>>(bitsForLine+bitsForByteInLine);
		
		Tester.db("\taddress\t>>>(bitsForLine + bitsForByteInLine)");
		Tester.db("\t" + String.valueOf(address) + "\t>>>(" + String.valueOf(bitsForLine) + " + " + String.valueOf(bitsForByteInLine) + ")\t\t\t\t\t\t" + String.valueOf(tag));

		System.out.printf("tag:     0x%08x (%d)\n", tag, tag);

		if (tags[line]==tag && validBits[line]) {
			hitCount++;
		} else {
			tags[line] = tag;
			validBits[line] = true;
			missCount++;
		}

		System.out.println("-");
	}
	
	public long getHitCount() {
		return hitCount;
	}
	
	public long getMissCount() {
		return missCount;
	}
	
}
