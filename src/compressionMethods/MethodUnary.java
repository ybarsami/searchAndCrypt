/**
 * Index compression with the unary method.
 *
 * Witten, Moffat, Bell, "Managing Gigabytes" (1999), p. 117
 * One such code is the unary code. In this code an integer x > 1 is coded
 * as x - 1 one bits followed by a zero bit, so that the code for integer 3
 * is 110.
 */

package compressionMethods;

import java.io.DataInputStream;

import static compressionMethods.Tools.*;

/**
 *
 * @author yann
 */
public class MethodUnary extends MethodByElement {

    @Override
    public void writeCode(int x, BitSetWithLastPosition buffer) {
        writeCodeUnary(x, buffer);
    }

    @Override
    public int readCode(DataInputStream in, int[] currentBits, int[] nbCurrentBitsRead) {
        return readCodeUnary(in, currentBits, nbCurrentBitsRead);
    }
    
    public static void writeCodeUnary(int x, BitSetWithLastPosition buffer) {
        // x - 1 "1"
        buffer.setEnd(x - 1);
        // One "0"
        buffer.clearEnd();
    }
    
    public static int readCodeUnary(DataInputStream in, int[] currentBits, int[] nbCurrentBitsRead) {
        int value = 0;
        boolean hasReadAZero = false;
        while (!hasReadAZero) {
            // If there are no more bits to read, read a new byte in the file.
            if (nbCurrentBitsRead[0] == nbBitsPerByte) {
                nbCurrentBitsRead[0] = 0;
                readByteFromFile(in, currentBits);
            }
            // Extract a bit.
            int bitRead = currentBits[nbCurrentBitsRead[0]++];
            if (bitRead == 1) {
                value++;
            } else {
                hasReadAZero = true;
            }
        }
        return value + 1;
    }

}
