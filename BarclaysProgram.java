package logic;

import java.lang.reflect.Array;

public class BarclaysProgram {

	public static void main(String[] args) {
		
		int[] array = {1,2,4,6,7,5,3,8,9,10};
		System.out.println(getMaxArraySize(array));
	}

	private static int getMaxArraySize(int[] array) {
		int lastMax = array[0];
		int count = 1;
		
		for(int i = 0; i < array.length; i++) {
			if(lastMax < array[i]) {
				count++;
				lastMax = array[i];
			}
			
			/* if(count == 1) {
				lastMax = array[i];
			} */
		}
		return count ;
	}
}
