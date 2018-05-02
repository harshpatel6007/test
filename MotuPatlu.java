package com.harsh.hackerearth;

import java.util.Arrays;
import java.util.Scanner;

//https://www.hackerearth.com/practice/algorithms/greedy/basics-of-greedy-algorithms/practice-problems/algorithm/motu-and-patlu-1-ab612ad8/
public class MotuPatlu {

	public static void main(String[] args) {
		
		Scanner sc = null;
		try {
			sc = new Scanner(System.in);			
			int testcases = sc.nextInt();			
			sc.nextLine();
			
			for(int i = 0; i < testcases; i++) {
				int numOfIcecrams = sc.nextInt();
				sc.nextLine();
				int[] icecreamHeights = new int[numOfIcecrams];
				for(int j = 0; j < numOfIcecrams; j++) {
					icecreamHeights[j] = sc.nextInt();
				}
//				System.out.println(Arrays.toString(icecreamHeights));
				printOutput(numOfIcecrams, icecreamHeights);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sc.close();
		}
	}

	private static void printOutput(int numOfIcecreams, int[] icecreamHeights) {
		int motuIceCreamCount = 0;
		//iterate from last element to second element
		for(int index = numOfIcecreams - 1; index > 0; index--) {
			long heightPatlu = icecreamHeights[index];
			long heightMotu = 2 * heightPatlu;
			//iterate from current position of Motu to current position of Patlu
			for(int i = motuIceCreamCount; i < index; i++) {
				if(heightMotu >= icecreamHeights[i]) {
					heightMotu -= icecreamHeights[i];
					motuIceCreamCount++;
				} else {
					icecreamHeights[i] -= heightMotu;
					heightMotu = 0;
				}
				if(heightMotu <= 0) {
					break;
				}
			}
			//Both reach at same Place, increase ice cream count of Motu
			if(motuIceCreamCount == index - 1) {
				motuIceCreamCount++;
			}
			//Motu reach Patlu's position, break
			if(motuIceCreamCount == index) {
				break;
			}
		}
		//Motu doesn't move anywhere
		if(motuIceCreamCount == 0) {
			motuIceCreamCount++;
		}
		
		int patluIceCreamCount = numOfIcecreams - motuIceCreamCount;
		System.out.println(motuIceCreamCount + " " + patluIceCreamCount);
		System.out.println(motuIceCreamCount > patluIceCreamCount ? "Motu" : (motuIceCreamCount == patluIceCreamCount ? "Tie" : "Patlu"));
	}

}
