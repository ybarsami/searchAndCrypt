/**
 * Global API that a client should follow in order to use the functionalities
 * of this library.
 */

package searchAndCrypt;

import java.io.File;

import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author yann
 */
public class Client {
    
    private final Server server;
    private final StringAnalyzer stringAnalyzer;
    private final Request request;
    private GlobalIndex globalIndex;
    
    /**
     * Creates a new instance of Client.
     */
    public Client(Server server, String indexType) {
        this.server = server;
        stringAnalyzer = new StringAnalyzer(server.getLanguage());
        request = new Request(server, indexType, stringAnalyzer);
        globalIndex = new GlobalIndex();
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Interactions with local data.
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * Export the index to the server, using the compression scheme specified
     * in indexType.
     *
     * @param indexType, the String representing the compression scheme for the
     * index.
     * For valid indexType Strings, see GlobalIndex.exportToFile.
     */
    public void exportToFile(String indexType) {
        String tmpIndexFilename = "tmpIndex.txt";
        File tmpIndexFile = globalIndex.exportToFile(tmpIndexFilename, indexType);
        server.updateIndexFile(tmpIndexFile, indexType);
        tmpIndexFile.delete();
    }
    
    /*
     * Export the index to the server, using the compression scheme specified
     * in indexType.
     *
     * @param indexType, the String representing the compression scheme for the
     * index.
     * For valid indexType Strings, see GlobalIndex.exportToFile.
     */
    public void exportToChunk(String indexType) {
        String tmpIndexFilename = "tmpIndex.txt";
        File tmpIndexFile = globalIndex.exportToFile(tmpIndexFilename, indexType);
        server.addChunkedIndexFile(tmpIndexFile, indexType, globalIndex.nbMails);
        tmpIndexFile.delete();
    }
    
    public static int nbMailsBeforeSave = 512; //1000000;
    
    /*
     * Download the e-mails from the server that are not yet indexed, and
     * add them in the index.
     */
    public void indexEverything(Server server, String indexType) {
        // Clear the index.
        globalIndex = new GlobalIndex();
        int nbIndexedMessages = 0;
        
        // Get the maximum identifier of an e-mail indexed on the server.
        int maxIdIndexedMail = server.getMaxIdIndexedMail();
        // Get all new mails from the server and index them.
        final TreeSet<Integer> allMessageIdentifiers = server.getAllMessageIdentifiers(maxIdIndexedMail);
        for (int i : allMessageIdentifiers) {
            String toBeAnalyzed = MimeParser.parseMimeMessage(server.getMessage(i));
            List<String> toBeIndexed = stringAnalyzer.analyzeNewString(toBeAnalyzed);
            for (String word: toBeIndexed) {
                globalIndex.updateWithNewWord(word, i);
            }
            nbIndexedMessages++;
            // Export a chunk of the index.
            if (nbIndexedMessages == nbMailsBeforeSave) {
                exportToChunk(indexType);
                globalIndex = new GlobalIndex();
                nbIndexedMessages = 0;
            }
        }
        // Export a chunk of the index.
        if (nbIndexedMessages > 0) {
            exportToChunk(indexType);
        }
        
        // Merge the chunks of the index.
        while (server.getNbIndexChunks() > 1) {
            List<File> chunks12 = server.getTwoChunksIndex(indexType);
            File merged = globalIndex.importAndMerge(chunks12.get(0), chunks12.get(1), indexType);
            server.addChunkedIndexFile(merged, indexType, Server.NOT_A_NEW_CHUNK);
        }
        
        // Store the full index on the server.
        server.replaceIndexWithLastChunk(indexType);
        
        // TEST PURPOSES ONLY
        boolean hasToExportASCII = false;
        if (hasToExportASCII) {
            File indexFile = server.getIndexFile(indexType);
            globalIndex.importFromFile(indexFile, indexType);
            globalIndex.exportToFileASCII("test_ASCII.txt");
        }
    }
    
    /*
     * Import the index from the server, using the compression scheme specified
     * in indexType.
     *
     * @param indexType, the String representing the compression scheme for the
     * index.
     * For valid indexType Strings, see GlobalIndex.importFromFile.
     */
    public void loadIndex(String indexType) {
        File indexFile = server.getIndexFile(indexType);
        globalIndex.importFromFile(indexFile, indexType);
    }
    
    
    /*
     * Finds in the e-mails the ones that match the request. The request is a
     * String which is normalized with Tools.normalize, then is split by spaces,
     * and the actual query performed is an AND on the different words that
     * appear in the split.
     *
     * @param requestedString, the request written by the user.
     * @return the list of e-mails that match the request.
     */
    public List<File> search(String requestedString) {
        return request.search(requestedString);
    }
    
}
