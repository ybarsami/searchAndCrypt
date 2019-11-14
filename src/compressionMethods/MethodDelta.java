/**
 * Index compression with the delta method.
 *
 * Witten, Moffat, Bell, "Managing Gigabytes" (1999), p. 117
 * A further development is the \delta code, in which the prefix indicating
 * the number of binary suffix bits is represented by the \gamma code rather
 * than the unary code. Taking the same example of x = 9, the unary prefix
 * of 1110 coding 4 is replaced by 11000, the \gamma code for 4. That is,
 * the \delta code for x = 9 is 11000 001.
 */

package compressionMethods;

import static compressionMethods.IntegerTools.*;

/**
 *
 * @author yann
 */
public class MethodDelta extends MethodByElement {

    @Override
    public void writeCode(int x, BitSequence buffer) {
        writeCodeDelta(x, buffer);
    }

    @Override
    public int readCode(BitInputStream bitInputStream) {
        return readCodeDelta(bitInputStream);
    }
    
    public static void writeCodeDelta(int x, BitSequence buffer) {
        int ilog2x = ilog2(x);
        MethodGamma.writeCodeGamma(1 + ilog2x, buffer);
        int residual = x - (1 << ilog2x);
        MethodBinary.writeCodeBinary(residual, buffer, ilog2x);
    }
    
    public static int readCodeDelta(BitInputStream bitInputStream) {
        int ilog2x = MethodGamma.readCodeGamma(bitInputStream) - 1;
        int residual = MethodBinary.readCodeBinary(bitInputStream, ilog2x);
        return residual + (1 << ilog2x);
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof MethodDelta;
    }
    
    @Override
    public int hashCode() {
        return MethodDelta.class.getName().hashCode();
    }

}
