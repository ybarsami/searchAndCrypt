/**
 * Most compression methods output the index bit by bit.
 */

package compressionMethods;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;

/**
 *
 * @author yann
 */
public abstract class MethodByBitSequence extends CompressionMethod {
    
    @Override
    public final void writeMailList(DataOutputStream dataOutputStream, ArrayIntList mailList) {
        try {
            BitSequence buffer = bitSequenceOfMailList(mailList);
            for (byte b : buffer.toByteArray()) {
                dataOutputStream.writeByte(b);
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
    
    @Override
    public final ArrayIntList readMailList(DataInputStream dataInputStream, int nbMailsLocal) {
        BitInputStream bitInputStream = new BitInputStream(dataInputStream);
        return readMailList(bitInputStream, nbMailsLocal);
    }
    
    /*
     * Output the mailList encoded in the given BitSequence.
     */
    public final ArrayIntList readMailList(BitSequence bitSequence, int nbMailsLocal) {
        BitSequenceStream bitSequenceStream = new BitSequenceStream(bitSequence);
        return readMailList(bitSequenceStream, nbMailsLocal);
    }
    
    public abstract ArrayIntList readMailList(BitStream bitStream, int nbMailsLocal);
    
}
