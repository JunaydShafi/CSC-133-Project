package pkgTTTBackend;

import org.junit.jupiter.api.Test;

import java.util.Scanner;

class SlIOManagerTest {
    private static Scanner myScanner = new Scanner(System.in);

    public static void main(String[] my_args) {
        System.out.println("Hello, ULT's!");
    }  //  public static void main(String[] my_args)

    private static boolean returnTestResult(final String testLabel, final boolean testVar) {
        if (testVar) {
            System.out.printf("%10s: PASS\n", testLabel);
        } else {
            System.out.printf("%10s: FAIL\n", testLabel);
        }  //  if (testVar)
        return testVar;
    }  //  private static boolean returnTestResult(...)


    // writing to screen
    private static boolean ULT_1() {
        boolean retVal = false;


        retVal = true;
        return returnTestResult("ULT_1", retVal);
    }  //  private static boolean ULT_1(...)

}