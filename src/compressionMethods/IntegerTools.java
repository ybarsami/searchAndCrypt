/**
 * Integer tools.
 */

package compressionMethods;

/**
 *
 * @author yann
 */
public class IntegerTools {
    
    public static final int nbBitsPerByte = 8;
    public static final boolean isBigEndian = true;
    
    /*
     * Transform a byte to an array of 8 bits.
     * This function uses a big endian or little endian representation of the
     * byte, depending on the value of the boolean isBigEndian.
     */
    public static int[] byte2bitArray(byte b) {
        int[] bitArray = new int[nbBitsPerByte];
        int myByte = byte2int(b);
        for (int i = 0; i < nbBitsPerByte; i++) {
            bitArray[isBigEndian ? nbBitsPerByte - 1 - i : i] = myByte % 2;
            myByte /= 2;
        }
        return bitArray;
    }
    
    /*
     * Transform an array of 8 bits to a byte.
     * This function uses a big endian or little endian representation of the
     * byte, depending on the value of the boolean isBigEndian.
     */
    public static byte bitArray2byte(int[] bitArray) {
        assert(bitArray.length == nbBitsPerByte);
        byte b = 0;
        for (int i = 0; i < nbBitsPerByte; i++) {
            b |= bitArray[i] << (isBigEndian ? nbBitsPerByte - 1 - i : i);
        }
        return b;
    }
    
    /*
     * Converts a signed byte b (-128 <= b <= 127) to an unsigned int.
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
        assert(x >= 1);
        return ilog2(x - 1) + 1;
    }
    
    /*
     * Returns the ceiling of x / y.
     * x must be >= 0.
     * y must be >= 1.
     */
    public static int ceilingDivision(int x, int y) {
        assert(x >= 0);
        assert(y >= 1);
        return (x + y - 1) / y;
    }
    
    /*
     * Returns the floor of log2(x) if x >= 1 ; return -1 if x == 0 (this allows
     * the formula for ceilingLog2(x) = ilog2(x - 1) + 1).
     * x must be >= 0.
     */
    public static int ilog2(int x) {
        assert(x >= 0);
        return 31 - Integer.numberOfLeadingZeros(x);
    }
}
