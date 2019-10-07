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
public class BitInputStream extends BitStream {
    
    final DataInputStream dataInputStream;
    
    final int[] currentBits;
    
    int nbCurrentBitsRead;
    
    /**
     * Creates a new instance of BitInputStream.
     */
    public BitInputStream(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
        this.currentBits = new int[nbBitsPerByte];
        this.nbCurrentBitsRead = 0;
    }
    
    public static final int nbBitsPerByte = 8;
    
    /*
     * In BitSequence, we output the bytes in big endian.
     * This is the "reverse" function, that reads a byte and output an array of
     * 8 bits.
     */
    public static void readByteFromFile(DataInputStream dataInputStream, int[] currentBits) {
        try {
            boolean isBigEndian = true;
            int currentByte = byte2int(dataInputStream.readByte());
            for (int i = 0; i < nbBitsPerByte; i++) {
                currentBits[isBigEndian ? nbBitsPerByte - 1 - i : i] = currentByte % 2;
                currentByte /= 2;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * Get the next bit from this bit stream.
     * The general case is that we read the bit at position nbCurrentBitsRead
     * inside the array currentBits (of size nbBitsPerByte = 8).
     * If we reach the end of the array currentBits, we read a new set of
     * nbBitsPerByte = 8 bits from the dataInputStream.
     * In both cases, we update nbCurrentBitsRead.
     */
    @Override
    public int getNextBit() {
        // If there are no more bits to read, read a new byte in the file.
        if (nbCurrentBitsRead == nbBitsPerByte) {
            nbCurrentBitsRead = 0;
            readByteFromFile(dataInputStream, currentBits);
        }
        // Extract a bit.
        return currentBits[nbCurrentBitsRead++];
    }

}
