/*
  prints the current username

  @author: Lothar Rubusch

  compile:
  $ javac ./Username.java

  run:
  $ java Username

  disassemble:
  $ javap -c ./Username.class
*/
public class Username{
    public static void main( String[] args){
        String username = "";
        try{
            username = System.getProperty("user.name");
        }catch(Exception e){
            System.out.println( "FAILED, some exception was thrown" );
            System.exit(0);
        }
        System.out.println( "username '" + username + "'" );
    }
}
