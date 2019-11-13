/**
 * Maintain a stream of bits that can be read one by one.
 */

package compressionMethods;

/**
 *
 * @author yann
 */
public abstract class BitStream {
    
    /*
     * Get the next bit from this bit stream.
     */
    public abstract int getNextBit() throws IndexOutOfBoundsException;

}
