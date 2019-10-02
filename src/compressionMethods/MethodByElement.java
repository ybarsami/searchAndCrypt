/**
 * Most compression methods begin by transforming the mailList to a gapList,
 * and then iterates on the gaps to output the index.
 */

package compressionMethods;

import java.io.DataInputStream;

import org.apache.commons.collections.primitives.ArrayIntList;

import static compressionMethods.Tools.*;

/**
 *
 * @author yann
 */
public abstract class MethodByElement extends MethodByBitSet {
    
    public static ArrayIntList gapList(ArrayIntList mailList) {
        ArrayIntList gapList = new ArrayIntList();
        gapList.add(mailList.get(0));
        for (int i = 1; i < mailList.size(); i++) {
            gapList.add(mailList.get(i) - mailList.get(i - 1));
        }
        return gapList;
    }
    
    /*
     * Output a BitSet encoding the numbers included in mailList. All the
     * numbers in the mailList are supposed to be in sorted order.
     */
    @Override
    public final BitSetWithLastPosition bitSetOfMailList(ArrayIntList mailList) {
        BitSetWithLastPosition buffer = new BitSetWithLastPosition();
        ArrayIntList gapList = gapList(mailList);
        for (int i = 0; i < mailList.size(); i++) {
            writeCode(gapList.get(i), buffer);
        }
        return buffer;
    }

    /*
     * Write the code of x inside the BitSet.
     */
    public abstract void writeCode(int x, BitSetWithLastPosition buffer);

    @Override
    public final ArrayIntList readMailList(DataInputStream in, int nbMailsLocal) {
        ArrayIntList mailList = new ArrayIntList();
        int[] currentBits = new int[nbBitsPerByte];
        int[] nbCurrentBitsRead = { nbBitsPerByte };
        int idMail = 0;
        int nbMailsTreated = 0;
        while (nbMailsTreated < nbMailsLocal) {
            // Extract a gap.
            int gap = readCode(in, currentBits, nbCurrentBitsRead);
            // Add the gap to the mail list.
            idMail += gap;
            mailList.add(idMail);
            nbMailsTreated++;
        }
        return mailList;
    }

    /*
     * Read the code of an integer which starts at position
     * nbCurrentBitsRead[0] inside the array currentBits (of size 8).
     * Update nbBitsRead[0] according to the number of bits read for this
     * integer.
     * If we reach the end of the array currentBits, we read a new set of 8
     * bits from the dataInputStream.
     */
    public abstract int readCode(DataInputStream in, int[] currentBits, int[] nbCurrentBitsRead);

}
