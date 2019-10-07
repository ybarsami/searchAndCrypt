package searchAndCrypt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
public class RequestTest {
    
    public RequestTest() {
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
     * Test of search method, of class Request.
     */
    @Test
    public void testSearch() {
        System.out.println("search");
        String requestedString, absoluteFolderName;
        Request instance;
        List<File> expResult, result;
        // allen-p dataset
        instance = new Request(
                new ServerLocal(ServerLocal.MAILS_TEST1),
                "delta",
                new StringAnalyzer("english"));
        absoluteFolderName = new File(ServerLocal.MAILS_TEST1.getFolderName()).getAbsolutePath();
        requestedString = "diminish";
        expResult = new ArrayList<>();
        expResult.add(new File(absoluteFolderName + File.separatorChar + "discussion_threads" + File.separatorChar + "548."));
        result = instance.search(requestedString);
        assertEquals(expResult, result);
        requestedString = "hello";
        expResult = new ArrayList<>();
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "114."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "133."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "147."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "169."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "266."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "268."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "381."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "43."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "446."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "71."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "discussion_threads" + File.separatorChar + "46."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "discussion_threads" + File.separatorChar + "47."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "inbox" + File.separatorChar + "42."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "inbox" + File.separatorChar + "65."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "inbox" + File.separatorChar + "67."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "inbox" + File.separatorChar + "71."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "sent" + File.separatorChar + "766."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "sent" + File.separatorChar + "767."));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "sent_items" + File.separatorChar + "199."));
        result = instance.search(requestedString);
        assertEquals(expResult, result);
    }
    
}
