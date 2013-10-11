
public class RvmScanner {
/*
 * see the source of method
 * org.jikesrvm.mm.mmtk.ScanBootImage.processChunk
 * as reference implementation (rmap)
 * 
 * extract object's class, use method:
 * org.jikesrvm.tools.bootImageViewer.ImageDecoder.getObjectClassDescriptor
 * 
 * first address of boot image data section obtain from
 * org.jikesrvm.tools.bootImageViewer.ImageDecoder.getBootImageDataStartAddress
 * 
 * first address of ramp from
 * org.jikesrvm.tools.bootImageViewer.ImageDecoder.getBootImageRmapStartAddress
 * 
 * load a byte at a given address form the rvm's memory
 * org.jikesrvm.tools.bootImageViewer.Image.loadByte
 * 
 * load reference to byte use
 * org.jikesrvm.tools.bootImageViewer.Image.loadAddress
 */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		org.jikesrvm.tools.bootImageViewer.ImageDecoder.getObjectClassDescriptor();
	}

}
