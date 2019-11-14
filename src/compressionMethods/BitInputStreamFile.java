/**
 * Allows a DataInputStream to be read bit by bit. The most little piece of data
 * that can be read on a DataInputStream is a byte, so we must here read 8 bits
 * by 8 bits.
 */

package compressionMethods;

import java.io.DataInputStream;
import java.io.IOException;

import static compressionMethods.IntegerTools.*;

/**
 *
 * @author yann
 */
public class BitInputStreamFile extends BitInputStream {
    
    // The BitInputStream here comes from an external file, converted to a data
    // input stream.
    private final DataInputStream dataInputStream;
    // It is not possible to directly read the file bit by bit, so we read the
    // file byte by byte (8 bits by 8 bits). When we load a byte, we copy the 8
    // bits inside this array...
    private int[] currentBits;
    // ... and we keep track of the number of bits we have already used among
    // those 8 bits (when we reach 8, we reload a new byte, and so on).
    private int nbCurrentBitsRead;
    
    /**
     * Creates a new instance of BitInputStreamFile.
     */
    public BitInputStreamFile(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
        this.nbCurrentBitsRead = nbBitsPerByte;
    }
    
    /*
     * Get the next bit from this bit stream.
     * In the general case, we read the bit at position nbCurrentBitsRead inside
     * the array currentBits (of size nbBitsPerByte = 8).
     * If we reach the end of the array currentBits, we read a new set of
     * nbBitsPerByte = 8 bits from the dataInputStream.
     * The bits are read in big endian by default, but this can be modified
     * by setting IntegerTools.isBigEndian to false.
     * In both cases, we update nbCurrentBitsRead.
     */
    @Override
    public int getNextBit() throws IndexOutOfBoundsException {
        // If there are no more bits to read, read a new byte in the file.
        if (nbCurrentBitsRead == nbBitsPerByte) {
            try {
                nbCurrentBitsRead = 0;
                currentBits = byteToBitArray(dataInputStream.readByte());
            } catch (IOException e) {
                throw new IndexOutOfBoundsException();
            }
        }
        // Extract a bit.
        return currentBits[nbCurrentBitsRead++];
    }

}
