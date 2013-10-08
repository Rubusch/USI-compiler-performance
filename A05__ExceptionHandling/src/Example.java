//Note: This class won't compile by design!
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class Example {

	private List<Integer> list;
	private static final int SIZE = 10;

	public static void main( String[] args ){
		Example ex = new Example();

		ex.exp();

		System.out.println( "READY.\n");
	}


/******************************************************************************/

	public void exp(){
		System.out.println("some heading instructions");
/*
		tryitout();
/* /
		try{
			tryitout();
		}catch(Exception e){
			System.out.println("my catch");
		}
/* /
		try{
			tryitout();
		}finally{
			System.out.println("my finally");
		}
/*/
		try{
			tryitout();
		}catch(Exception e){
			System.out.println( "my catch" );
		}finally{
			System.out.println("my finally");
		}
//*/
		System.out.println("some trailing instructions");
	}

	public void tryitout(){
		System.out.println("try it out function");
	}
}
