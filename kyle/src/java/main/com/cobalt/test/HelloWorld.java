package com.cobalt.test;

public class HelloWorld {

	public HelloWorld() {
		
	}
	
	public static String testMethod(String input) throws Exception {
		if (input.contains("test")) {
            subMethod();
            return "This is a test";
        }
        if (input.contains("world"))
            throw new Exception("ZA WARUDO");
		if (input.contains("method"))
			return ("It's a method");
		else if (input.contains("thing"))
			return ("A test of things");
		else
			return "Didn't specify what kind of method it was\n" +
            "Doesn't matter, we tested it anyway.";
	}
    
    public static void subMethod(){
        for (int i = 0; i < 20; i++){
            System.out.print("i = " + i);
        }
    }
}
