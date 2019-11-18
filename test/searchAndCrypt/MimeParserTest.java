package searchAndCrypt;

import java.io.File;
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
public class MimeParserTest {
    
    public MimeParserTest() {
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
     * Test of parseMimeMessage method, of class MimeParser.
     */
    @Test
    public void testParseMimeMessage() {
        System.out.println("parseMimeMessage");
        String absoluteFolderName = new File(ServerLocal.MAILS_TEST1.getFolderName()).getAbsolutePath();
        File file = new File(absoluteFolderName + File.separatorChar + "inbox" + File.separatorChar + "25");
        String expResult = "Thu Oct 25 21:04:35 CEST 2001\n" +
                "renee.ratcliff@enron.com\n" +
                "k..allen@enron.com\n" +
                "Distribution Form\n" +
                "Phillip,\n" +
                "\n" +
                "Pursuant to your request, please see the attached.\n" +
                "\n" +
                "Thanks,\n" +
                "\n" +
                "Renee\n" +
                "\n" +
                "  \n" +
                "";
        String result = MimeParser.parseMimeMessage(file);
        assertEquals(expResult, result);
    }
    
}
