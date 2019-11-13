/**
 * Allows a BitSequence to be read bit by bit.
 */

package compressionMethods;

/**
 *
 * @author yann
 */
public class BitSequenceStream extends BitStream {
    
    final BitSequence bitSequence;
    
    int nbBitsRead;
    
    /**
     * Creates a new instance of BitSequenceStream.
     */
    public BitSequenceStream(BitSequence bitSequence) {
        this.bitSequence = bitSequence;
        this.nbBitsRead = 0;
    }
    
    /*
     * Get the next bit from this bit stream.
     */
    @Override
    public int getNextBit() throws IndexOutOfBoundsException {
        return bitSequence.get(nbBitsRead++) ? 1 : 0;
    }

}
