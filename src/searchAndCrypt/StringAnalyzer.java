/**
 * Analyze a meaningful part of a message.
 */

package searchAndCrypt;

import java.util.Set;
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
                    // stemmer.stem() is a boolean, but, quoting Olly Betts:
                    // "The return value is just the last signal from the
                    // Snowball program, so if it's false then execution didn't
                    // reach the very end of the `stem` function.  But for the
                    // current stemmers, that doesn't indicate an error (or
                    // indeed anything at all)."
                    // So we can just discard the return boolean value.
                    stemmer.stem();
                    return stemmer.getCurrent();
                };
    }
    
    /*
     * We have a new string to analyze.
     *
     * @param toBeIndexed, the string to be analyzed.
     * @return the set of stemmed words contained in this string.
     */
    public Set<String> analyzeNewString(String toBeAnalyzed) {
        return Pattern.compile(" +")
            .splitAsStream(StringNormalizer.normalize(toBeAnalyzed))
            .map(wordStemmer)
            .filter(word -> !word.equals(""))
            .collect(Collectors.toSet());
    }
    
    /*
     * Creates a stemmer according to the chosen language.
     *
     * @param language, the String representing the language on which the
     * stemmer will work.
     * Valid language Strings are:
     * danish, dutch, english, finnish, french, german, hungarian, italian,
     * norwegian, porter, portuguese, romanian, russian, spanish, swedish,
     * turkish.
     * @return the SnowballStemmer of this language.
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
