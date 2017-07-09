package com.lafois.domo.daikin;

public class BinaryUtils {

	public static String reverseBits(String binaryString) {
		String[] bytes = binaryString.split(" ");
		
		StringBuffer result = new StringBuffer();
		
		for(int i = 0; i < bytes.length; i++) {
		
			int hexValue = Integer.parseInt(bytes[i], 16);
			String bin = Integer.toBinaryString(hexValue);
			Long l = new Long(bin);
			
			bin = String.format("%1$08d", l);
			String newValue = new StringBuffer(bin).reverse().toString();
			int newHexValue = Integer.parseInt(newValue, 2);
			
			result.append(String.format("%1$02x ", newHexValue));
		}
		return result.toString();
	}
	
	public static void main(String[] args) {
		//String s1 = "88 5b e4 00 00 92 3c 00 0a 00 00 60 06 00 00 83 01 00 71";
		String s1 = "0a fa";
		
		String reversedS1 = reverseBits(s1);
		
		System.out.println(reversedS1);
		
		int[] byteReversedS1 = stringToBytes(reversedS1);
	
		computeCS(byteReversedS1);
	}

	private static void computeCS(int[] byteReversedS1) {
		int cs = 0;
		for(int i = 0; i < byteReversedS1.length - 1; i++) {
			cs += byteReversedS1[i];
		}
		
		System.out.print(Integer.toHexString(cs & 0xff));
	}

	private static int[] stringToBytes(String reversedS1) {
		String[] bytes = reversedS1.split(" ");
		int[] byteArray = new int[bytes.length];
		
		for(int i = 0; i < bytes.length; i++) {
			byteArray[i] = Integer.parseInt(bytes[i], 16);
		}
		
		return byteArray;
	}

	public static int[] stringToByteArray(String reversed) {
		
		String[] bytes = reversed.split(" ");
		
		int[] returnArray = new int[bytes.length];
		
		for(int i = 0; i < bytes.length; i++) {
			returnArray[i] = Integer.parseInt(bytes[i], 16);
		}
		
		return returnArray;
	}

	///////////////////////////////////////////////////////

	public static String bytesToString(int[] frame) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < frame.length; i++) {
			sb.append(String.format("%1$02x ", frame[i]));
		}
		return sb.toString();
	}

	///////////////////////////////////////////////////////

}
