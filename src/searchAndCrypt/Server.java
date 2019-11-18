/**
 * Global API that a server should follow in order to use the functionalities
 * of this library.
 */

package searchAndCrypt;

import java.io.File;

import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

/**
 *
 * @author yann
 */
public abstract class Server {
    
    /*
     * Get the number of chunks of the full index currently stored on this
     * server.
     */
    public abstract int getNbIndexChunks();
    
    /*
     * Get the maximum identifier of an e-mail indexed on the server.
     */
    public abstract int getMaxIdIndexedMail();
    
    ////////////////////////////////////////////////////////////////////////////
    // Interaction : Client <- Server.
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * Get all the e-mail identifiers that have identifier >= minIdentifier.
     *
     * @return the wanted e-mail identifiers, as a sorted set.
     */
    public abstract TreeSet<Integer> getAllMessageIdentifiers(int minIdentifier);
    public final TreeSet<Integer> getAllMessageIdentifiers() {
        return getAllMessageIdentifiers(0);
    }
    
    /*
     * Get the e-mail which has the given identifier.
     *
     * @param idMessage, the identifier of the wanted e-mail.
     * @return the wanted message.
     */
    public abstract File getMessage(int idMessage);
    
    /*
     * Get the index.
     *
     * @return the file containing the compressed index.
     */
    public abstract File getIndexFile();
    
    /*
     * Get the two consecutive chunks with minimum file size.
     *
     * @return the two consecutive chunks with minimum file size.
     */
    public abstract List<File> getTwoChunksIndex();
    
    /*
     * Get the list of mails removed from the server since last connexion.
     * The client can then update the index by removing those e-mails from the
     * posting lists (thus, gaining some space).
     *
     * @return the list of mails removed from the server since last connexion.
     */
    public abstract List<Integer> getRemovedMailIds();
    
    // Main language in which the mails are written.
    private String language;
    
    /*
     * Get the language used.
     *
     * @return the String containing the language currently in use for the
     * stemmer, null if the index is not stemmed.
     */
    public final String getLanguage() {
        return language;
    }
    
    /*
     * Sets a language to be used by the stemmer.
     *
     * @param language, the String representing the language on which the
     * stemmer will work.
     * Valid language strings are:
     * danish, dutch, english, finnish, french, german, hungarian, italian,
     * norwegian, porter, portuguese, romanian, russian, spanish, swedish,
     * turkish.
     *
     * @return true if this changed the language, false otherwise.
     */
    public final boolean setLanguage(String language) {
        if (Objects.equals(this.language, language)) {
            return false;
        }
        this.language = language;
        return true;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Interaction : Client -> Server.
    ////////////////////////////////////////////////////////////////////////////
    
    public static final int NOT_A_NEW_CHUNK = -1;
    
    /*
     * Updates the index with a new version.
     *
     * @param newIndexFile, a new version of the index that will replace the
     * current one.
     * @return true iff the index has successfully be replaced.
     */
    public abstract boolean updateIndexFile(File newIndexFile);
    
    /*
     * Receives a new chunk of the index --- to avoid needing to build the full
     * index before sending it.
     *
     * @param chunkedIndexFile, a chunk of the index that will added to the
     *        current ones.
     * @param maxIdMail, the maximum index of the mail contained in the chunk.
     *        If set to -1, this is the result of two chunks currently on
     *        the server (in this case, replace them with the chunk), otherwise
     *        this is a new chunk (in this case, add the chunk to the list).
     * @return true iff the chunked index has been successfully added.
     */
    public abstract boolean addChunkedIndexFile(File chunkedIndexFile, int maxIdMail);
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Local computation by the Server.
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * The chunks have all been merged to the last chunk on the server.
     * Set the index to this last chunk.
     *
     * @return true iff the index has successfully be replaced.
     */
    public abstract boolean replaceIndexWithLastChunk();
    
    /*
     * Whenever a new mail comes or an old mail is deleted, updates the map
     * between the e-mails and the identifiers (as managed in the inverted
     * lists).
     */
    public abstract void updateMap();
    
    /*
     * Whenever a lot a e-mails have been deleted (i.e., their identifier now
     * corresponds to no e-mail on the server), it can be a good idea to
     * compress the map between the e-mails and the identifiers.
     */
    public abstract void compressMap();
}
