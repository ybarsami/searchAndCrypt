/**
 * Index compression with the unary method.
 *
 * Witten, Moffat, Bell, "Managing Gigabytes" (1999), p. 117
 * One such code is the unary code. In this code an integer x >= 1 is coded
 * as x - 1 one bits followed by a zero bit, so that the code for integer 3
 * is 110.
 */

package compressionMethods;

/**
 *
 * @author yann
 */
public class MethodUnary extends MethodByElement {

    @Override
    public void writeCode(int x, BitSequence buffer) {
        writeCodeUnary(x, buffer);
    }

    @Override
    public int readCode(BitStream bitStream) {
        return readCodeUnary(bitStream);
    }
    
    public static void writeCodeUnary(int x, BitSequence buffer) {
        assert(x >= 1);
        // x - 1 "1"
        buffer.append(true, x - 1);
        // One "0"
        buffer.append(false);
    }
    
    public static int readCodeUnary(BitStream bitStream) {
        int value = 0;
        boolean hasReadAZero = false;
        while (!hasReadAZero) {
            // Extract a bit.
            int bitRead = bitStream.getNextBit();
            if (bitRead == 1) {
                value++;
            } else {
                hasReadAZero = true;
            }
        }
        return value + 1;
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof MethodUnary;
    }
    
    @Override
    public int hashCode() {
        return MethodUnary.class.getName().hashCode();
    }

}
