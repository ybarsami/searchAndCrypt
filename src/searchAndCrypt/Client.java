/**
 * Global API that a client should follow in order to use the functionalities
 * of this library.
 */

package searchAndCrypt;

import java.io.File;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author yann
 */
public class Client {
    
    private final Server server;
    private final Request request;
    private GlobalIndex globalIndex;
    private StringAnalyzer stringAnalyzer;
    
    // Number of mails we add to the index before sending a chunk of index to
    // the server.
    private final int nbMailsBeforeSave = 512;
    
    // The index has to be compressed by a specific integer-compressing method
    // to save memory, thus speed when downloading / uploading the index.
    // This library comes with a number of techniques which can be used by
    // choosing one of the following values for indexType: "binary32", "binary",
    // "delta", "gamma", or "interpolative", see GlobalIndex.importFromFile.
    // See CompressionMethod.java and other files inside the package
    // compressionMethods for more details.
    private final String indexType = "delta";
    
    /**
     * Creates a new instance of Client.
     */
    public Client(Server server) {
        this.server = server;
        stringAnalyzer = new StringAnalyzer(server.getLanguage());
        request = new Request(server, stringAnalyzer);
        globalIndex = new GlobalIndex();
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Interactions with server.
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * Sets a language to be used by the stemmer. If the language is different
     * from the one stored on the server, we have to re-index everything.
     *
     * @param language, the String representing the language on which the
     * stemmer will work.
     * Valid language strings are:
     * danish, dutch, english, finnish, french, german, hungarian, italian,
     * norwegian, porter, portuguese, romanian, russian, spanish, swedish,
     * turkish.
     */
    public final void setLanguage(String language) {
        if (server.setLanguage(language)) {
            stringAnalyzer = new StringAnalyzer(language);
            updateIndex(true);
        }
    }
    
    /*
     * Export the full compressed index to the server.
     */
    public void exportToFile() {
        String tmpIndexFilename = "tmpIndex.txt";
        File tmpIndexFile = globalIndex.exportToFile(tmpIndexFilename, indexType);
        server.updateIndexFile(tmpIndexFile);
        tmpIndexFile.delete();
    }
    
    /*
     * Export a chunk of the compressed index to the server.
     */
    public void exportToChunk() {
        String tmpIndexFilename = "tmpIndex.txt";
        File tmpIndexFile = globalIndex.exportToFile(tmpIndexFilename, indexType);
        server.addChunkedIndexFile(tmpIndexFile, globalIndex.nbMails);
        tmpIndexFile.delete();
    }
    
    /*
     * Download e-mails from the server and add them in the index.
     *
     * @param startFromScratch, a boolean telling whether to index:
     *            > in case of true, all e-mails
     *            > in case of false, only e-mails not yet indexed.
     */
    public void updateIndex(boolean startFromScratch) {
        // Clear the index.
        globalIndex = new GlobalIndex();
        int nbIndexedMessages = 0;
        
        int maxIdIndexedMail = startFromScratch
                ? 0
                : server.getMaxIdIndexedMail(); // Get the maximum identifier of an e-mail indexed on the server.
        // Get all new mails from the server and index them.
        final TreeSet<Integer> allMessageIdentifiers = server.getAllMessageIdentifiers(maxIdIndexedMail);
        for (int i : allMessageIdentifiers) {
            String toBeAnalyzed = MimeParser.parseMimeMessage(server.getMessage(i));
            Set<String> toBeIndexed = stringAnalyzer.analyzeNewString(toBeAnalyzed);
            for (String word: toBeIndexed) {
                globalIndex.updateWithNewWord(word, i);
            }
            nbIndexedMessages++;
            // Export a chunk of the index.
            if (nbIndexedMessages == nbMailsBeforeSave) {
                exportToChunk();
                globalIndex = new GlobalIndex();
                nbIndexedMessages = 0;
            }
        }
        // Export a chunk of the index.
        if (nbIndexedMessages > 0) {
            exportToChunk();
        }
        
        // Merge the chunks of the index.
        while (server.getNbIndexChunks() > 1) {
            List<File> chunks12 = server.getTwoChunksIndex();
            File merged = globalIndex.importAndMerge(chunks12.get(0), chunks12.get(1));
            server.addChunkedIndexFile(merged, Server.NOT_A_NEW_CHUNK);
        }
        
        // Store the full index on the server.
        server.replaceIndexWithLastChunk();
    }
    
    /*
     * Download the e-mails from the server that are not yet indexed, and
     * add them in the index.
     */
    public void updateIndex() {
        updateIndex(false);
    }
    
    /*
     * Import the compressed index from the server.
     */
    public void loadIndex() {
        File indexFile = server.getIndexFile();
        globalIndex.importFromFile(indexFile);
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
