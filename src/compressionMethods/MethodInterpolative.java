/**
 * Index compression with the interpolative method.
 * We here use only the algorithm from Section 3, without the refinements
 * presented in Sections 4 and 6.
 *
 * Moffat and Stuiver, "Exploiting Clustering in Inverted File Compression" (1996), p. 5
 * The process of calculating ranges and codes is captured by the following
 * pseudo-code. Function Binary_Code(x, lo, hi) is assumed to encode a
 * number lo <= x <= hi in some appropriate manner. The simplest mechanism
 * for doing this (as assumed above) requires ceiling(log_2(hi - lo + 1))
 * bits. Other mechanisms are also possible, and are discussed below. The
 * operation “+” in step 5 denotes concatenation of codewords.
 *
 * Interpolative_Code(L, f, lo, hi)
 *     1. Let L[O... (f - 1)] be a sorted list of f document numbers, all
 *        in the range lo... hi.
 *     2. If f = 0 then return the empty string.
 *     3. If f = 1 then return Binary_Code(L[O], lo, hi).
 *     4. Otherwise, calculate
 *        (a) h <-- f div 2.
 *        (b) m <-- L[h].
 *        (c) L1 <-- L[O... (h - 1)].
 *        (d) L2 <-- L[(h + 1)... (f - 1)].
 *     5. Return Binary_Code(m, lo + h, hi - (f - h - 1)) +
 *               Interpolative_Code(L1, h, lo, m - 1) +
 *               Interpolative_Code(L2, f - h - 1, m + 1, hi).
 */

package compressionMethods;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import static compressionMethods.IntegerTools.*;

/**
 *
 * @author yann
 */
public class MethodInterpolative extends MethodByBitSequence {

    /*
     * When using this method, each of the mail identifiers has to be less
     * or equal than nbMails, and the list of identifiers has to be in strict
     * ascending order.
     */
    private final int nbMails;
    
    /**
     * Creates a new instance of MethodInterpolative.
     */
    public MethodInterpolative(int nbMails) {
        this.nbMails = nbMails;
    }
    
    @Override
    public final BitSequence bitSequenceOfMailList(ArrayIntList mailList) {
        int nbMailsLocal = mailList.size();
        for (int i = 1; i < nbMailsLocal; i++) {
            assert(mailList.get(i) > mailList.get(i - 1));
        }
        assert(mailList.get(nbMailsLocal - 1) <= this.nbMails);
        BitSequence buffer = new BitSequence();
        writeListCodeInterpolative(buffer, mailList, nbMailsLocal, 1, this.nbMails);
        return buffer;
    }
    
    @Override
    public final ArrayIntList readMailList(BitInputStream bitInputStream, int nbMailsLocal) {
        ArrayIntList mailList = new ArrayIntList();
        readCodeListInterpolative(bitInputStream, mailList, nbMailsLocal, 1, this.nbMails);
        return mailList;
    }
    
    private static void writeListCodeInterpolative(BitSequence buffer, IntList mailList, int f, int lo, int hi) {
        switch (f) {
            case 0:
                return;
            case 1:
                writeCodeBinary(mailList.get(0), buffer, lo, hi);
                return;
            default:
                int h = f / 2;
                int m = mailList.get(h);
                // fromIndex is inclusive, toIndex is exclusive.
                IntList L1 = mailList.subList(0, h);
                IntList L2 = mailList.subList(h + 1, f);
                writeCodeBinary(m, buffer, lo + h, hi - (f - h - 1));
                writeListCodeInterpolative(buffer, L1, h, lo, m - 1);
                writeListCodeInterpolative(buffer, L2, f - h - 1, m + 1, hi);
                break;
        }
    }
    
    /*
     * Writes x on just ceiling(log_2(hi - lo + 1)) bits.
     * Assumes that lo <= x <= hi.
     */
    private static void writeCodeBinary(int x, BitSequence buffer, int lo, int hi) {
        MethodBinary.writeCodeBinary(x - lo, buffer, ceilingLog2(hi - lo + 1));
    }
    
    private static void readCodeListInterpolative(BitInputStream bitInputStream, ArrayIntList mailList, int f, int lo, int hi) {
        switch (f) {
            case 0:
                return;
            case 1:
                {
                    int m = readCodeBinary(bitInputStream, lo, hi);
                    mailList.add(m);
                    break;
                }
            default:
                {
                    int h = f / 2;
                    int m = readCodeBinary(bitInputStream, lo + h, hi - (f - h - 1));
                    readCodeListInterpolative(bitInputStream, mailList, h, lo, m - 1);
                    mailList.add(m);
                    readCodeListInterpolative(bitInputStream, mailList, f - h - 1, m + 1, hi);
                    break;
                }
        }
    }
    
    private static int readCodeBinary(BitInputStream bitInputStream, int lo, int hi) {
        // The number of bits we have to read for the current int to extract.
        int nbBitsToRead = ceilingLog2(hi - lo + 1);
        return MethodBinary.readCodeBinary(bitInputStream, nbBitsToRead) + lo;
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof MethodInterpolative &&
                ((MethodInterpolative)o).nbMails == this.nbMails;
    }
    
    @Override
    public int hashCode() {
        return MethodInterpolative.class.getName().hashCode();
    }

}
