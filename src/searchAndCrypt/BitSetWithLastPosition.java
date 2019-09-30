/**
 * This class overrides some methods of the java.util.BitSet class by providing
 * a way to know what is the last bit which is relevant. We thus store a private
 * int "lastPosition" indicating that all bits (even those which have value 0)
 * at index less or equal to lastPosition are relevant.
 * 
 * WARNING : not all functions have been overriden, only the ones I use.
 */

package searchAndCrypt;

import java.util.BitSet;

/**
 *
 * @author yann
 */
public class BitSetWithLastPosition extends BitSet {
    
    private int lastPosition; // The last position of a bit which have been a value (1 or 0) of bitSet.
                              // It is neither bitSet.size() --- which is implementation-dependant.
                              //       neither bitSet.length() --- which gives the last 1 position.
    
    /**
     * Creates a new instance of BitSetWithLastPosition.
     */
    public BitSetWithLastPosition() {
        super();
        lastPosition = -1;
    }
    
    /*
     * The numberof relevant bits in the BitSet. Because indexes start at 0, it
     * is thus just lastPosition + 1.
     */
    public int nbBitsSet() {
        return lastPosition + 1;
    }
    
    /*
     * Returns a new byte array containing all the bits in this bit set.
     * The bits are written in BIG_ENDIAN.
     * 
     * WARNING: The BitSet class from which this class inherites uses a
     * different representation, and super.toByteArray writes bits in
     * LITTLE_ENDIAN.
     * e.g. 242_{10} = 11110010_2 would be written as 01001111_2 = 79_{10}.
     */
    @Override
    public byte[] toByteArray() {
        byte[] bytes = new byte[(nbBitsSet() + 7) / 8];       
        for (int i = 0; i < nbBitsSet(); i++) {
            if (get(i)) {
                bytes[i / 8] |= 1 << (7 - (i % 8));
            }
        }
        return bytes;
    }
    
    /*
     * Sets the bit at the specified index to {@code true}.
     *
     * @param  bitIndex the index of the bit to be set
     */
    @Override
    public void set(int bitIndex) {
        super.set(bitIndex);
        if (bitIndex > lastPosition) {
            lastPosition = bitIndex;
        }
    }
    
    /*
     * Adds a bit just after the last position and sets it to {@code true}.
     */
    public void setEnd() {
        set(lastPosition + 1);
    }
    
    /*
     * Sets the bit specified by the index to {@code false}.
     *
     * @param  bitIndex the index of the bit to be cleared
     */
    @Override
    public void clear(int bitIndex) {
        super.clear(bitIndex);
        if (bitIndex > lastPosition) {
            lastPosition = bitIndex;
        }
    }
    
    /*
     * Adds a bit just after the last position and sets it to {@code false}.
     */
    public void clearEnd() {
        clear(lastPosition + 1);
    }
    
    /*
     * Sets the bit just after the last position to the specified value.
     * N.B. : I think that this function has a bad name. set is everywhere else
     * used meaning "set to 1" and here it is the only place where it really
     * means "set to a given value". But this was the choice made in BitSet.
     *
     * @param  value a boolean value to set
     */
    public void setEnd(boolean value) {
        if (value) {
            setEnd();
        } else {
            clearEnd();
        }
    }
    
    /*
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     *
     * @param  fromIndex index of the first bit to be set
     * @param  toIndex index after the last bit to be set
     */
    @Override
    public void set(int fromIndex, int toIndex) {
        super.set(fromIndex, toIndex);
        if (toIndex - 1 > lastPosition) {
            lastPosition = toIndex - 1;
        }
    }
    
    /*
     * Sets the bits from the {@code lastPosition + 1} index (inclusive) to the
     * {@code lastPosition + 1 + nbPositions} (exclusive) to {@code true}.
     *
     * @param  nbPositions number of positions of bits to be set
     */
    public void setEnd(int nbPositions) {
        set(lastPosition + 1, lastPosition + 1 + nbPositions);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lastPosition; i++) {
            sb.append(this.get(i) ? '1' : '0');
        }
        return sb.toString();
    }
}
