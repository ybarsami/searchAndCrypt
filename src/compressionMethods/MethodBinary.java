/**
 * Index compression with the binary method.
 *
 * Witten, Moffat, Bell, "Managing Gigabytes" (1999), p. 114
 * For a text of N documents and an index containing f pointers, the total
 * space required with a naive representation is f * ceiling(log N) bits,
 * provided that pointers are stored in a minimal number of bits.
 */

package compressionMethods;

import java.io.DataInputStream;

import static compressionMethods.Tools.*;

/**
 *
 * @author yann
 */
public class MethodBinary extends MethodByElement {
    
    /*
     * ceiling(log nbMails), the minimal number of bits to store numbers in
     * { 1, 2, ... nbMails }.
     */
    private int nbBits;

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
        writeCodeBinary(x, buffer, nbBits);
    }

    @Override
    public int readCode(DataInputStream in, int[] currentBits, int[] nbCurrentBitsRead) {
        return readCodeBinary(in, currentBits, nbCurrentBitsRead, nbBits);
    }
    
    /*
     * Writes x on just nbBits bits.
     * Assumes that 0 <= x < 2^nbBits.
     */
    public  static void writeCodeBinary(int x, BitSequence buffer, int nbBits) {
        int bitMask = 1 << (nbBits - 1);
        for (int j = 0; j < nbBits; j++) {
            buffer.append((x & bitMask) != 0);
            bitMask >>= 1;
        }
    }
    
    public static int readCodeBinary(DataInputStream in, int[] currentBits, int[] nbCurrentBitsRead, int nbBitsToRead) {
        // The number of bits read *for the current int to extract*.
        int nbBitsRead = 0;
        int value = 0;
        while (nbBitsRead < nbBitsToRead) {
            // If there are no more bits to read, read a new byte in the file.
            if (nbCurrentBitsRead[0] == nbBitsPerByte) {
                nbCurrentBitsRead[0] = 0;
                readByteFromFile(in, currentBits);
            }
            // Extract a bit.
            int bitRead = currentBits[nbCurrentBitsRead[0]++];
            value *= 2;
            value += bitRead;
            nbBitsRead++;
        }
        return value;
    }

}
