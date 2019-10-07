/**
 * Most compression methods begin by transforming the mailList to a gapList,
 * and then iterates on the gaps to output the index.
 */

package compressionMethods;

import java.io.DataInputStream;

import org.apache.commons.collections.primitives.ArrayIntList;

/**
 *
 * @author yann
 */
public abstract class MethodByElement extends MethodByBitSequence {
    
    public static ArrayIntList gapList(ArrayIntList mailList) {
        ArrayIntList gapList = new ArrayIntList();
        gapList.add(mailList.get(0));
        for (int i = 1; i < mailList.size(); i++) {
            gapList.add(mailList.get(i) - mailList.get(i - 1));
        }
        return gapList;
    }
    
    /*
     * Output a BitSequence encoding the numbers included in mailList. All the
     * numbers in the mailList are supposed to be in sorted order.
     */
    @Override
    public final BitSequence bitSequenceOfMailList(ArrayIntList mailList) {
        BitSequence buffer = new BitSequence();
        ArrayIntList gapList = gapList(mailList);
        for (int i = 0; i < mailList.size(); i++) {
            writeCode(gapList.get(i), buffer);
        }
        return buffer;
    }
    
    /*
     * Write the code of x inside the BitSequence.
     */
    public abstract void writeCode(int x, BitSequence buffer);
    
    @Override
    public final ArrayIntList readMailList(BitStream bitStream, int nbMailsLocal) {
        ArrayIntList mailList = new ArrayIntList();
        int idMail = 0;
        int nbMailsTreated = 0;
        while (nbMailsTreated < nbMailsLocal) {
            // Extract a gap.
            int gap = readCode(bitStream);
            // Add the gap to the mail list.
            idMail += gap;
            mailList.add(idMail);
            nbMailsTreated++;
        }
        return mailList;
    }
    
    /*
     * Read the code of an integer from the given bit input stream.
     */
    public abstract int readCode(BitStream bitStream);

}
