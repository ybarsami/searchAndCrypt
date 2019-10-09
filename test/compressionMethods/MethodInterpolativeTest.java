/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compressionMethods;

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
public class MethodInterpolativeTest {
    
    public MethodInterpolativeTest() {
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
    
    private ArrayIntList intArray2arrayIntList(int[] intArray) {
        ArrayIntList arrayIntList = new ArrayIntList();
        for (int i = 0; i < intArray.length; i++) {
            arrayIntList.add(intArray[i]);
        }
        return arrayIntList;
    }

    /**
     * Test of bitSequenceOfMailList method, of class MethodInterpolative.
     */
    @Test
    public void testBitSequenceOfMailList() {
        System.out.println("bitSequenceOfMailList");
        String expResult, result;
        int nbMails;
        MethodInterpolative instance;
        ArrayIntList mailList;
        int[] mailArray;
        BitSequence bitSequence;
        
        // { 3, 8, 9 } in [1..10]
        nbMails = 10;
        instance = new MethodInterpolative(nbMails);
        mailArray = new int[] { 3, 8, 9 };
        mailList = intArray2arrayIntList(mailArray);
        bitSequence = instance.bitSequenceOfMailList(mailList);
        result = bitSequence.toString();
        // 8 is the pointer n°1 in the 3-pointer list, we hence code 8 in [1 + 1; 10 - (3 - 1 - 1)],
        // i.e. 8 in [2; 9] -> we need to code 8-2 = 6 on ceiling(log_2(9-2+1)) = 3 bits -> 110.
        // We now code:
        //     - { 3 } in the range [1..7] (010)
        //     - and { 9 } in the range [9..10] -> { 1 } in the range [1..2] -> (0)
        expResult = "110" + "010" + "0";
        assertEquals(expResult, result);
        
        // { 1, 2, 6 } in [1..9]
        nbMails = 9;
        instance = new MethodInterpolative(nbMails);
        mailArray = new int[] { 1, 2, 6 };
        mailList = intArray2arrayIntList(mailArray);
        bitSequence = instance.bitSequenceOfMailList(mailList);
        result = bitSequence.toString();
        // 2 is the pointer n°1 in the 3-pointer list, we hence code 2 in [1 + 1; 9 - (3 - 1 - 1)],
        // i.e. 2 in [2; 8] -> we need to code 2-2 = 0 on ceiling(log_2(8-2+1)) = 3 bits -> 000.
        // We now code:
        //     - { 1 } in the range [1..1] ()
        //     - and { 6 } in the range [3..9] -> { 4 } in the range [1..7] -> (011)
        expResult = "000" + "" + "011";
        assertEquals(expResult, result);
        
        // { 3, 8, 9, 11, 12, 13, 17 } in [1..20]
        nbMails = 20;
        instance = new MethodInterpolative(nbMails);
        mailArray = new int[] { 3, 8, 9, 11, 12, 13, 17 };
        mailList = intArray2arrayIntList(mailArray);
        bitSequence = instance.bitSequenceOfMailList(mailList);
        result = bitSequence.toString();
        // 11 is the pointer n°3 in the 7-pointer list, we hence code 11 in [1 + 3; 20 - (7 - 3 - 1)],
        // i.e. 11 in [4; 17] -> we need to code 11-4 = 7 on ceiling(log_2(17-4+1)) = 4 bits -> 0111.
        // We now code:
        //     - { 3, 8, 9 } in the range [1..10] (1100100)
        //     - and { 12, 13, 17 } in the range [12..20] -> { 1, 2, 6 } in the range [1..9]
        expResult = "0111" + "1100100" + "000011";
        assertEquals(expResult, result);
        
        // Trying to convert list with identifiers bigger than nbMails
        nbMails = 4;
        instance = new MethodInterpolative(nbMails);
        mailArray = new int[] { 3, 8, 9, 11, 12, 13, 17 };
        mailList = intArray2arrayIntList(mailArray);
        try {
            bitSequence = instance.bitSequenceOfMailList(mailList);
            fail("This should not be executed.");
        } catch(AssertionError e) {}
        
        // Trying to convert unsorted list
        nbMails = 20;
        instance = new MethodInterpolative(nbMails);
        mailArray = new int[] { 3, 8, 9, 11, 12, 17, 13 };
        mailList = intArray2arrayIntList(mailArray);
        try {
            bitSequence = instance.bitSequenceOfMailList(mailList);
            fail("This should not be executed.");
        } catch(AssertionError e) {}
    }

    /**
     * Test of readMailList method, of class MethodInterpolative.
     */
    @Test
    public void testReadMailList() {
        System.out.println("readMailList");
        int[] expResult, result;
        int nbMails = 20;
        MethodInterpolative instance = new MethodInterpolative(nbMails);
        BitSequence bitSequence = new BitSequence();
        // "01111100100000011"
        bitSequence.append(false);
        bitSequence.append(true, 5);
        bitSequence.append(false, 2);
        bitSequence.append(true);
        bitSequence.append(false, 6);
        bitSequence.append(true, 2);
        expResult = new int[] { 3, 8, 9, 11, 12, 13, 17 };
        int expNbMailsLocal = 7;
        ArrayIntList mailList = instance.readMailList(bitSequence, expNbMailsLocal);
        int nbMailsLocal = mailList.size();
        result = new int[nbMailsLocal];
        for (int i = 0; i < nbMailsLocal; i++) {
            result[i] = mailList.get(i);
        }
        assertArrayEquals(expResult, result);
    }

    /**
     * Full test to test that the interpolative code is a bijection.
     */
    @Test
    public void testBijection() {
        System.out.println("bijection");
        int nbMails = 15747;
        MethodInterpolative instance = new MethodInterpolative(nbMails);
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
