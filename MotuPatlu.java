package com.harsh.java.practice.hackerEarth;

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
				System.out.println(Arrays.toString(icecreamHeights));
				printOutput(numOfIcecrams, icecreamHeights);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			sc.close();
		}
	}

	private static void printOutput(int numOfIcecreams, int[] icecreamHeights) {
		int reachedIndex = 0;
		int i = 0;
		
		for(i = numOfIcecreams - 1; i >= 0; i--) {
			long heightPatlu = icecreamHeights[i];
			long heightMotu = 2 * heightPatlu;
			
			for(int j = reachedIndex; j < i; j++) {
				if(heightMotu > 0) {
					if(icecreamHeights[j] > heightMotu) {
						icecreamHeights[j] -= heightMotu;					
					} else {
						heightMotu -= icecreamHeights[j];
						reachedIndex++;
					}		
				}						
			}
			
			if(reachedIndex == (i - 1)) {
				break;
			}
						
		}
		
		System.out.println(reachedIndex + " " + i);
	}

}
