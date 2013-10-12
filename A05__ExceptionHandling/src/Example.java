//Note: This class won't compile by design!
import java.util.List;

public class Example {
	public static boolean itzok = true;
	public static void main( String[] args ){
		Example ex = new Example();

		ex.test();

		System.out.println( "READY.\n");
	}


/******************************************************************************/

	public void test(){
		System.out.println("some heading instructions");

/*
		if( itzok ){
			System.out.println( "true" );
		}else{
			System.out.println( "else" );
		}
//*/
		
/*
		tryitout();
/*/
		try{
			tryitout();
		}catch(Exception e){
			System.out.println("my catch");
		}
/* /
		try{
			tryitout();
		}finally{
			System.out.println("finally");
		}
/* /
		try{
			tryitout();
		}catch(Exception e){
			System.out.println( "my catch" );
		}finally{
			System.out.println("my finally");
		}
/* /
		try{
			try{
				tryitout();
			}catch(Exception ee){
				ee.printStackTrace();
			}
		}catch(Exception e){
			System.out.println( "my catch" );
		}finally{
			System.out.println("my finally");
		}
/* /
		try{
			tryitout();
			try{
				tryitout();
			}catch(Exception ee){
				System.out.println("inner catch");
			}finally{
				System.out.println("intern finally");
			}
		}catch(Exception e){
			System.out.println( "outer catch" );
		}finally{
			System.out.println("outer finally");
		}
//*/
		System.out.println("some trailing instructions");
	}

	public void tryitout(){
		System.out.println("try it out function");
	}
}
