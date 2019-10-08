package compressionMethods;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author yann
 */
public class IntegerToolsTest {
    
    public IntegerToolsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of byte2int method, of class IntegerTools.
     */
    @Test
    public void testByte2int() {
        System.out.println("byte2int");
        byte b;
        int expResult, result;
        // x >= 0 is conserved
        b = 0;
        expResult = 0;
        result = IntegerTools.byte2int(b);
        assertEquals(expResult, result);
        // x < 0 is mirrored
        b = -1;
        expResult = 255;
        result = IntegerTools.byte2int(b);
        assertEquals(expResult, result);
    }

    /**
     * Test of ceilingLog2 method, of class IntegerTools.
     */
    @Test
    public void testCeilingLog2() {
        System.out.println("ceilingLog2");
        int x, expResult, result;
        // log_2(1) = 0
        x = 1;
        expResult = 0;
        result = IntegerTools.ceilingLog2(x);
        assertEquals(expResult, result);
        // log_2(2) = 1
        x = 2;
        expResult = 1;
        result = IntegerTools.ceilingLog2(x);
        assertEquals(expResult, result);
        // log_2(3) = 1.584962501
        x = 3;
        expResult = 2;
        result = IntegerTools.ceilingLog2(x);
        assertEquals(expResult, result);
        // log_2(0) = undefined
        x = 0;
        try {
            result = IntegerTools.ceilingLog2(x);
            fail("This should not be executed.");
        } catch(AssertionError e) {}
    }

    /**
     * Test of ceilingDivision method, of class IntegerTools.
     */
    @Test
    public void testCeilingDivision() {
        System.out.println("ceilingDivision");
        int x, y, expResult, result;
        // ceiling(8/5) = 2
        x = 8;
        y = 5;
        expResult = 2;
        result = IntegerTools.ceilingDivision(x, y);
        assertEquals(expResult, result);
        // 8/0 = undefined
        x = 8;
        y = 0;
        try {
            result = IntegerTools.ceilingDivision(x, y);
            fail("This should not be executed.");
        } catch(AssertionError e) {}
        // -XXX/YYY = undefined
        x = -42;
        y = 8;
        try {
            result = IntegerTools.ceilingDivision(x, y);
            fail("This should not be executed.");
        } catch(AssertionError e) {}
    }

    /**
     * Test of ilog2 method, of class IntegerTools.
     */
    @Test
    public void testIlog2() {
        System.out.println("ilog2");
        int x, expResult, result;
        // log_2(1) = 0
        x = 1;
        expResult = 0;
        result = IntegerTools.ilog2(x);
        assertEquals(expResult, result);
        // log_2(2) = 1
        x = 2;
        expResult = 1;
        result = IntegerTools.ilog2(x);
        assertEquals(expResult, result);
        // log_2(3) = 1.584962501
        x = 3;
        expResult = 1;
        result = IntegerTools.ilog2(x);
        assertEquals(expResult, result);
        // log_2(0) = mathematically undefined, but in the implementation, defined as -1
        x = 0;
        expResult = -1;
        result = IntegerTools.ilog2(x);
        assertEquals(expResult, result);
        // log_2(-1) = undefined
        x = -1;
        try {
            result = IntegerTools.ilog2(x);
            fail("This should not be executed.");
        } catch(AssertionError e) {}
    }
    
}
