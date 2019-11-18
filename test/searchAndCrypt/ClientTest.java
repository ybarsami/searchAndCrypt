package searchAndCrypt;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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
public class ClientTest {
    
    public ClientTest() {
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
     * Test of exportToFile method, of class Client.
     */
    @Test
    public void testExportToFile() {
        System.out.println("exportToFile");
        Server server;
        Client instance;
        // allen-p dataset
        server = new ServerLocal(ServerLocal.MAILS_TEST1);
        instance = new Client(server);
//        instance.exportToFile();
    }

    /**
     * Test of exportToChunk method, of class Client.
     */
    @Test
    public void testExportToChunk() {
        System.out.println("exportToChunk");
        Server server;
        Client instance;
        // allen-p dataset
        server = new ServerLocal(ServerLocal.MAILS_TEST1);
        instance = new Client(server);
//        instance.exportToChunk();
    }

    /**
     * Test of updateIndex method, of class Client.
     */
    @Test
    public void testUpdateIndex() {
        System.out.println("updateIndex");
        Server server;
        Client instance;
        // allen-p dataset
        server = new ServerLocal(ServerLocal.MAILS_TEST1);
        instance = new Client(server);
        instance.updateIndex();
        File indexFile = server.getIndexFile();
        GlobalIndex globalIndex = new GlobalIndex();
        globalIndex.importFromFile(indexFile);
        File result = globalIndex.exportToFileASCII("test_ASCII.txt");
        File expResult = new File("test" + File.separatorChar + "allen-p_ASCII.txt");
        try {
            List<String> original = Files.readAllLines(result.toPath(), StandardCharsets.UTF_8);
            List<String> revised = Files.readAllLines(expResult.toPath(), StandardCharsets.UTF_8);

            // Compute diff. Get the Patch object. Patch is the container for computed deltas.
            Patch<String> patch = DiffUtils.diff(original, revised);
            assertEquals(patch.getDeltas().size(), 0);
        } catch (IOException e) {
            fail("IOException when testing.");
        } catch (DiffException e) {
            fail("DiffException when testing.");
        }
        result.delete();
    }

    /**
     * Test of loadIndex method, of class Client.
     */
    @Test
    public void testLoadIndex() {
        System.out.println("loadIndex");
        Server server;
        Client instance;
        // allen-p dataset
        server = new ServerLocal(ServerLocal.MAILS_TEST1);
        instance = new Client(server);
//        instance.loadIndex();
    }

    /**
     * Test of search method, of class Client.
     */
    @Test
    public void testSearch() {
        System.out.println("search");
        String requestedString, absoluteFolderName;
        Server server;
        Client instance;
        List<File> expResult, result;
        // allen-p dataset
        server = new ServerLocal(ServerLocal.MAILS_TEST1);
        instance = new Client(server);
        File indexFile = server.getIndexFile();
        if (!indexFile.exists()) {
            instance.updateIndex();
        }
        absoluteFolderName = new File(ServerLocal.MAILS_TEST1.getFolderName()).getAbsolutePath();
        requestedString = "diminish";
        expResult = new ArrayList<>();
        expResult.add(new File(absoluteFolderName + File.separatorChar + "discussion_threads" + File.separatorChar + "548"));
        result = instance.search(requestedString);
        assertEquals(expResult, result);
        requestedString = "hello";
        expResult = new ArrayList<>();
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "114"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "133"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "147"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "169"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "266"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "268"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "381"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "43"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "446"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "deleted_items" + File.separatorChar + "71"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "discussion_threads" + File.separatorChar + "46"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "discussion_threads" + File.separatorChar + "47"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "inbox" + File.separatorChar + "42"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "inbox" + File.separatorChar + "65"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "inbox" + File.separatorChar + "67"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "inbox" + File.separatorChar + "71"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "sent" + File.separatorChar + "766"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "sent" + File.separatorChar + "767"));
        expResult.add(new File(absoluteFolderName + File.separatorChar + "sent_items" + File.separatorChar + "199"));
        result = instance.search(requestedString);
        assertEquals(expResult, result);
    }
    
}
