/**
 * Integer tools.
 */

package compressionMethods;

/**
 *
 * @author yann
 */
public class IntegerTools {
    
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
}
