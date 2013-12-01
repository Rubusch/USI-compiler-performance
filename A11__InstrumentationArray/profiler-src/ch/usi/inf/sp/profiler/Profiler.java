package ch.usi.inf.sp.profiler;

public class Profiler {
	public static void log(String message){
		System.out.println(message);
	}

	/*
	 * IMPORTANT: the order of the arguments has to be arg1: int, arg2: String / rest
	 * 
	 * 
	 * setup on the operand stack:
	 * 
	 *  1. duplicate the NEWARRAY count operand: DUP
	 *  2. push the String onto the stack: LDC
	 *  3. keep the order in the function signature
	 *
	 *                                     2)
	 *                                     +--------+
	 *                   1)                | String |
	 *                   +-------+         +--------+
	 *             DUP   | count |   LDC   | count  |
	 * +-------+   ==>   +-------+   ==>   +--------+
	 * | count |         | count |         | count  |
	 * +-------+         +-------+         +--------+
	 * | (...) |         | (...) |         | (...)  |
	 * 
	 * 
	 * 3) func( int count, String arg )
	 * 
	 */
	public static void logNewArray( int size, String message ){
		System.out.println(message + String.valueOf( size ));
	}

	public static void logANewArray( int size, String message ){
		System.out.println(message + String.valueOf( size ));
	}

//	public static void logMultiANewArray( int[] sizes, String message, String dimensions ){
//	public static void logMultiANewArray( int[] sizes, String dimensions, String message ){
	
//	public static void logMultiANewArray( String dimensions, int[] sizes, String message ){

//	public static void logMultiANewArray( String dimensions){ // , int[] sizes, String message ){ // ok
//	public static void logMultiANewArray( String dimensions, int[] sizes){ // , String message ){ // TODO
	public static void logMultiANewArray( String dimensions, String message ){
		for( int idx=0; idx<Integer.valueOf(dimensions); ++idx){
			System.out.print("XXX, ");
		}
		System.out.println("");

		System.out.println("DEBUG: MULTIANEWARRAY called!!!");
	}

	/*
	 * DEBUG
	 */
	public static void DEBUG_opstack(int val){
		System.out.println("DEBUG '" + val + '"');
	}
}
