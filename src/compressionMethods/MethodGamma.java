/**
 * Index compression with the gamma method.
 *
 * Witten, Moffat, Bell, "Managing Gigabytes" (1999), p. 117
 * One is the \gamma code, which represents the number x as a unary code for
 * 1 + floor(log x) followed by a code of floor(log x) bits that represents
 * the value of x - 2^{floor(x)} in binary. The unary part specifies how
 * many bits are required to code x, and then the binary part actually codes
 * x in that many bits. For example, consider x = 9. Then floor(log x) = 3,
 * and so 4 = 1 + 3 is coded in unary (code 1110) followed by 1 = 9 - 8 as a
 * three-bit binary number (code 001), which combine to give a codeword of
 * 1110 001.
 */

package compressionMethods;

import java.io.DataInputStream;

import static compressionMethods.Tools.*;

/**
 *
 * @author yann
 */
public class MethodGamma extends MethodByElement {

    @Override
    public void writeCode(int x, BitSetWithLastPosition buffer) {
        writeCodeGamma(x, buffer);
    }

    @Override
    public int readCode(DataInputStream in, int[] currentBits, int[] nbCurrentBitsRead) {
        return readCodeGamma(in, currentBits, nbCurrentBitsRead);
    }
    
    public static void writeCodeGamma(int x, BitSetWithLastPosition buffer) {
        int ilog2x = ilog2(x);
        MethodUnary.writeCodeUnary(1 + ilog2(x), buffer);
        int residual = x - (1 << ilog2x);
        MethodBinary.writeCodeBinary(residual, buffer, ilog2x);
    }
    
    public static int readCodeGamma(DataInputStream in, int[] currentBits, int[] nbCurrentBitsRead) {
        int ilog2x = MethodUnary.readCodeUnary(in, currentBits, nbCurrentBitsRead) - 1;
        int residual = MethodBinary.readCodeBinary(in, currentBits, nbCurrentBitsRead, ilog2x);
        return residual + (1 << ilog2x);
    }

}
