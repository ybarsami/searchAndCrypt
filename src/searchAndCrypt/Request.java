/**
 * Request.java
 *
 * Created on 14 févr. 2019
 */

package searchAndCrypt;

import java.io.File;

import java.util.ArrayList;
import java.util.TreeSet;

import org.tartarus.snowball.SnowballStemmer;

/**
 *
 * @author yann
 */
public class Request {
    
    public final static int bandwidthDownload = 600000; // Bandwidth, in bytes / second.
    public final static int bandwidthUpload   = 200000; // Bandwidth, in bytes / second.
    
    private Server server;
    
    // Stem words. Default is no stemmer.
    private SnowballStemmer stemmer = null;
    
    /**
     * Creates a new instance of Request.
     */
    public Request(Server server) {
        this.server = server;
    }
    
    public void setStemmer(SnowballStemmer stemmer) {
        this.stemmer = stemmer;
    }
    
    /*
     * Finds in the e-mails the ones that match the request. The request is a
     * String which is normalized with Tools.normalize, then is split by spaces,
     * and the actual query performed is an AND on the different words that
     * appear in the split.
     *
     * @param request, the request written by the user.
     * @return the list of e-mails that match the request.
     */
    public ArrayList<File> search(String request) {
        // TESTING: time to transfer files from server
        double executionTime = 0.;
        
        // Normalize the String.
        request = Tools.normalize(request);
        
        // Get rid of empty requests.
        if (request.length() == 0) {
            return new ArrayList<>();
        }
        
        // Split the String into words.
        String[] splitLine = request.split(" ");
        
        // Get rid of requests that contains only words removed by the stemmer.
        // e.g., some stemmers also have a list of stop-words.
        boolean isValidRequest = false;
        for (String word: splitLine) {
            // Gives the word to the stemmer.
            stemmer.setCurrent(word);
            if (stemmer.stem()) {
                isValidRequest = true;
            }
        }
        if (!isValidRequest) {
            return new ArrayList<>();
        }
        
        GlobalIndex globalIndex = new GlobalIndex();
        String indexType = "delta";
        File indexFile = server.getIndexFile(indexType);
        globalIndex.importFromFile(indexFile, indexType);
        // TESTING: time to transfer files from server
        executionTime += (double) indexFile.length() / (double) bandwidthDownload;
        System.out.println("Downloading the global index took " + executionTime + " seconds.");
        
        // For each stemmed word from the request, put its inverted list.
        ArrayList<TreeSet<Integer>> potentialEmails = new ArrayList<>();
        
        for (String word: splitLine) {
            // Gives the word to the stemmer.
            stemmer.setCurrent(word);
            if (stemmer.stem()) {
                word = stemmer.getCurrent();
                
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
            // TESTING: time to transfer files from server
            /*
            double timeDownloadEmail = (double) email.length() / (double) bandwidthDownload;
            executionTime += timeDownloadEmail;
            System.out.println("Downloading the mail index " + idMessage + " ( " + email.length() +
                    " bytes) took " + timeDownloadEmail + " seconds.");
            System.out.println("Total execution time for now is " + executionTime + " seconds.");
            */
        }
        
        // TESTING: time to transfer files from server
        System.out.println("Request for '" + request + "' took " + executionTime + " seconds.");
        return answers;
    }

}
