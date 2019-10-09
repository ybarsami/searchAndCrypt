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
public class CompressionMethodTest {
    
    public CompressionMethodTest() {
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
     * Test of createCompressionMethod method, of class CompressionMethod.
     */
    @Test
    public void testCreateCompressionMethod() {
        System.out.println("createCompressionMethod");
        String indexType;
        int nbMails = 1;
        CompressionMethod expResult, result;
        // Existing compression methods
        indexType = "binary32";
        result = CompressionMethod.createCompressionMethod(indexType, nbMails);
        expResult = new MethodBinary32();
        assertEquals(expResult, result);
        indexType = "binary";
        result = CompressionMethod.createCompressionMethod(indexType, nbMails);
        expResult = new MethodBinary(nbMails);
        assertEquals(expResult, result);
        indexType = "delta";
        result = CompressionMethod.createCompressionMethod(indexType, nbMails);
        expResult = new MethodDelta();
        assertEquals(expResult, result);
        indexType = "gamma";
        result = CompressionMethod.createCompressionMethod(indexType, nbMails);
        expResult = new MethodGamma();
        assertEquals(expResult, result);
        indexType = "interpolative";
        result = CompressionMethod.createCompressionMethod(indexType, nbMails);
        expResult = new MethodInterpolative(nbMails);
        assertEquals(expResult, result);
        indexType = "unary";
        result = CompressionMethod.createCompressionMethod(indexType, nbMails);
        expResult = new MethodUnary();
        assertEquals(expResult, result);
        // Non-existing compression methods
        indexType = "this_method_is_not_supported";
        result = CompressionMethod.createCompressionMethod(indexType, nbMails);
        expResult = null;
        assertEquals(expResult, result);
    }
    
}
