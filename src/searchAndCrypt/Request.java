/**
 * Basic request.
 */

package searchAndCrypt;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author yann
 */
public class Request {
    
    private final Server server;
    private final String indexType;
    private final StringAnalyzer stringAnalyzer;
    
    /**
     * Creates a new instance of Request.
     */
    public Request(Server server, String indexType, StringAnalyzer stringAnalyzer) {
        this.server = server;
        this.indexType = indexType;
        this.stringAnalyzer = stringAnalyzer;
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
        List<String> requestedWords = stringAnalyzer.analyzeNewString(requestedString);
        
        // Get rid of empty requests.
        if (requestedWords.isEmpty()) {
            return new ArrayList<>();
        }
        
        GlobalIndex globalIndex = new GlobalIndex();
        File indexFile = server.getIndexFile(indexType);
        globalIndex.importFromFile(indexFile, indexType);
        
        // For each stemmed word from the request, put its inverted list.
        ArrayList<TreeSet<Integer>> potentialEmails = new ArrayList<>();
        
        for (String word: requestedWords) {
            // Check if this word is in the index.
            GlobalEntry globalEntry = globalIndex.find(word);
            
            if (globalEntry == null) {
                // If this word is not in the index, we can already answer.
                return new ArrayList<>();
                
            } else {
                // If it was in the index, we get its associated inverted list.
                potentialEmails.add(globalEntry.toTreeSet());
            }
        }
        
        // Intersection of the inverted lists.
        TreeSet<Integer> blocAnswers = potentialEmails.get(0);
        for (int i = 1; i < potentialEmails.size(); i++) {
            blocAnswers.retainAll(potentialEmails.get(i));
        }
        
        // Get the answers.
        ArrayList<File> answers = new ArrayList<>();
        
        // Add the mails to the final answer.
        for (Integer idMessage: blocAnswers) {
            final File email = server.getMessage(idMessage);
            answers.add(email);
        }
        
        return answers;
    }

}
