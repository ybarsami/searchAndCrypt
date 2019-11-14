package compressionMethods;

import java.util.Collections;

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
public class BitSequenceTest {
    
    public BitSequenceTest() {
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
     * Test of nbBits method, of class BitSequence.
     */
    @Test
    public void testNbBits() {
        System.out.println("nbBits");
        BitSequence instance;
        int expResult, result;
        // Empty bit sequence
        instance = new BitSequence();
        expResult = 0;
        result = instance.nbBits();
        assertEquals(expResult, result);
        // Any bit sequence
        instance = new BitSequence();
        expResult = 42;
        for (int i = 0; i < expResult; i++) {
            instance.append(true);
        }
        result = instance.nbBits();
        assertEquals(expResult, result);
    }

    /**
     * Test of get method, of class BitSequence.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        BitSequence instance;
        boolean expResult, result;
        // Empty sequence
        instance = new BitSequence();
        try {
            instance.get(0);
            fail("This should not be executed.");
        } catch(IndexOutOfBoundsException e) {}
        // Value "11111111"
        instance = new BitSequence();
        instance.append(true, IntegerTools.nbBitsPerByte);
        for (int i = 0; i < IntegerTools.nbBitsPerByte; i++) {
            expResult = true;
            result = instance.get(0);
            assertEquals(expResult, result);
        }
        // Value "10101010010101"
        instance = new BitSequence();
        for (int i = 0; i < 4; i++) {
            instance.append(true);
            instance.append(false);
        }
        for (int i = 0; i < 3; i++) {
            instance.append(false);
            instance.append(true);
        }
        for (int i = 0; i < 4; i++) {
            expResult = true;
            result = instance.get(2 * i);
            assertEquals(expResult, result);
            expResult = false;
            result = instance.get(2 * i + 1);
            assertEquals(expResult, result);
        }
        for (int i = 0; i < 3; i++) {
            expResult = false;
            result = instance.get(8 + 2 * i);
            assertEquals(expResult, result);
            expResult = true;
            result = instance.get(8 + 2 * i + 1);
            assertEquals(expResult, result);
        }
        try {
            instance.get(-1);
            fail("This should not be executed.");
        } catch(IndexOutOfBoundsException e) {}
        try {
            instance.get(14);
            fail("This should not be executed.");
        } catch(IndexOutOfBoundsException e) {}
    }

    /**
     * Test of toByteArray method, of class BitSequence.
     */
    @Test
    public void testToByteArray() {
        System.out.println("toByteArray");
        BitSequence instance;
        byte[] expResult, result;
        // Empty sequence
        instance = new BitSequence();
        expResult = new byte[0];
        result = instance.toByteArray();
        assertArrayEquals(expResult, result);
        // Value more than 128
        instance = new BitSequence();
        instance.append(true, IntegerTools.nbBitsPerByte);
        expResult = new byte[1];
        expResult[0] = (byte)255;
        result = instance.toByteArray();
        assertArrayEquals(expResult, result);
        // Big endian
        instance = new BitSequence();
        instance.append(true, 2);
        expResult = new byte[1];
        expResult[0] = (byte)(128 + 64);
        result = instance.toByteArray();
        assertArrayEquals(expResult, result);
        // General case
        instance = new BitSequence();
        for (int i = 0; i < 4; i++) {
            instance.append(true);
            instance.append(false);
        }
        for (int i = 0; i < 3; i++) {
            instance.append(false);
            instance.append(true);
        }
        expResult = new byte[2];
        expResult[0] = (byte)(128 + 32 + 8 + 2);
        expResult[1] = (byte)(64 + 16 + 4);
        result = instance.toByteArray();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of append method, of class BitSequence.
     */
    @Test
    public void testAppend_boolean() {
        System.out.println("append(boolean)");
        boolean value;
        BitSequence instance;
        String before, after;
        // Append true
        value = true;
        instance = new BitSequence();
        for (int i = 0; i < 3; i++) {
            instance.append(false);
            instance.append(true);
        }
        before = instance.toString();
        instance.append(value);
        after = instance.toString();
        assertEquals(before + "1", after);
        // Append false
        value = false;
        instance = new BitSequence();
        for (int i = 0; i < 3; i++) {
            instance.append(true);
            instance.append(true);
            instance.append(false);
        }
        before = instance.toString();
        instance.append(value);
        after = instance.toString();
        assertEquals(before + "0", after);
        // Maximum number of append
        value = false;
        instance = new BitSequence();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            instance.append(value);
        }
        // Too much append
        try {
            instance.append(value);
            fail("This should not be executed.");
        } catch(IndexOutOfBoundsException e) {}
    }

    /**
     * Test of append method, of class BitSequence.
     */
    @Test
    public void testAppend_boolean_int() {
        System.out.println("append(boolean, int)");
        int nbPositions;
        boolean value;
        BitSequence instance;
        String before, after;
        // Append true
        value = true;
        nbPositions = 2;
        instance = new BitSequence();
        for (int i = 0; i < 3; i++) {
            instance.append(false);
            instance.append(true);
        }
        before = instance.toString();
        instance.append(value, nbPositions);
        after = instance.toString();
        assertEquals(before + String.join("", Collections.nCopies(nbPositions, "1")), after);
        // Append false
        value = false;
        nbPositions = 5;
        instance = new BitSequence();
        for (int i = 0; i < 3; i++) {
            instance.append(true);
            instance.append(true);
            instance.append(false);
        }
        before = instance.toString();
        instance.append(value, nbPositions);
        after = instance.toString();
        assertEquals(before + String.join("", Collections.nCopies(nbPositions, "0")), after);
        // Maximum number of append
        value = false;
        nbPositions = Integer.MAX_VALUE;
        instance = new BitSequence();
        instance.append(value, nbPositions);
        // Too much append
        try {
            instance.append(value);
            fail("This should not be executed.");
        } catch(IndexOutOfBoundsException e) {}
    }

    /**
     * Test of toString method, of class BitSequence.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        BitSequence instance;
        String expResult, result;
        // Empty sequence
        instance = new BitSequence();
        expResult = "";
        result = instance.toString();
        assertEquals(expResult, result);
        // Value more than 128
        instance = new BitSequence();
        instance.append(true, IntegerTools.nbBitsPerByte);
        expResult = "11111111";
        result = instance.toString();
        assertEquals(expResult, result);
        // Big endian
        instance = new BitSequence();
        instance.append(true, 2);
        expResult = "11";
        result = instance.toString();
        assertEquals(expResult, result);
        // General case
        instance = new BitSequence();
        for (int i = 0; i < 4; i++) {
            instance.append(true);
            instance.append(false);
        }
        for (int i = 0; i < 3; i++) {
            instance.append(false);
            instance.append(true);
        }
        expResult = "10101010010101";
        result = instance.toString();
        assertEquals(expResult, result);
    }
    
}
