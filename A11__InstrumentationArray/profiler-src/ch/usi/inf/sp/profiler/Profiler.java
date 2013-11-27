package ch.usi.inf.sp.profiler;

public class Profiler {
	public static void log(String message){
		System.out.println(message);
	}

	public static void logNewArray( int size, String message ){
//	public static void logNewArray( String message, int size ){
		System.out.println(message + String.valueOf( size ));
	}

	// TODO
	public static void logANewArray(  int size, String message ){
		System.out.println(message + ", " + String.valueOf( size ));
	}

	// TODO
	public static void logMultiANewArray( int size, String message ){
		System.out.println(message + ", " + String.valueOf( size ));
	}
}
