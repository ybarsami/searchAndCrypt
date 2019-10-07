package searchAndCrypt;

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
public class StringNormalizerTest {
    
    public StringNormalizerTest() {
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
     * Test of normalize method, of class StringNormalizer.
     */
    @Test
    public void testNormalize() {
        System.out.println("normalize");
        String str, expResult, result;
        // Capitals
        str       = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        expResult = "abcdefghijklmnopqrstuvwxyz";
        result = StringNormalizer.normalize(str);
        assertEquals(expResult, result);
        // Non-capitals
        str       = "abcdefghijklmnopqrstuvwxyz";
        expResult = "abcdefghijklmnopqrstuvwxyz";
        result = StringNormalizer.normalize(str);
        assertEquals(expResult, result);
        // Accents
        str       = "éÉèÈçÇàÀùÙâÂêÊîÎôÔûÛäÄëËïÏöÖüÜ";
        expResult = "eeeeccaauuaaeeiioouuaaeeiioouu";
        result = StringNormalizer.normalize(str);
        assertEquals(expResult, result);
        // Double letters
        str       = "œŒ";
        expResult = "oeoe";
        result = StringNormalizer.normalize(str);
        assertEquals(expResult, result);
        // Numerics
        str       = "1234567890";
        expResult = "1234567890";
        result = StringNormalizer.normalize(str);
        assertEquals(expResult, result);
        // Non-alphanumerics
        str       = "&~#'{([-|`_^@)]°=}+$£%*µ<>,?;.:/!§";
        expResult = "                                  ";
        result = StringNormalizer.normalize(str);
        assertEquals(expResult, result);
        // Special characters in non-alphanumerics
        str       = "\"\\";
        expResult = "  ";
        result = StringNormalizer.normalize(str);
        assertEquals(expResult, result);
        // Real-life test
        str       = "C'est-à-dire que les œufs";
        expResult = "c est a dire que les oeufs";
        result = StringNormalizer.normalize(str);
        assertEquals(expResult, result);
    }
    
}
