/**
 * Integer and string tools.
 */

package searchAndCrypt;

import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 * @author yann
 */
public class Tools {
    
    ////////////////////////////////////////////////////////////////////////////
    // Integer utils
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * Converts a signed byte b (-127 <= b <= 128) to an unsigned int.
     * (if the byte is negative, adds 256).
     */
    public static int byte2int(byte b) {
        return b < 0 ? (int)b + 256 : (int)b;
    }
    
    /*
     * Returns the ceiling of log2(x).
     * x must be >= 1.
     */
    public static int ceilingLog2(int x) {
        return ilog2(x - 1) + 1;
    }
    
    /*
     * Returns the ceiling of x / y.
     */
    public static int ceilingDivision(int x, int y) {
        return (x + y - 1) / y;
    }
    
    /*
     * Returns the floor of log2(x) if x >= 1 ; return -1 if x == 0 (this allows
     * the formula for ceilingLog2(x) = ilog2(x - 1) + 1).
     * x must be >= 0.
     */
    public static int ilog2(int x) {
        return 31 - Integer.numberOfLeadingZeros(x);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Number representation utils
    ////////////////////////////////////////////////////////////////////////////
    
    // In BitSetWithLastPosition, we output the bytes in big endian.
    // This is the "reverse" function, that reads a byte and output an array of
    // 8 bits.
    public static void readByteFromFile(DataInputStream in, int[] currentBits) {
        try {
            boolean isBigEndian = true;
            int currentByte = byte2int(in.readByte());
            for (int i = 0; i < nbBitsPerByte; i++) {
                currentBits[isBigEndian ? nbBitsPerByte - 1 - i : i] = currentByte % 2;
                currentByte /= 2;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static final int nbBitsPerByte = 8;
}
