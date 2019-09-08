/**
 * Client.java
 *
 * Created on 14 févr. 2019
 */

package searchAndCrypt;

import org.tartarus.snowball.SnowballStemmer;

/**
 *
 * @author yann
 */
public class Client {
    
    private Server server;
    public MimeParse mimeParse;
    public Request request;
    
    /**
     * Creates a new instance of Client.
     */
    public Client(Server server) {
        this.server = server;
        mimeParse = new MimeParse(server);
        request = new Request(server);
        loadLanguage(server.getLanguage());
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
    public final void loadLanguage(String language) {
        if (language == null) {
            mimeParse.setStemmer(null);
            request.setStemmer(null);
            
        } else {
            try {
                Class stemClass = Class.forName("org.tartarus.snowball.ext." +
                        language + "Stemmer");
                mimeParse.setStemmer((SnowballStemmer) stemClass.newInstance());
                request.setStemmer((SnowballStemmer) stemClass.newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                System.out.println(language + " is not a valid language for SnowballStemmer.");
                System.out.println("Valid languages are: danish, dutch, english, finnish, french, german, hungarian, italian, norwegian, porter, portuguese, romanian, russian, spanish, swedish, turkish.");
            }
        }
    }
}
