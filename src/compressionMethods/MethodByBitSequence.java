/**
 * Most compression methods output the index bit by bit.
 */

package compressionMethods;

import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;

/**
 *
 * @author yann
 */
public abstract class MethodByBitSequence extends CompressionMethod {
    
    @Override
    public final void writeMailList(DataOutputStream out, ArrayIntList mailList) {
        try {
            BitSequence buffer = bitSequenceOfMailList(mailList);
            for (byte b : buffer.toByteArray()) {
                out.writeByte(b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * Output a BitSequence encoding the numbers included in mailList. All the
     * numbers in the mailList are supposed to be in sorted order.
     */
    public abstract BitSequence bitSequenceOfMailList(ArrayIntList mailList);
    
}
