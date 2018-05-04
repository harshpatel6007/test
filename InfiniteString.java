package com.harsh.programming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class InfiniteString {
	
	public static void main(String[] args) {
		
		BufferedReader br = null;
		PrintWriter wr = null;
		
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
	         wr = new PrintWriter(System.out);
	         int N = Integer.parseInt(br.readLine().trim());
	         
	         String[] wordsArray = br.readLine().split(" ");
	         
	         int Q = Integer.parseInt(br.readLine().trim());
	         long[] queryArray = new long[Q];
	         for(int i_queryArray=0; i_queryArray<Q; i_queryArray++)
	         {
	         	queryArray[i_queryArray] = Long.parseLong(br.readLine());
	         }

	         char[] out_ = solve(queryArray, wordsArray);
	         for(int i_out_= 0; i_out_<out_.length; i_out_++)
	         {
	         	System.out.println(out_[i_out_]);
	         }
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			wr.close();
	        try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static char[] solve(long[] queryArray, String[] wordsArray) {
		char[] result = new char[queryArray.length];
		String origString = "";
		for(String str : wordsArray) {
			origString += str;
		}
		String finalString = origString + "$" + new StringBuilder(origString).reverse();
		
		int strLength = finalString.length();
		for(int i = 0; i < queryArray.length; i++) {
			long number = queryArray[i];
			if(number > strLength) {
				long k = getKforNumber(number, 2, strLength);
				List<Long> listOfKs = new ArrayList<Long>();
				addListOfKsForNumber(k, listOfKs);
				result[i] = getCharacterAtIndex(number, strLength, finalString, listOfKs);
			} else {
				result[i] = finalString.charAt((int)(number - 1));
			}
		}
		return result;
	}

	private static void printAll(String str, int strLength) {
		String result = str;
		for(int i = 8; i < 174; i++) {
			long k = getKforNumber(i, 2, strLength);
			List<Long> listOfKs = new ArrayList<Long>();
			addListOfKsForNumber(k, listOfKs);
			System.out.println("Get chjar for : " + i + " list : " + listOfKs);
			char ch = getCharacterAtIndex(i, strLength, str, listOfKs);
			result += ch;
		}
		System.out.println(result);
	}

	private static char getCharacterAtIndex(long number, int strLength,
			String str, List<Long> listOfKs) {
		
		char ch = '\u0000';
		int lastcount = 0;
		for(Long KValue : listOfKs) {
			if(number < lastcount + strLength) {
				ch = str.charAt((int)(number - lastcount - 1));
				break;
			}			
			lastcount += strLength;
			if(lastcount == number) {
				ch = str.charAt(0);
				break;
			}
			if(number <= lastcount + KValue) {
				ch = '$';
				break;
			}					
			lastcount += KValue;
			if(lastcount == number) {
				ch = str.charAt(0);
				break;
			}
		}
		if(ch == '\u0000') {
			ch = str.charAt((int)(number - lastcount - 1));
		}
		return ch;
	}

	private static long getKforNumber(long number, long k, long stringLength) {
		long currentLimit = (stringLength * 2) + k;
		if(number <= currentLimit) {
			return k;
		} else {
			return getKforNumber(number, ++k, currentLimit);
		}
	}
	
	private static void addListOfKsForNumber(long k, List<Long> list) {
		if(k == 2) {
			list.add(k);
		} else {
			addListOfKsForNumber(k-1, list);
			list.add(k);
			addListOfKsForNumber(k-1, list);
		}
	}
	
//	public static void main(String[] args) {
//	String str = "123$321";
//	long number = 26;
//	int strLength = str.length();
////	long k = getKforNumber(number, 2, strLength);
////	System.out.println(number + " " + k);
////	List<Long> listOfKs = new ArrayList<Long>();
////	addListOfKsForNumber(k, listOfKs);
////	System.out.println(listOfKs);
////	System.out.println("Character : " + getCharacterAtIndex(number, strLength, str, listOfKs));
//	
//	printAll(str, strLength);
//	
//}
} 

