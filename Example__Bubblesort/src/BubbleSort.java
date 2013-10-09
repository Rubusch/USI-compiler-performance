/*
 * to be meant as a test example
 */

public class BubbleSort {
	public static void main(String[] args) {
		int []arr = { 5, 1, 4, 2, 3 };

		bubbleSort( arr );

		for( int num : arr){
			System.out.println(num);
		}
		System.out.println("READY.");
	}

	public static void bubbleSort( int[] data){
		for( int k=0; k<data.length-1; ++k){
			boolean isSorted=true;
			for(int i=1; i<data.length-k; ++i){
				if(data[i]<data[i-1]){
					int temp=data[i];
					data[i]=data[i-1];
					data[i-1]=temp;
					isSorted=false;
				}
			}
			if(isSorted){
				break;
			}
		}
	}

}
