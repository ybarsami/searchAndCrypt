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
        int x;
        BitSequence buffer;
        String result, expResult;
        // Writing 1
        x = 1;
        expResult = "00";
        buffer = new BitSequence();
        instance.writeCode(x, buffer);
        result = buffer.toString();
        assertEquals(expResult, result);
        // Writing 2
        x = 2;
        expResult = "01";
        buffer = new BitSequence();
        instance.writeCode(x, buffer);
        result = buffer.toString();
        assertEquals(expResult, result);
        // Writing 3
        x = 3;
        expResult = "10";
        buffer = new BitSequence();
        instance.writeCode(x, buffer);
        result = buffer.toString();
        assertEquals(expResult, result);
        // Writing 4
        x = 4;
        expResult = "11";
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
        // Trying to write 5
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
        int nbMails = 4;
        MethodBinary instance = new MethodBinary(nbMails);
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
        // Writing and reading 4
        expResult = 4;
        buffer = new BitSequence();
        instance.writeCode(expResult, buffer);
        bitStream = new BitSequenceStream(buffer);
        result = instance.readCode(bitStream);
        assertEquals(expResult, result);
    }

    /**
     * Test of writeCodeBinary method, of class MethodBinary.
     */
    @Test
    public void testWriteCodeBinary() {
        System.out.println("writeCodeBinary");
        int nbBitsToWrite = 2;
        int x;
        BitSequence buffer;
        String result, expResult;
        // Writing 0
        x = 0;
        expResult = "00";
        buffer = new BitSequence();
        MethodBinary.writeCodeBinary(x, buffer, nbBitsToWrite);
        result = buffer.toString();
        assertEquals(expResult, result);
        // Writing 1
        x = 1;
        expResult = "01";
        buffer = new BitSequence();
        MethodBinary.writeCodeBinary(x, buffer, nbBitsToWrite);
        result = buffer.toString();
        assertEquals(expResult, result);
        // Writing 2
        x = 2;
        expResult = "10";
        buffer = new BitSequence();
        MethodBinary.writeCodeBinary(x, buffer, nbBitsToWrite);
        result = buffer.toString();
        assertEquals(expResult, result);
        // Writing 3
        x = 3;
        expResult = "11";
        buffer = new BitSequence();
        MethodBinary.writeCodeBinary(x, buffer, nbBitsToWrite);
        result = buffer.toString();
        assertEquals(expResult, result);
        // Trying to write -1
        x = -1;
        buffer = new BitSequence();
        try {
            MethodBinary.writeCodeBinary(x, buffer, nbBitsToWrite);
            fail("This should not be executed.");
        } catch(AssertionError e) {}
        // Trying to write 4
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
        int nbBits = 2;
        int result, expResult;
        BitSequence buffer;
        BitStream bitStream;
        // Writing and reading 0
        expResult = 0;
        buffer = new BitSequence();
        MethodBinary.writeCodeBinary(expResult, buffer, nbBits);
        bitStream = new BitSequenceStream(buffer);
        result = MethodBinary.readCodeBinary(bitStream, nbBits);
        assertEquals(expResult, result);
        // Writing and reading 1
        expResult = 1;
        buffer = new BitSequence();
        MethodBinary.writeCodeBinary(expResult, buffer, nbBits);
        bitStream = new BitSequenceStream(buffer);
        result = MethodBinary.readCodeBinary(bitStream, nbBits);
        assertEquals(expResult, result);
        // Writing and reading 2
        expResult = 2;
        buffer = new BitSequence();
        MethodBinary.writeCodeBinary(expResult, buffer, nbBits);
        bitStream = new BitSequenceStream(buffer);
        result = MethodBinary.readCodeBinary(bitStream, nbBits);
        assertEquals(expResult, result);
        // Writing and reading 3
        expResult = 3;
        buffer = new BitSequence();
        MethodBinary.writeCodeBinary(expResult, buffer, nbBits);
        bitStream = new BitSequenceStream(buffer);
        result = MethodBinary.readCodeBinary(bitStream, nbBits);
        assertEquals(expResult, result);
    }
    
}
