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
public class MethodByElementTest {
    
    public MethodByElementTest() {
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
     * Test of gapList method, of class MethodByElement.
     */
    @Test
    public void testGapList() {
        System.out.println("gapList");
        int[] expResult, result;
        ArrayIntList mailList, gapList;
        int[] mailArray;
        // Generic sorted mail array
        mailArray = new int[] { 3, 8, 9, 11, 12, 13, 17 };
        mailList = new ArrayIntList();
        for (int i = 0; i < mailArray.length; i++) {
            mailList.add(mailArray[i]);
        }
        expResult = new int[] { 3, 5, 1, 2, 1, 1, 4 };
        gapList = MethodByElement.gapList(mailList);
        int nbGaps = gapList.size();
        result = new int[nbGaps];
        for (int i = 0; i < nbGaps; i++) {
            result[i] = gapList.get(i);
        }
        assertArrayEquals(expResult, result);
        // Non-sorted mail array
        mailArray = new int[] { 3, 8, 9, 12, 13, 17, 11 };
        mailList = new ArrayIntList();
        for (int i = 0; i < mailArray.length; i++) {
            mailList.add(mailArray[i]);
        }
        try {
            gapList = MethodByElement.gapList(mailList);
            fail("This should not be executed.");
        } catch(AssertionError e) {}
    }
    
}
