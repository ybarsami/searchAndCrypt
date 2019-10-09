package compressionMethods;

import java.util.HashMap;

import org.apache.commons.collections.primitives.ArrayIntList;

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
public class MethodDeltaTest {
    
    public MethodDeltaTest() {
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
     * Test of writeCode method, of class MethodDelta.
     */
    @Test
    public void testWriteCode() {
        System.out.println("writeCode");
        MethodDelta instance = new MethodDelta();
        BitSequence buffer;
        String result, expResult;
        HashMap<Integer, String> map = new HashMap<>();
        map.put( 1, "0");
        map.put( 2, "1000");
        map.put( 3, "1001");
        map.put( 4, "10100");
        map.put( 5, "10101");
        map.put( 6, "10110");
        map.put( 7, "10111");
        map.put( 8, "11000000");
        map.put( 9, "11000001");
        map.put(10, "11000010");
        // Writing 1..10
        for (int x : map.keySet()) {
            expResult = map.get(x);
            buffer = new BitSequence();
            instance.writeCode(x, buffer);
            result = buffer.toString();
            assertEquals(expResult, result);
        }
        int x;
        // Trying to write 0
        x = 0;
        buffer = new BitSequence();
        try {
            instance.writeCode(x, buffer);
            fail("This should not be executed.");
        } catch(AssertionError e) {}
    }

    /**
     * Test of readCode method, of class MethodDelta.
     */
    @Test
    public void testReadCode() {
        System.out.println("readCode");
        MethodDelta instance = new MethodDelta();
        int result, expResult;
        BitSequence buffer;
        BitStream bitStream;
        // Writing and reading 1..42
        for (expResult = 1; expResult < 43; expResult++) {
            buffer = new BitSequence();
            instance.writeCode(expResult, buffer);
            bitStream = new BitSequenceStream(buffer);
            result = instance.readCode(bitStream);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of writeCodeDelta method, of class MethodDelta.
     */
    @Test
    public void testWriteCodeDelta() {
        System.out.println("writeCodeDelta");
        BitSequence buffer;
        String result, expResult;
        HashMap<Integer, String> map = new HashMap<>();
        map.put( 1, "0");
        map.put( 2, "1000");
        map.put( 3, "1001");
        map.put( 4, "10100");
        map.put( 5, "10101");
        map.put( 6, "10110");
        map.put( 7, "10111");
        map.put( 8, "11000000");
        map.put( 9, "11000001");
        map.put(10, "11000010");
        // Writing 1..10
        for (int x : map.keySet()) {
            expResult = map.get(x);
            buffer = new BitSequence();
            MethodDelta.writeCodeDelta(x, buffer);
            result = buffer.toString();
            assertEquals(expResult, result);
        }
        int x;
        // Trying to write 0
        x = 0;
        buffer = new BitSequence();
        try {
            MethodGamma.writeCodeGamma(x, buffer);
            fail("This should not be executed.");
        } catch(AssertionError e) {}
    }

    /**
     * Test of readCodeDelta method, of class MethodDelta.
     */
    @Test
    public void testReadCodeDelta() {
        System.out.println("readCodeDelta");
        int result, expResult;
        BitSequence buffer;
        BitStream bitStream;
        // Writing and reading 1..42
        for (expResult = 1; expResult < 43; expResult++) {
            buffer = new BitSequence();
            MethodDelta.writeCodeDelta(expResult, buffer);
            bitStream = new BitSequenceStream(buffer);
            result = MethodDelta.readCodeDelta(bitStream);
            assertEquals(expResult, result);
        }
    }

    /**
     * Full test to test that the delta code is a bijection.
     */
    @Test
    public void testBijection() {
        System.out.println("bijection");
//        int nbMails = 15747;
        MethodDelta instance = new MethodDelta();
        ArrayIntList mailListInput = new ArrayIntList();
        int[] expResult = new int[] { 84, 85, 510, 941, 946, 965, 978, 1008, 1009, 1774, 1862, 2248, 2254, 2755, 2756, 3494, 3495, 3716, 4428, 4462, 4676, 5218, 5219, 5430, 5455, 5470, 6007, 6229, 6408, 6467, 6500, 6601, 6654, 6850, 7757, 8261, 8262, 8263, 8264, 8265, 8324, 8359, 8423, 8438, 8808, 9413, 9739, 9885, 10512, 10766, 10842, 10962, 11124, 11140, 11141, 11188, 11222, 11780, 12146, 12148, 12415, 12455, 12456, 12644, 12736, 13643, 14131, 14153, 14172, 14239, 14240, 14250, 14254, 14262, 14596, 14860, 15032, 15033, 15042, 15043, 15428 };
        int nbMailsInput = expResult.length;
        for (int i = 0; i < nbMailsInput; i++) {
            mailListInput.add(expResult[i]);
        }
        BitSequence bitSequence = instance.bitSequenceOfMailList(mailListInput);
        BitSequenceStream bitSequenceStream = new BitSequenceStream(bitSequence);
        ArrayIntList mailListOutput = instance.readMailList(bitSequenceStream, nbMailsInput);
        int nbMailsOutput = mailListOutput.size();
        int[] result = new int[nbMailsOutput];
        for (int i = 0; i < nbMailsOutput; i++) {
            result[i] = mailListOutput.get(i);
        }
        assertArrayEquals(expResult, result);
    }
    
}
