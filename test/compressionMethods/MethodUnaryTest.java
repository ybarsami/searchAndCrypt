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
public class MethodUnaryTest {
    
    public MethodUnaryTest() {
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
     * Test of writeCode method, of class MethodUnary.
     */
    @Test
    public void testWriteCode() {
        System.out.println("writeCode");
        MethodUnary instance = new MethodUnary();
        int x;
        BitSequence buffer;
        String result, expResult;
        // Writing 1
        x = 1;
        expResult = "0";
        buffer = new BitSequence();
        instance.writeCode(x, buffer);
        result = buffer.toString();
        assertEquals(expResult, result);
        // Writing 2
        x = 2;
        expResult = "10";
        buffer = new BitSequence();
        instance.writeCode(x, buffer);
        result = buffer.toString();
        assertEquals(expResult, result);
        // Writing 3
        x = 3;
        expResult = "110";
        buffer = new BitSequence();
        instance.writeCode(x, buffer);
        result = buffer.toString();
        assertEquals(expResult, result);
        // Trying to write 0
        x = 0;
        buffer = new BitSequence();
        try {
            instance.writeCode(x, buffer);
            fail("This should not be executed.");
        } catch(AssertionError e) {}
    }

    /**
     * Test of readCode method, of class MethodUnary.
     */
    @Test
    public void testReadCode() {
        System.out.println("readCode");
        MethodUnary instance = new MethodUnary();
        int result, expResult;
        BitSequence buffer;
        BitStream bitStream;
        // Writing and reading 1
        expResult = 1;
        buffer = new BitSequence();
        instance.writeCode(expResult, buffer);
        bitStream = new BitSequenceStream(buffer);
        result = instance.readCode(bitStream);
        assertEquals(expResult, result);
        // Writing and reading 2
        expResult = 2;
        buffer = new BitSequence();
        instance.writeCode(expResult, buffer);
        bitStream = new BitSequenceStream(buffer);
        result = instance.readCode(bitStream);
        assertEquals(expResult, result);
        // Writing and reading 3
        expResult = 3;
        buffer = new BitSequence();
        instance.writeCode(expResult, buffer);
        bitStream = new BitSequenceStream(buffer);
        result = instance.readCode(bitStream);
        assertEquals(expResult, result);
    }

    /**
     * Test of writeCodeUnary method, of class MethodUnary.
     */
    @Test
    public void testWriteCodeUnary() {
        System.out.println("writeCodeUnary");
        int x;
        BitSequence buffer;
        String result, expResult;
        // Writing 1
        x = 1;
        expResult = "0";
        buffer = new BitSequence();
        MethodUnary.writeCodeUnary(x, buffer);
        result = buffer.toString();
        assertEquals(expResult, result);
        // Writing 2
        x = 2;
        expResult = "10";
        buffer = new BitSequence();
        MethodUnary.writeCodeUnary(x, buffer);
        result = buffer.toString();
        assertEquals(expResult, result);
        // Writing 3
        x = 3;
        expResult = "110";
        buffer = new BitSequence();
        MethodUnary.writeCodeUnary(x, buffer);
        result = buffer.toString();
        assertEquals(expResult, result);
        // Trying to write 0
        x = 0;
        buffer = new BitSequence();
        try {
            MethodUnary.writeCodeUnary(x, buffer);
            fail("This should not be executed.");
        } catch(AssertionError e) {}
    }

    /**
     * Test of readCodeUnary method, of class MethodUnary.
     */
    @Test
    public void testReadCodeUnary() {
        System.out.println("readCodeUnary");
        int result, expResult;
        BitSequence buffer;
        BitStream bitStream;
        // Writing and reading 1
        expResult = 1;
        buffer = new BitSequence();
        MethodUnary.writeCodeUnary(expResult, buffer);
        bitStream = new BitSequenceStream(buffer);
        result = MethodUnary.readCodeUnary(bitStream);
        assertEquals(expResult, result);
        // Writing and reading 2
        expResult = 2;
        buffer = new BitSequence();
        MethodUnary.writeCodeUnary(expResult, buffer);
        bitStream = new BitSequenceStream(buffer);
        result = MethodUnary.readCodeUnary(bitStream);
        assertEquals(expResult, result);
        // Writing and reading 3
        expResult = 3;
        buffer = new BitSequence();
        MethodUnary.writeCodeUnary(expResult, buffer);
        bitStream = new BitSequenceStream(buffer);
        result = MethodUnary.readCodeUnary(bitStream);
        assertEquals(expResult, result);
    }
    
}
