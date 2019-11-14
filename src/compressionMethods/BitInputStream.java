/**
 * Maintain a stream of bits that can be read one by one.
 */

package compressionMethods;

/**
 *
 * @author yann
 */
public abstract class BitInputStream {
    
    /*
     * Get the next bit from this bit stream.
     */
    public abstract int getNextBit() throws IndexOutOfBoundsException;

}
