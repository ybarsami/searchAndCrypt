package compressionMethods;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
public class MethodBinary32Test {
    
    public MethodBinary32Test() {
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
     * Full test to test that the binary32 code is a bijection.
     */
    @Test
    public void testBijection() {
        System.out.println("bijection");
//        int nbMails = 15747;
        MethodBinary32 instance = new MethodBinary32();
        ArrayIntList mailListInput = new ArrayIntList();
        int[] expResult = new int[] { 84, 85, 510, 941, 946, 965, 978, 1008, 1009, 1774, 1862, 2248, 2254, 2755, 2756, 3494, 3495, 3716, 4428, 4462, 4676, 5218, 5219, 5430, 5455, 5470, 6007, 6229, 6408, 6467, 6500, 6601, 6654, 6850, 7757, 8261, 8262, 8263, 8264, 8265, 8324, 8359, 8423, 8438, 8808, 9413, 9739, 9885, 10512, 10766, 10842, 10962, 11124, 11140, 11141, 11188, 11222, 11780, 12146, 12148, 12415, 12455, 12456, 12644, 12736, 13643, 14131, 14153, 14172, 14239, 14240, 14250, 14254, 14262, 14596, 14860, 15032, 15033, 15042, 15043, 15428 };
        int nbMailsInput = expResult.length;
        for (int i = 0; i < nbMailsInput; i++) {
            mailListInput.add(expResult[i]);
        }
        String fileName = "tmpTest.txt";
        try (FileOutputStream fout = new FileOutputStream(fileName);
                DataOutputStream out = new DataOutputStream(fout)) {
            out.writeInt(nbMailsInput);
            instance.writeMailList(out, mailListInput);
        } catch (IOException e) {
            fail("IOException when testing.");
        }
        try (FileInputStream fin = new FileInputStream(fileName);
                DataInputStream in = new DataInputStream(fin)) {
            int nbMailsOutput = in.readInt();
            int[] result = new int[nbMailsOutput];
            ArrayIntList mailListOutput = instance.readMailList(in, nbMailsInput);
            for (int i = 0; i < nbMailsOutput; i++) {
                result[i] = mailListOutput.get(i);
            }
            assertArrayEquals(expResult, result);
        } catch (FileNotFoundException e) {
            fail("Test file not found when testing.");
        } catch (IOException e) {
            fail("IOException when testing.");
        }
        new File(fileName).delete();
    }
    
}
