package me.dayanath.jboy;

public class Test {

	public static void main(String[] args) {
		long start = System.nanoTime();
		Z80 z80 = new Z80();
		//for(int i=0;i<1000000;i++)	
		//System.out.println((System.nanoTime() - start)/1000000d);
		byte b = (byte) 254;
		System.out.println(b);
	}

}
