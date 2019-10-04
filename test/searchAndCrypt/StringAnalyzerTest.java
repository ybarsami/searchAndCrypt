/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searchAndCrypt;

import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.danishStemmer;
import org.tartarus.snowball.ext.dutchStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.finnishStemmer;
import org.tartarus.snowball.ext.frenchStemmer;
import org.tartarus.snowball.ext.germanStemmer;
import org.tartarus.snowball.ext.hungarianStemmer;
import org.tartarus.snowball.ext.italianStemmer;
import org.tartarus.snowball.ext.norwegianStemmer;
import org.tartarus.snowball.ext.portugueseStemmer;
import org.tartarus.snowball.ext.romanianStemmer;
import org.tartarus.snowball.ext.russianStemmer;
import org.tartarus.snowball.ext.spanishStemmer;
import org.tartarus.snowball.ext.swedishStemmer;
import org.tartarus.snowball.ext.turkishStemmer;

/**
 *
 * @author yann
 */
public class StringAnalyzerTest {
    
    public StringAnalyzerTest() {
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
     * Test of analyzeNewString method, of class StringAnalyzer.
     */
    @Test
    public void testAnalyzeNewString() {
        System.out.println("analyzeNewString");
        StringAnalyzer instance;
        String toBeAnalyzed;
        List<String> expResult, result;
        // English stemming
        instance = new StringAnalyzer("english");
        toBeAnalyzed = "word words";
        expResult = Arrays.asList("word word".split(" "));
        result = instance.analyzeNewString(toBeAnalyzed);
        assertEquals(expResult, result);
        // French stemming
        instance = new StringAnalyzer("french");
        toBeAnalyzed = "mot mots";
        expResult = Arrays.asList("mot mot".split(" "));
        result = instance.analyzeNewString(toBeAnalyzed);
        assertEquals(expResult, result);
    }

    /**
     * Test of getSnowballStemmer method, of class StringAnalyzer.
     */
    @Test
    public void testGetSnowballStemmer() {
        System.out.println("getSnowballStemmer");
        String language;
        SnowballStemmer expResult, result;
        // Existing Stemmers
        language = "danish";
        expResult = new danishStemmer();
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
        language = "dutch";
        expResult = new dutchStemmer();
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
        language = "english";
        expResult = new englishStemmer();
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
        language = "finnish";
        expResult = new finnishStemmer();
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
        language = "french";
        expResult = new frenchStemmer();
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
        language = "german";
        expResult = new germanStemmer();
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
        language = "hungarian";
        expResult = new hungarianStemmer();
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
        language = "italian";
        expResult = new italianStemmer();
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
        language = "norwegian";
        expResult = new norwegianStemmer();
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
        language = "portuguese";
        expResult = new portugueseStemmer();
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
        language = "romanian";
        expResult = new romanianStemmer();
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
        language = "russian";
        expResult = new russianStemmer();
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
        language = "spanish";
        expResult = new spanishStemmer();
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
        language = "swedish";
        expResult = new swedishStemmer();
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
        language = "turkish";
        expResult = new turkishStemmer();
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
        // Non-existing Stemmers
        language = "this_language_is_not_supported";
        expResult = null;
        result = StringAnalyzer.getSnowballStemmer(language);
        assertEquals(expResult, result);
    }
    
}
