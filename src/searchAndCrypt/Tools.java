/**
 * Integer and string tools.
 */

package searchAndCrypt;

import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 * @author yann
 */
public class Tools {
    
    ////////////////////////////////////////////////////////////////////////////
    // Integer utils
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * Converts a signed byte b (-127 <= b <= 128) to an unsigned int.
     * (if the byte is negative, adds 256).
     */
    public static int byte2int(byte b) {
        return b < 0 ? (int)b + 256 : (int)b;
    }
    
    /*
     * Returns the ceiling of log2(x).
     * x must be >= 1.
     */
    public static int ceilingLog2(int x) {
        return ilog2(x - 1) + 1;
    }
    
    /*
     * Returns the ceiling of x / y.
     */
    public static int ceilingDivision(int x, int y) {
        return (x + y - 1) / y;
    }
    
    /*
     * Returns the floor of log2(x) if x >= 1 ; return -1 if x == 0 (this allows
     * the formula for ceilingLog2(x) = ilog2(x - 1) + 1).
     * x must be >= 0.
     */
    public static int ilog2(int x) {
        return 31 - Integer.numberOfLeadingZeros(x);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Number representation utils
    ////////////////////////////////////////////////////////////////////////////
    
    // In BitSetWithLastPosition, we output the bytes in big endian.
    // This is the "reverse" function, that reads a byte and output an array of
    // 8 bits.
    public static void readByteFromFile(DataInputStream in, int[] currentBits) {
        try {
            boolean isBigEndian = true;
            int currentByte = byte2int(in.readByte());
            for (int i = 0; i < nbBitsPerByte; i++) {
                currentBits[isBigEndian ? nbBitsPerByte - 1 - i : i] = currentByte % 2;
                currentByte /= 2;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static final int nbBitsPerByte   = 8;
    public static final int nbBitsPerNibble = 4;
    
    
    ////////////////////////////////////////////////////////////////////////////
    // String utils
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * Mirror of the unicode table from 00c0 to 024f without diacritics.
     * Latin 1 - Latin Extended-B
     * Compare with https://unicode-table.com/en/
     */
    private static final String tab00c0 =
        "AAAAAAACEEEEIIII" + // AAAAAA(AE)CEEEEIIII
        "DNOOOOOxOUUUUYTs" + // DNOOOOOxOUUUUY(TH)(ss)
        "aaaaaaaceeeeiiii" + // aaaaaa(ae)ceeeeiiii 
        "dnooooo/ouuuuyty" + // dnooooo/ouuuuy(th)y
        "AaAaAaCcCcCcCcDd" +
        "DdEeEeEeEeEeGgGg" +
        "GgGgHhHhIiIiIiIi" +
        "IiIiJjKkkLlLlLlL" + // Ii(IJ)(ij)JjKkkLlLlLlL
        "lLlNnNnNnnNnOoOo" +
        "OoOoRrRrRrSsSsSs" + // Oo(OE)(oe)RrRrRrSsSsSs
        "SsTtTtTtUuUuUuUu" +
        "UuUuWwYyYZzZzZzf" +
        "bBBb66oCcDDDddEE" +
        "EFfGghIIKkllMNnO" + // EFfGg(hv)IIKkllMNnO
        "OoOopprssssttttu" + // Oo(Oi)(oi)pprssssttttu
        "uUVYyZzZZzz255?w" +
        "|||!DDdLLlNNnAaI" + // |||!(DZ)(Dz)(dz)(LJ)(Lj)(lj)(NJ)(Nj)(nj)AaI
        "iOoUuUuUuUuUueAa" +
        "AaAaGgGgKkOoOoZz" + // Aa(AE)(ae)GgGgKkOoOoZz
        "jDDdGgHwNnAaAaOo" + // j(DZ)(Dz)(dz)Gg(Hw)wNnAa(AE)(ae)Oo
        "AaAaEeEeIiIiOoOo" +
        "RrRrUuUuSsTtYyHh" +
        "ndOoZzAaEeOoOoOo" +
        "OoYylntjdqACcLTs" + // OoYylntj(db)(qp)ACcLTs
        "z??BUAEeJjQqRrYy";
    private static final String tab00c0dual =
        "      E         " + // AAAAAA(AE)CEEEEIIII
        "              Hs" + // DNOOOOOxOUUUUY(TH)(ss)
        "      e         " + // aaaaaa(ae)ceeeeiiii 
        "              h " + // dnooooo/ouuuuy(th)y
        "                " +
        "                " +
        "                " +
        "  Jj            " + // Ii(IJ)(ij)JjKkkLlLlLlL
        "                " +
        "  Ee            " + // Oo(OE)(oe)RrRrRrSsSsSs
        "                " +
        "                " +
        "                " +
        "     v          " + // EFfGg(hv)IIKkllMNnO
        "  ii            " + // Oo(Oi)(oi)pprssssttttu
        "                " +
        "    ZzzJjjJjj   " + // |||!(DZ)(Dz)(dz)(LJ)(Lj)(lj)(NJ)(Nj)(nj)AaI
        "                " +
        "  Ee            " + // Aa(AE)(ae)GgGgKkOoOoZz
        " Zzz  w     Ee  " + // j(DZ)(Dz)(dz)Gg(Hw)wNnAa(AE)(ae)Oo
        "                " +
        "                " +
        "                " +
        "        bp      " + // OoYylntj(db)(qp)ACcLTs
        "                ";
    
    private static boolean charHandeldByTable(char oneChar) {
        return oneChar >= '\u00c0' && oneChar <= '\u024f';
    }
    
    /*
     * Replaces :
     *    - capitalized letters with uncapitalized letters
     *    - accentuated letters with normal letters (e.g., "é" with "e"),
     *    - double letters with their two letters (e.g., "œ" with "oe"),
     *    - special characters with " " (e.g., "-" with " ").
     * N.B.: it is required for the Snowball stemmer that words do not contain
     * capitalized letters, see https://snowballstem.org/texts/vowelmarking.html
     * For a full example,
     *    - "C'est-à-dire que les œufs" will be normalized as
     *    - "c est a dire que les oeufs".
     * 
     * TODO: we might want to keep, e.g., the point in "3.14", the "-" in
     * "It's -40°C out there"... (other examples of non-alphanumeric to keep ?)
     * 
     * @param str, the String to normalize.
     * @return the normalized version of str.
     */
    public static String normalize(String str) {
        if (str == null) {
            return "";
        }
        
        // Solution adapted from https://pastebin.com/FAAm6a2j
        // https://stackoverflow.com/questions/3322152/is-there-a-way-to-get-rid-of-accents-and-convert-a-whole-string-to-regular-lette
        int length = str.length();
        char oneChar;
        for (int i = 0; i < str.length(); i++) {
            oneChar = str.charAt(i);
            if (charHandeldByTable(oneChar) && (tab00c0dual.charAt((int)oneChar - '\u00c0') != ' ')) {
                length++;
            }
        }
        
        char[] newString = new char[length];
        int offset = 0;
        for (int i = 0; i < str.length(); i++) {
            oneChar = str.charAt(i);
            
            if (charHandeldByTable(oneChar)) {
                // Replaces accentuated letters with normal letters.
                oneChar = tab00c0.charAt((int)oneChar - '\u00c0');
            }
            
            if (oneChar >= 'A' && oneChar <= 'Z') {
                // The string in lower case.
                oneChar -= 'A' - 'a';
                
            } else if (!((oneChar >= 'a' && oneChar <= 'z') ||
                    (oneChar >= '0' && oneChar <= '9'))) {
                // Replaces non-alphanumeric by a space.
                oneChar = ' ';
            }
            newString[i + offset] = oneChar;
            
            // Replaces double letters with two normal letters.
            oneChar = str.charAt(i);
            if (charHandeldByTable(oneChar)) {
                oneChar = tab00c0dual.charAt((int)oneChar - '\u00c0');
                if (oneChar != ' ') {
                    offset++;
                    
                    if (oneChar >= 'A' && oneChar <= 'Z') {
                        // The string in lower case.
                        oneChar -= 'A' - 'a';
                        
                    } else if (!((oneChar >= 'a' && oneChar <= 'z') ||
                            (oneChar >= '0' && oneChar <= '9'))) {
                        // Replaces non-alphanumeric by a space.
                        oneChar = ' ';
                    }
                    newString[i + offset] = oneChar;
                }
            }
        }
        
        return new String(newString);
    }
    
    private static final int subURLMaxSizeToIndex = 30;
    private static final String subString1 = "http://";
    private static final String subString2 = "https://";
    private static final String subString3 = "mailto:";
    
    /*
     * If the string contains an URL, remove parts of the URL which are irrelevant
     * for indexing. In our case, we consider that parts between two consecutive
     * '/' are irrelevant when their size is >= subURLMaxSizeToIndex, unless
     * it is the first part of the URL (between "http(s)://" and the first '/').
     *
     * @param str, the String from which we want to remove long URL substrings.
     * @return the version of str without the irrelevant parts from its URLs.
     */
    public static String removeLongURL(String str) {
        // To find urls, there are a lot of complicated regular expressions out there.
        // Here, we KISS (keep it simple stupid).
        // https://stackoverflow.com/questions/5713558/detect-and-extract-url-from-a-string
        // http://www.regexguru.com/2008/11/detecting-urls-in-a-block-of-text/
        // https://engineering.linkedin.com/blog/2016/06/open-sourcing-url-detector--a-java-library-to-detect-and-normali
        // https://stackoverflow.com/questions/161738/what-is-the-best-regular-expression-to-check-if-a-string-is-a-valid-url
        // https://mathiasbynens.be/demo/url-regex
        // http://www.ex-parrot.com/~pdw/Mail-RFC822-Address.html
        if (!str.contains(subString1) && !str.contains(subString2) && !str.contains(subString3)) {
            return str;
        } else {
            StringBuilder sb = new StringBuilder();
            boolean isMail = false;
            
            // Index what is before the URL, including the "http(s)://".
            int length = 0;
            int index = str.indexOf(subString1);
            if (index >= 0) {
                length = subString1.length();
            } else {
                index = str.indexOf(subString2);
                if (index >= 0) {
                    length = subString2.length();
                } else {
                    // Thanks to the first if, we know that either subString1, 2 or 3 is present.
                    isMail = true;
                    index = str.indexOf(subString3);
                    length = subString3.length();
                }
            }
            sb.append(str.substring(0, index + length));
            str = str.substring(index + length);
            
            if (isMail) {
                // We are parsing a "mailto:..."
                int indexQuestion = str.indexOf("?");
                if (indexQuestion != -1) {
                    // A '?' in a "mailto:..." usually indicates parameters passed, we can drop them.
                    str = str.substring(0, indexQuestion);
                }
                if (str.length() <= subURLMaxSizeToIndex) {
                    // Index only smaller than subURLMaxSizeToIndex subURLs.
                    sb.append(str);
                }
                
            } else {
                // We are parsing a "http(s)://..."
                // Always index the base URL (between "http(s)://" and the first '/').
                index = str.indexOf("/");
                if (index == -1) {
                    sb.append(str);
                } else {
                    sb.append(str.substring(0, index + 1));
                    str = str.substring(index + 1);
                }
                
                // For every pair of '/', check what is inside.
                while (index >= 0) {
                    index = str.indexOf("/");
                    if (index == -1) {
                        // If no more '/' are found in the URL, we are in the last part of it.
                        int indexQuestion = str.indexOf("?");
                        if (indexQuestion != -1) {
                            // A '?' in the last part of the URL usually indicates parameters passed to a php page, we can drop them.
                            str = str.substring(0, indexQuestion);
                        }
                        if (str.length() <= subURLMaxSizeToIndex) {
                            // Index only smaller than subURLMaxSizeToIndex subURLs.
                            sb.append(str);
                        }
                    } else {
                        if (index <= subURLMaxSizeToIndex) {
                            // Index only smaller than subURLMaxSizeToIndex subURLs.
                            sb.append(str.substring(0, index + 1));
                        }
                        str = str.substring(index + 1);
                    }
                }
            }
            
            return sb.toString();
        }
    }
}
