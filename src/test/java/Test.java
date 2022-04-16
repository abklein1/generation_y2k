/*
    File: Test.java
    Author: Alex Klein
    Date: 04/13/2022
    Description: This will be a test for the program

 */

package java;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Test {

    @Test
    public void testDummy(){
        System.out.println("Testing for true");
        String string = "test";
        assertEquals("test", string);
    }


}