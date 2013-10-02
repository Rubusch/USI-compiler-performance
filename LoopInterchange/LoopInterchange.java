public class LoopInterchange {
    private final int a = 4000;
    private final int b = 1000;

    private String[][] arr = new String[a][b];

    public LoopInterchange(){
        init();
    }

    private void init(){
        System.out.println("init");
        int timeA = System.nanotime();
/*
        for(int i=0; i<a; i++){
            for(int j=0; j<b; j++){
                arr[i][j] = new String();
                arr[i][j] = String.valueOf(i*j);
            }
        }
/*/
        for(int j=0; j<b; j++){
            for(int i=0; i<a; i++){
                arr[i][j] = new String();
                arr[i][j] = String.valueOf(i*j);
            }
        }
//*/
        int timeB = System.nanotime();

        System.out.println( "diff time: " + (timeB - timeA) );
    }

    public void getVal(){
        System.out.println("2*2 = " + arr[2][2]);
    }

    /////////////////   main()   /////////////////
    public static void main(String[] args){
            LoopInterchange a01 = new LoopInterchange();
//            a01.getVal();

            System.out.println("READY.");
    }
}
