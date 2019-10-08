/**
 * Index compression with the binary method.
 *
 * Witten, Moffat, Bell, "Managing Gigabytes" (1999), p. 114
 * For a text of N documents and an index containing f pointers, the total
 * space required with a naive representation is f * ceiling(log N) bits,
 * provided that pointers are stored in a minimal number of bits.
 */

package compressionMethods;

import static compressionMethods.IntegerTools.*;

/**
 *
 * @author yann
 */
public class MethodBinary extends MethodByElement {
    
    /*
     * ceiling(log nbMails), the minimal number of bits to store numbers in
     * { 1, 2, ... nbMails }.
     */
    private final int nbBits;
    
    /**
     * Creates a new instance of MethodBinary.
     * 
     * When using this method, each of the mail identifiers has to be less
     * or equal than nbMails.
     */
    public MethodBinary(int nbMails) {
        this.nbBits = ceilingLog2(nbMails);
    }
    
    @Override
    public void writeCode(int x, BitSequence buffer) {
        writeCodeBinary(x - 1, buffer, nbBits);
    }
    
    @Override
    public int readCode(BitStream bitStream) {
        return readCodeBinary(bitStream, nbBits) + 1;
    }
    
    /*
     * Writes x on just nbBitsToWrite bits.
     * Assumes that 0 <= x < 2^nbBitsToWrite.
     */
    public  static void writeCodeBinary(int x, BitSequence buffer, int nbBitsToWrite) {
        assert(nbBitsToWrite >= 0);
        assert(x >= 0);
        assert(x < (1 << nbBitsToWrite));
        int bitMask = 1 << (nbBitsToWrite - 1);
        for (int j = 0; j < nbBitsToWrite; j++) {
            buffer.append((x & bitMask) != 0);
            bitMask >>= 1;
        }
    }
    
    public static int readCodeBinary(BitStream bitStream, int nbBitsToRead) {
        int value = 0;
        for (int j = 0; j < nbBitsToRead; j++) {
            // Extract a bit.
            int bitRead = bitStream.getNextBit();
            value *= 2;
            value += bitRead;
        }
        return value;
    }

}
