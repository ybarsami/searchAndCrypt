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
public class MethodBinaryTest {
    
    public MethodBinaryTest() {
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
     * Test of writeCode method, of class MethodBinary.
     */
    @Test
    public void testWriteCode() {
        System.out.println("writeCode");
        int nbMails = 4;
        MethodBinary instance = new MethodBinary(nbMails);
        BitSequence buffer;
        String result, expResult;
        HashMap<Integer, String> map = new HashMap<>();
        map.put(1, "00");
        map.put(2, "01");
        map.put(3, "10");
        map.put(4, "11");
        // Writing 1..4
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
        // Trying to write 5 (not possible on ceiling(log_2(4)) = 2 bits)
        x = 5;
        buffer = new BitSequence();
        try {
            instance.writeCode(x, buffer);
            fail("This should not be executed.");
        } catch(AssertionError e) {}
    }

    /**
     * Test of readCode method, of class MethodBinary.
     */
    @Test
    public void testReadCode() {
        System.out.println("readCode");
        int nbMails = 32;
        MethodBinary instance = new MethodBinary(nbMails);
        int result, expResult;
        BitSequence buffer;
        BitInputStreamArray bitInputStream;
        // Writing and reading 1..nbMails
        for (expResult = 1; expResult <= nbMails; expResult++) {
            buffer = new BitSequence();
            instance.writeCode(expResult, buffer);
            bitInputStream = new BitInputStreamArray(buffer);
            result = instance.readCode(bitInputStream);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of writeCodeBinary method, of class MethodBinary.
     */
    @Test
    public void testWriteCodeBinary() {
        System.out.println("writeCodeBinary");
        int nbBitsToWrite = 2;
        BitSequence buffer;
        String result, expResult;
        HashMap<Integer, String> map = new HashMap<>();
        map.put(0, "00");
        map.put(1, "01");
        map.put(2, "10");
        map.put(3, "11");
        // Writing 0..3
        for (int x : map.keySet()) {
            expResult = map.get(x);
            buffer = new BitSequence();
            MethodBinary.writeCodeBinary(x, buffer, nbBitsToWrite);
            result = buffer.toString();
            assertEquals(expResult, result);
        }
        int x;
        // Trying to write -1
        x = -1;
        buffer = new BitSequence();
        try {
            MethodBinary.writeCodeBinary(x, buffer, nbBitsToWrite);
            fail("This should not be executed.");
        } catch(AssertionError e) {}
        // Trying to write 4 (not possible on 2 bits)
        x = 4;
        buffer = new BitSequence();
        try {
            MethodBinary.writeCodeBinary(x, buffer, nbBitsToWrite);
            fail("This should not be executed.");
        } catch(AssertionError e) {}
    }

    /**
     * Test of readCodeBinary method, of class MethodBinary.
     */
    @Test
    public void testReadCodeBinary() {
        System.out.println("readCodeBinary");
        int nbBits = 5;
        int result, expResult;
        BitSequence buffer;
        BitInputStreamArray bitInputStream;
        // Writing and reading 0..31
        for (expResult = 0; expResult < 32; expResult++) {
            buffer = new BitSequence();
            MethodBinary.writeCodeBinary(expResult, buffer, nbBits);
            bitInputStream = new BitInputStreamArray(buffer);
            result = MethodBinary.readCodeBinary(bitInputStream, nbBits);
            assertEquals(expResult, result);
        }
    }

    /**
     * Full test to test that the binary code is a bijection.
     */
    @Test
    public void testBijection() {
        System.out.println("bijection");
        int nbMails = 15747;
        MethodBinary instance = new MethodBinary(nbMails);
        ArrayIntList mailListInput = new ArrayIntList();
        int[] expResult = new int[] { 84, 85, 510, 941, 946, 965, 978, 1008, 1009, 1774, 1862, 2248, 2254, 2755, 2756, 3494, 3495, 3716, 4428, 4462, 4676, 5218, 5219, 5430, 5455, 5470, 6007, 6229, 6408, 6467, 6500, 6601, 6654, 6850, 7757, 8261, 8262, 8263, 8264, 8265, 8324, 8359, 8423, 8438, 8808, 9413, 9739, 9885, 10512, 10766, 10842, 10962, 11124, 11140, 11141, 11188, 11222, 11780, 12146, 12148, 12415, 12455, 12456, 12644, 12736, 13643, 14131, 14153, 14172, 14239, 14240, 14250, 14254, 14262, 14596, 14860, 15032, 15033, 15042, 15043, 15428 };
        int nbMailsInput = expResult.length;
        for (int i = 0; i < nbMailsInput; i++) {
            mailListInput.add(expResult[i]);
        }
        BitSequence bitSequence = instance.bitSequenceOfMailList(mailListInput);
        ArrayIntList mailListOutput = instance.readMailList(bitSequence, nbMailsInput);
        int nbMailsOutput = mailListOutput.size();
        int[] result = new int[nbMailsOutput];
        for (int i = 0; i < nbMailsOutput; i++) {
            result[i] = mailListOutput.get(i);
        }
        assertArrayEquals(expResult, result);
    }
    
}
