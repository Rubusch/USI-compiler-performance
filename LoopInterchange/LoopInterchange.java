public class LoopInterchange {
    private final int maxRows = 4000;
    private final int maxCols = 1000;

    private String[][] arr = new String[maxRows][maxCols];

    public LoopInterchange(){
        init();
    }

    private void init(){

    	System.out.println("start");
        long timeStart = System.nanoTime();
        // the following makes a difference, since a regular compiler can, but will not optimize this, typically
//*
        for(int i=0; i<maxRows; i++){
            for(int j=0; j<maxCols; j++){
                arr[i][j] = new String();
                arr[i][j] = String.valueOf(i*j);
            }
        }
/*/
        for(int j=0; j<maxCols; j++){
            for(int i=0; i<maxRows; i++){
                arr[i][j] = new String();
                arr[i][j] = String.valueOf(i*j);
            }
        }
//*/
        long timeEnd = System.nanoTime();

        System.out.println("end");
        long timeDiff = (timeEnd - timeStart)/1000000;

        System.out.println( "diff time: " + timeDiff + " ms\n");
    }

    public void getVal(){
        System.out.println("2*2 = " + arr[2][2]);
    }



    public static void main(String[] args){
    	try{
            LoopInterchange a01 = new LoopInterchange();
            LoopInterchange a02 = new LoopInterchange();
            LoopInterchange a03 = new LoopInterchange();
            LoopInterchange a04 = new LoopInterchange();
            LoopInterchange a05 = new LoopInterchange();
    	}catch(OutOfMemoryError exp){
    		System.out.println("FAILED");
    		exp.printStackTrace();
    	}
//            a01.getVal();

            System.out.println("\nREADY.");
    }
}
