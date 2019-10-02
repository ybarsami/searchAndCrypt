/**
 * Normalize a string.
 */

package searchAndCrypt;

/**
 *
 * @author yann
 */
public class StringNormalizer {
    
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
}
