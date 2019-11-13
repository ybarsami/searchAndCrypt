/**
 * This class internally uses java.util.BitSet. We only need to append new bits
 * to the bitset, so the interface is different. Furthermore, we here provide
 * a way to know what is the last bit which is relevant. We thus store a private
 * int "lastPosition" indicating that all bits (even those which have value 0)
 * at index less or equal to lastPosition are relevant.
 */

package compressionMethods;

import java.util.BitSet;

import static compressionMethods.IntegerTools.*;

/**
 *
 * @author yann
 */
public class BitSequence {
    
    private BitSet bitSet;
    private int lastPosition; // The last position of a bit which have been a value (1 or 0) of bitSet.
                              // It is neither bitSet.size() --- which is implementation-dependant.
                              //       neither bitSet.length() --- which gives the last 1 position.
    
    /**
     * Creates a new instance of BitSequence.
     */
    public BitSequence() {
        bitSet = new BitSet();
        lastPosition = -1;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Accessor methods
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * The number of bits in the BitSequence. Because indexes start at 0, it
     * is thus just lastPosition + 1.
     */
    public int nbBits() {
        return lastPosition + 1;
    }
    
    /*
     * Returns the value of the bit with the specified index.
     */
    public boolean get(int i) throws IndexOutOfBoundsException {
        if (i <= lastPosition) {
            return bitSet.get(i);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }
    
    /*
     * Returns a new byte array containing all the bits in this bit sequence.
     * The bits are written in big endian by default, but this can be modified
     * by setting IntegerTools.isBigEndian to false.
     * 
     * REMARK: The BitSet class (internally used by BitSequence) also has a
     * toByteArray() method, that uses the little endian representation.
     * e.g. 242_{10} = 11110010_2 would be written as 01001111_2 = 79_{10}.
     */
    public byte[] toByteArray() {
        final int nbBytes = ceilingDivision(nbBits(), 8);
        byte[] bytes = new byte[nbBytes];
        final int[] bits = new int[nbBitsPerByte];
        for (int i = 0; i < nbBytes; i++) {
            for (int j = 0; j < nbBitsPerByte; j++) {
                bits[j] = bitSet.get(i *nbBitsPerByte + j) ? 1 : 0;
            }
            bytes[i] |= bitArrayToByte(bits);
        }
        return bytes;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= lastPosition; i++) {
            sb.append(bitSet.get(i) ? '1' : '0');
        }
        return sb.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Modifying methods
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * Append {@code true} to the bit sequence.
     */
    private void appendTrue() {
        lastPosition++;
        bitSet.set(lastPosition);
    }
    
    /*
     * Append {@code false} to the bit sequence.
     */
    private void appendFalse() {
        lastPosition++;
        bitSet.clear(lastPosition);
    }
    
    /*
     * Append the specified value to the bit sequence.
     *
     * @param value a boolean value to append.
     */
    public void append(boolean value) {
        if (value) {
            appendTrue();
        } else {
            appendFalse();
        }
    }
    
    /*
     * Append the specified value to the bit sequence, nbPositions times.
     *
     * @param value a boolean value to append.
     * @param nbPositions number of positions of bits to be added.
     */
    public void append(boolean value, int nbPositions) {
        for (int i = 0; i < nbPositions; i++) {
            append(value);
        }
    }
}
