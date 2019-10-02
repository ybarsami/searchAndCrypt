/**
 * Analyze a meaningful part of a message.
 */

package searchAndCrypt;

import java.util.List;
import java.util.function.Function;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.tartarus.snowball.SnowballStemmer;

/**
 *
 * @author yann
 */
public class StringAnalyzer {
    
    @FunctionalInterface
    interface WordStemmer extends Function<String, String> {}
    
    private final WordStemmer wordStemmer;
    
    /**
     * Creates a new instance of StringAnalyzer.
     */
    public StringAnalyzer(String language) {
        SnowballStemmer stemmer = getSnowballStemmer(language);
        wordStemmer = (stemmer == null)
                ? word -> word
                : word -> {
                    stemmer.setCurrent(word);
                    return stemmer.stem() ? stemmer.getCurrent() : null;
                };
    }
    
    /*
     * We have a new string to analyze.
     *
     * @param toBeIndexed, the string to be analyzed.
     */
    public List<String> analyzeNewString(String toBeAnalyzed) {
        return Pattern.compile(" +")
            .splitAsStream(StringNormalizer.normalize(toBeAnalyzed))
            .map(wordStemmer)
            .filter(word -> word != null)
            .collect(Collectors.toList());
    }
    
    /*
     * Sets a language to be used by the stemmer.
     *
     * @param language, the String representing the language on which the
     * stemmer will work.
     * Valid language Strings are:
     * danish, dutch, english, finnish, french, german, hungarian, italian,
     * norwegian, porter, portuguese, romanian, russian, spanish, swedish,
     * turkish.
     */
    public static final SnowballStemmer getSnowballStemmer(String language) {
        try {
            Class stemClass = Class.forName("org.tartarus.snowball.ext." +
                    language + "Stemmer");
            return (SnowballStemmer) stemClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            System.out.println(language + " is not a valid language for SnowballStemmer.");
            System.out.println("Valid languages are: danish, dutch, english, finnish, french, german, hungarian, italian, norwegian, porter, portuguese, romanian, russian, spanish, swedish, turkish.");
        }
        return null;
    }
}
