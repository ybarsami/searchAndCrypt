/**
 * Analyze a meaningful part of a message.
 */

package searchAndCrypt;

import java.util.ArrayList;
import java.util.List;

import org.tartarus.snowball.SnowballStemmer;

/**
 *
 * @author yann
 */
public class StringAnalyzer {
    
    // Stem words. Default is no stemmer.
    private SnowballStemmer stemmer = null;
    
    public void setStemmer(SnowballStemmer stemmer) {
        this.stemmer = stemmer;
    }
    
    
    /*
     * We have a new string to analyze.
     *
     * @param toBeIndexed, the string to be analyzed.
     */
    public List<String> analyzeNewString(String toBeAnalyzed) {
        List<String> words = new ArrayList<>();
        
        // Normalize the String.
        String normalizedString = Tools.normalize(toBeAnalyzed);
        
        // Split the String into words (separated by spaces).
        String[] strings = normalizedString.split("\\s+");
        
        for (String str: strings) {
            // Strip long http(s):// sub-strings (usually weird stuff inside).
            String word = Tools.removeLongURL(str);
            
            // Give the word to the stemmer, and add the result to the list of
            // words that will be returned.
            if (stemmer == null) {
                // No stemmer.
                words.add(word);
                
            } else {
                // SnowBall stemmer.
                stemmer.setCurrent(word);
                if (stemmer.stem()) {
                    word = stemmer.getCurrent();
                    words.add(word);
                }
            }
        }
        
        return words;
    }

}
