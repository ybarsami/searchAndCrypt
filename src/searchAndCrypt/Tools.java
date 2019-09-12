/**
 * Tools.java
 *
 * Created on 25 janv. 2019
 */

package searchAndCrypt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

/**
 *
 * @author yann
 */
public class Tools {
    
    ////////////////////////////////////////////////////////////////////////////
    // Integer utils
    ////////////////////////////////////////////////////////////////////////////
    
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
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Number representation utils
    ////////////////////////////////////////////////////////////////////////////
    
    public static final int nbBitsPerByte   = 8;
    public static final int nbBitsPerNibble = 4;
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Number representation utils : mailList to bitSet
    ////////////////////////////////////////////////////////////////////////////
    
    public static ArrayIntList gapList(ArrayIntList mailList) {
        ArrayIntList gapList = new ArrayIntList();
        gapList.add(mailList.get(0));
        for (int i = 1; i < mailList.size(); i++) {
            gapList.add(mailList.get(i) - mailList.get(i - 1));
        }
        return gapList;
    }
    
    public static BitSetWithLastPosition binaryOfMailList(ArrayIntList mailList, int nbBits) {
        BitSetWithLastPosition buffer = new BitSetWithLastPosition();
        ArrayIntList gapList = gapList(mailList);
        for (int i = 0; i < mailList.size(); i++) {
            Tools.writeCodeBinary(gapList.get(i), buffer, nbBits);
        }
        return buffer;
    }
    
    public static BitSetWithLastPosition variableNibbleOfMailList(ArrayIntList mailList) {
        BitSetWithLastPosition buffer = new BitSetWithLastPosition();
        ArrayIntList gapList = gapList(mailList);
        for (int i = 0; i < mailList.size(); i++) {
            Tools.writeCodeVariableNibble(gapList.get(i), buffer);
        }
        return buffer;
    }
    
    public static BitSetWithLastPosition variableByteOfMailList(ArrayIntList mailList) {
        BitSetWithLastPosition buffer = new BitSetWithLastPosition();
        ArrayIntList gapList = gapList(mailList);
        for (int i = 0; i < mailList.size(); i++) {
            Tools.writeCodeVariableByte(gapList.get(i), buffer);
        }
        return buffer;
    }
    
    public static BitSetWithLastPosition gammaOfMailList(ArrayIntList mailList) {
        BitSetWithLastPosition buffer = new BitSetWithLastPosition();
        ArrayIntList gapList = gapList(mailList);
        for (int i = 0; i < mailList.size(); i++) {
            Tools.writeCodeGamma(gapList.get(i), buffer);
        }
        return buffer;
    }
    
    public static BitSetWithLastPosition deltaOfMailList(ArrayIntList mailList) {
        BitSetWithLastPosition buffer = new BitSetWithLastPosition();
        ArrayIntList gapList = gapList(mailList);
        for (int i = 0; i < mailList.size(); i++) {
            Tools.writeCodeDelta(gapList.get(i), buffer);
        }
        return buffer;
    }
    
    public static BitSetWithLastPosition interpolativeOfMailList(ArrayIntList mailList, int nbMails) {
        int size = mailList.size();
        BitSetWithLastPosition buffer = new BitSetWithLastPosition();
        writeListCodeInterpolative(buffer, mailList, size, 1, nbMails);
        return buffer;
    }
    
    /*
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
    public static void writeListCodeInterpolative(BitSetWithLastPosition buffer, IntList mailList, int f, int lo, int hi) {
        if (f == 0) {
            return;
        } else if (f == 1) {
            writeCodeBinary(mailList.get(0), buffer, lo, hi);
            return;
        } else {
            int h = f / 2;
            int m = mailList.get(h);
            // fromIndex is inclusive, toIndex is exclusive.
            IntList L1 = mailList.subList(0, h);
            IntList L2 = mailList.subList(h + 1, f);
            writeCodeBinary(m, buffer, lo + h, hi - (f - h - 1));
            writeListCodeInterpolative(buffer, L1, h, lo, m - 1);
            writeListCodeInterpolative(buffer, L2, f - h - 1, m + 1, hi);
        }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Number representation utils : gap to bits
    ////////////////////////////////////////////////////////////////////////////
    
    public static BitSetWithLastPosition bitSetOfMailList(String indexType, int nbMails, ArrayIntList mailList) {
        switch(indexType) {
            case "binary":
                int nbBits = Tools.ceilingLog2(nbMails);
                return Tools.binaryOfMailList(mailList, nbBits);
            case "delta":
                return Tools.deltaOfMailList(mailList);
            case "gamma":
                return Tools.gammaOfMailList(mailList);
            case "interpolative":
                return Tools.interpolativeOfMailList(mailList, nbMails);
            case "variable_byte":
                return Tools.variableByteOfMailList(mailList);
            case "variable_nibble":
                return Tools.variableNibbleOfMailList(mailList);
            default:
                System.out.println(indexType + " is not a valid index type.");
                System.out.println("Valid index types are: "
                        + "binary32, binary, "
                        + "delta, gamma, interpolative, "
                        + "variable_byte, variable_nibble.");
                return new BitSetWithLastPosition();
        }
    }
    
    public static void writeMailList(String indexType, DataOutputStream out, int nbMails, ArrayIntList mailList) {
        try {
            switch(indexType) {
                case "binary32":
                    for (int i = 0; i < mailList.size(); i++) {
                        out.writeInt(mailList.get(i));
                    }
                    break;
                default:
                    BitSetWithLastPosition buffer = Tools.bitSetOfMailList(indexType, nbMails, mailList);
                    for (byte b : buffer.toByteArray()) {
                        out.writeByte(b);
                    }
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void writeMailListASCII(String indexType, FileWriter out, int nbMails, ArrayIntList mailList) {
        try {
            switch(indexType) {
                case "binary32":
                    for (int i = 0; i < mailList.size(); i++) {
                        out.write(" " + mailList.get(i));
                    }
                    break;
                default:
                    out.write(" ");
                    BitSetWithLastPosition buffer = Tools.bitSetOfMailList(indexType, nbMails, mailList);
                    for (int i = 0; i < 8 * Tools.ceilingDivision(buffer.nbBitsSet(), 8); i++) {
                        out.write(buffer.get(i) ? "1" : "0");
                    }
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * Writes the nbBits of (x - 1) on 4 * ceiling(nbBits/3) bits.
     * It writes the bits 4 by 4 (nibble by nibble), using the first of the 4
     * bits as a continuation bit : it is set to 1 if it is the end of the
     * variable nibble code of (x - 1), 0 if the variableNibble code of (x - 1)
     * continues on other nibble(s).
     */
    public static void writeCodeVariableNibble(int x, BitSetWithLastPosition buffer) {
        writeCodeVariableXBits(x, buffer, nbBitsPerNibble);
    }
    
    public static void writeCodeVariableByte(int x, BitSetWithLastPosition buffer) {
        writeCodeVariableXBits(x, buffer, nbBitsPerByte);
    }
    
    // Maximum 2^{X - 1} values per chunk (excluding the continuation bit).
    public static void writeCodeVariableXBits(int x, BitSetWithLastPosition buffer, int nbBitsPerChunk) {
        int nbBits = Math.max(Tools.ceilingLog2(x), 1);
        int nbChunks = ceilingDivision(nbBits, nbBitsPerChunk - 1);
        int bitMask = 1 << (nbBits - 1);
        x = x - 1;
        
        // First chunk (includes padding)
        // Continuation bit
        buffer.setEnd(nbChunks == 1);
        // Padding
        for (int j = nbBits; j < (nbBitsPerChunk - 1) * nbChunks; j++) {
            buffer.clearEnd();
        }
        // First useful bits
        for (int j = (nbBitsPerChunk - 1) * (nbChunks - 1); j < nbBits; j++) {
            buffer.setEnd((x & bitMask) != 0);
            bitMask >>= 1;
        }
        
        // Regular chunks
        for (int i = 1; i < nbChunks; i++) {
            // Continuation bit
            buffer.setEnd(i == nbChunks - 1);
            // Useful bits
            for (int j = 0; j < nbBitsPerChunk - 1; j++) {
                buffer.setEnd((x & bitMask) != 0);
                bitMask >>= 1;
            }
        }
    }
    
    /*
     * Writes x on just nbBits bits.
     * Assumes that 0 <= x < 2^nbBits.
     */
    public static void writeCodeBinary(int x, BitSetWithLastPosition buffer, int nbBits) {
        int bitMask = 1 << (nbBits - 1);
        for (int j = 0; j < nbBits; j++) {
            buffer.setEnd((x & bitMask) != 0);
            bitMask >>= 1;
        }
    }
    
    /*
     * Writes x on just ceiling(log_2(hi - lo + 1)) bits.
     * Assumes that lo <= x <= hi.
     */
    public static void writeCodeBinary(int x, BitSetWithLastPosition buffer, int lo, int hi) {
        writeCodeBinary(x - lo, buffer, ceilingLog2(hi - lo + 1));
    }
    
    /*
     * Witten, Moffat, Bell, "Managing Gigabytes" (1999), p. 117
     * One such code is the unary code. In this code an integer x > 1 is coded
     * as x - 1 one bits followed by a zero bit, so that the code for integer 3
     * is 110.
     */
    public static void writeCodeUnary(int x, BitSetWithLastPosition buffer) {
        // x - 1 "1"
        buffer.setEnd(x - 1);
        // One "0"
        buffer.clearEnd();
    }
    
    /*
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
    public static void writeCodeGamma(int x, BitSetWithLastPosition buffer) {
        int ilog2x = ilog2(x);
        writeCodeUnary(1 + ilog2(x), buffer);
        int residual = x - (1 << ilog2x);
        writeCodeBinary(residual, buffer, ilog2x);
    }
    
    /*
     * Witten, Moffat, Bell, "Managing Gigabytes" (1999), p. 117
     * A further development is the \delta code, in which the prefix indicating
     * the number of binary suffix bits is represented by the \gamma code rather
     * than the unary code. Taking the same example of x = 9, the unary prefix
     * of 1110 coding 4 is replaced by 11000, the \gamma code for 4. That is,
     * the \delta code for x = 9 is 11000 001.
     */
    public static void writeCodeDelta(int x, BitSetWithLastPosition buffer) {
        int ilog2x = ilog2(x);
        writeCodeGamma(1 + ilog2x, buffer);
        int residual = x - (1 << ilog2x);
        writeCodeBinary(residual, buffer, ilog2x);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Number representation utils : bits to gaps
    ////////////////////////////////////////////////////////////////////////////
    
    public static void readCodeListASCII(String indexType, String bitSetString, int nbMails, int nbMailsLocal, ArrayIntList mailList) {
        // Create a file with the content of the string.
        File file = new File("tmpASCII.txt");
        try (FileOutputStream fout = new FileOutputStream(file);
                DataOutputStream out = new DataOutputStream(fout)) {
            BitSetWithLastPosition buffer = new BitSetWithLastPosition();
            for (int i = 0; i < bitSetString.length(); i++) {
                buffer.setEnd(bitSetString.charAt(i) == '1');
            }
            for (byte b : buffer.toByteArray()) {
                out.writeByte(b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Then reads the file.
        try (FileInputStream fin = new FileInputStream(file);
                DataInputStream in = new DataInputStream(fin)) {
            Tools.readCodeList(indexType, in, nbMails, nbMailsLocal, mailList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        file.delete();
    }
    
    public static void readCodeList(String indexType, DataInputStream in, int nbMails, int nbMailsLocal, ArrayIntList mailList) {
        switch(indexType) {
            case "binary32":
                try {
                    for (int j = 0; j < nbMailsLocal; j++) {
                        int idMail = in.readInt();
                        mailList.add(idMail);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "binary":
                int nbBits = Tools.ceilingLog2(nbMails);
                Tools.readCodeListBinary(in, nbBits, nbMailsLocal, mailList);
                break;
            case "delta":
                Tools.readCodeListDelta(in, nbMailsLocal, mailList);
                break;
            case "gamma":
                Tools.readCodeListGamma(in, nbMailsLocal, mailList);
                break;
            case "interpolative":
                Tools.readCodeListInterpolative(in, nbMails, nbMailsLocal, mailList);
                break;
            case "variable_byte":
                Tools.readCodeListVariableByte(in, nbMailsLocal, mailList);
                break;
            case "variable_nibble":
                Tools.readCodeListVariableNibble(in, nbMailsLocal, mailList);
                break;
            default:
                System.out.println(indexType + " is not a valid index type.");
                System.out.println("Valid index types are: binary32, binary, "
                        + "delta, gamma, interpolative, "
                        + "variable_byte, variable_nibble.");
                break;
        }
    }
    
    private static void readByteFromFile(DataInputStream in, int[] currentBits) {
        try {
            // The toByteArray() function in BitSet outputs the bytes in little endian.
            boolean isBigEndian = true;
            int currentByte = byte2int(in.readByte());
            for (int i = 0; i < nbBitsPerByte; i++) {
                currentBits[isBigEndian ? nbBitsPerByte - 1 - i : i] = currentByte % 2;
                currentByte /= 2;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static int readCodeUnary(DataInputStream in, int[] currentBits, int[] nbCurrentBitsRead) {
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
    
    // @param nbBitsToRead, the number of bits we have to read for the current int to extract.
    private static int readCodeBinary(DataInputStream in, int[] currentBits, int[] nbCurrentBitsRead, int nbBitsToRead) {
        // The number of bits read *for the current int to extract*.
        int nbBitsRead = 0;
        int value = 0;
        while (nbBitsRead < nbBitsToRead) {
            // If there are no more bits to read, read a new byte in the file.
            if (nbCurrentBitsRead[0] == nbBitsPerByte) {
                nbCurrentBitsRead[0] = 0;
                readByteFromFile(in, currentBits);
            }
            // Extract a bit.
            int bitRead = currentBits[nbCurrentBitsRead[0]++];
            value *= 2;
            value += bitRead;
            nbBitsRead++;
        }
        return value;
    }
    
    public static void readCodeListBinary(DataInputStream in, int nbBitsToRead, int nbMailsLocal, ArrayIntList mailList) {
        int[] currentBits = new int[nbBitsPerByte];
        int[] nbCurrentBitsRead = { nbBitsPerByte };
        int idMail = 0;
        int nbMailsTreated = 0;
        while (nbMailsTreated < nbMailsLocal) {
            // Extract a gap.
            int gap = readCodeBinary(in, currentBits, nbCurrentBitsRead, nbBitsToRead);
            // Add the gap to the mail list.
            idMail += gap;
            mailList.add(idMail);
            nbMailsTreated++;
        }
    }
    
    private static int readCodeBinary(DataInputStream in, int[] currentBits, int[] nbCurrentBitsRead, int lo, int hi) {
        // The number of bits we have to read for the current int to extract.
        int nbBitsToRead = ceilingLog2(hi - lo + 1);
        return readCodeBinary(in, currentBits, nbCurrentBitsRead, nbBitsToRead) + lo;
    }
    
    private static int readCodeDelta(DataInputStream in, int[] currentBits, int[] nbCurrentBitsRead) {
        int ilog2x = readCodeGamma(in, currentBits, nbCurrentBitsRead) - 1;
        int residual = readCodeBinary(in, currentBits, nbCurrentBitsRead, ilog2x);
        return residual + (1 << ilog2x);
    }
    
    public static void readCodeListDelta(DataInputStream in, int nbMailsLocal, ArrayIntList mailList) {
        int[] currentBits = new int[nbBitsPerByte];
        int[] nbCurrentBitsRead = { nbBitsPerByte };
        int idMail = 0;
        int nbMailsTreated = 0;
        while (nbMailsTreated < nbMailsLocal) {
            // Extract a gap.
            int gap = readCodeDelta(in, currentBits, nbCurrentBitsRead);
            // Add the gap to the mail list.
            idMail += gap;
            mailList.add(idMail);
            nbMailsTreated++;
        }
    }
    
    private static int readCodeGamma(DataInputStream in, int[] currentBits, int[] nbCurrentBitsRead) {
        int ilog2x = readCodeUnary(in, currentBits, nbCurrentBitsRead) - 1;
        int residual = readCodeBinary(in, currentBits, nbCurrentBitsRead, ilog2x);
        return residual + (1 << ilog2x);
    }
    
    public static void readCodeListGamma(DataInputStream in, int nbMailsLocal, ArrayIntList mailList) {
        int[] currentBits = new int[nbBitsPerByte];
        int[] nbCurrentBitsRead = { nbBitsPerByte };
        int idMail = 0;
        int nbMailsTreated = 0;
        while (nbMailsTreated < nbMailsLocal) {
            // Extract a gap.
            int gap = readCodeGamma(in, currentBits, nbCurrentBitsRead);
            // Add the gap to the mail list.
            idMail += gap;
            mailList.add(idMail);
            nbMailsTreated++;
        }
    }
    
    public static void readCodeListInterpolative(DataInputStream in, int nbMails, int nbMailsLocal, ArrayIntList mailList) {
        int lo = 1;
        int hi = nbMails;
        int[] currentBits = new int[nbBitsPerByte];
        int[] nbCurrentBitsRead = { nbBitsPerByte };
        readCodeListInterpolative(in, currentBits, nbCurrentBitsRead, mailList, nbMailsLocal, lo, hi);
    }
    
    public static void readCodeListInterpolative(DataInputStream in, int[] currentBits, int[] nbCurrentBitsRead, ArrayIntList mailList,
            int f, int lo, int hi) {
        if (f == 0) {
            return;
        } else if (f == 1) {
            int m = readCodeBinary(in, currentBits, nbCurrentBitsRead, lo, hi);
            mailList.add(m);
        } else {
            int h = f / 2;
            int m = readCodeBinary(in, currentBits, nbCurrentBitsRead, lo + h, hi - (f - h - 1));
            readCodeListInterpolative(in, currentBits, nbCurrentBitsRead, mailList, h, lo, m - 1);
            mailList.add(m);
            readCodeListInterpolative(in, currentBits, nbCurrentBitsRead, mailList, f - h - 1, m + 1, hi);
        }
    }
    
    public static void readCodeListVariableByte(DataInputStream in, int nbMailsLocal, ArrayIntList mailList) {
        readCodeListVariableXBits(in, nbMailsLocal, mailList, nbBitsPerByte);
    }
    
    public static void readCodeListVariableNibble(DataInputStream in, int nbMailsLocal, ArrayIntList mailList) {
        readCodeListVariableXBits(in, nbMailsLocal, mailList, nbBitsPerNibble);
    }
    
    public static void readCodeListVariableXBits(DataInputStream in, int nbMailsLocal, ArrayIntList mailList, int nbBitsPerChunk) {
        int[] currentBits = new int[nbBitsPerByte];
        int nbCurrentBitsRead = nbBitsPerByte;
        int idMail = 0;
        int nbMailsTreated = 0;
        int gap = 0;
        while (nbMailsTreated < nbMailsLocal) {
            // If there are no more bits to read, read a new byte in the file.
            if (nbCurrentBitsRead == nbBitsPerByte) {
                nbCurrentBitsRead = 0;
                readByteFromFile(in, currentBits);
            }
            // Extract nbBitsPerChunk bits.
            int continuationBit = currentBits[nbCurrentBitsRead++];
            // Update the gap.
            for (int i = 0; i < nbBitsPerChunk - 1; i++) {
                int bitRead = currentBits[nbCurrentBitsRead++];
                gap *= 2;
                gap += bitRead;
            }
            if (continuationBit == 1) {
                // If the continuation bit is 1, add the gap to the mail list.
                // What is actually stored is gap - 1
                idMail += gap + 1;
                mailList.add(idMail);
                nbMailsTreated++;
                gap = 0;
            }
        }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // String utils
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * Mirror of the unicode table from 00c0 to 024f without diacritics.
     * Latin 1 - Latin Extended-B
     * Compare with https://unicode-table.com/en/
     */
    private static final String tab00c0 =
        "AAAAAAACEEEEIIII" + // AAAAAA(AE)CEEEEIIII
        "DNOOOOOxOUUUUYTs" + // DNOOOOOxOUUUUY(TH)(ss)
        "aaaaaaaceeeeiiii" + // aaaaaa(ae)ceeeeiiii 
        "dnooooo/ouuuuyty" + // dnooooo/ouuuuy(th)y
        "AaAaAaCcCcCcCcDd" +
        "DdEeEeEeEeEeGgGg" +
        "GgGgHhHhIiIiIiIi" +
        "IiIiJjKkkLlLlLlL" + // Ii(IJ)(ij)JjKkkLlLlLlL
        "lLlNnNnNnnNnOoOo" +
        "OoOoRrRrRrSsSsSs" + // Oo(OE)(oe)RrRrRrSsSsSs
        "SsTtTtTtUuUuUuUu" +
        "UuUuWwYyYZzZzZzf" +
        "bBBb66oCcDDDddEE" +
        "EFfGghIIKkllMNnO" + // EFfGg(hv)IIKkllMNnO
        "OoOopprssssttttu" + // Oo(Oi)(oi)pprssssttttu
        "uUVYyZzZZzz255?w" +
        "|||!DDdLLlNNnAaI" + // |||!(DZ)(Dz)(dz)(LJ)(Lj)(lj)(NJ)(Nj)(nj)AaI
        "iOoUuUuUuUuUueAa" +
        "AaAaGgGgKkOoOoZz" + // Aa(AE)(ae)GgGgKkOoOoZz
        "jDDdGgHwNnAaAaOo" + // j(DZ)(Dz)(dz)Gg(Hw)wNnAa(AE)(ae)Oo
        "AaAaEeEeIiIiOoOo" +
        "RrRrUuUuSsTtYyHh" +
        "ndOoZzAaEeOoOoOo" +
        "OoYylntjdqACcLTs" + // OoYylntj(db)(qp)ACcLTs
        "z??BUAEeJjQqRrYy";
    private static final String tab00c0dual =
        "      E         " + // AAAAAA(AE)CEEEEIIII
        "              Hs" + // DNOOOOOxOUUUUY(TH)(ss)
        "      e         " + // aaaaaa(ae)ceeeeiiii 
        "              h " + // dnooooo/ouuuuy(th)y
        "                " +
        "                " +
        "                " +
        "  Jj            " + // Ii(IJ)(ij)JjKkkLlLlLlL
        "                " +
        "  Ee            " + // Oo(OE)(oe)RrRrRrSsSsSs
        "                " +
        "                " +
        "                " +
        "     v          " + // EFfGg(hv)IIKkllMNnO
        "  ii            " + // Oo(Oi)(oi)pprssssttttu
        "                " +
        "    ZzzJjjJjj   " + // |||!(DZ)(Dz)(dz)(LJ)(Lj)(lj)(NJ)(Nj)(nj)AaI
        "                " +
        "  Ee            " + // Aa(AE)(ae)GgGgKkOoOoZz
        " Zzz  w     Ee  " + // j(DZ)(Dz)(dz)Gg(Hw)wNnAa(AE)(ae)Oo
        "                " +
        "                " +
        "                " +
        "        bp      " + // OoYylntj(db)(qp)ACcLTs
        "                ";
    
    private static boolean charHandeldByTable(char oneChar) {
        return oneChar >= '\u00c0' && oneChar <= '\u024f';
    }
    
    /*
     * Replaces :
     *    - capitalized letters with uncapitalized letters
     *    - accentuated letters with normal letters (e.g., "é" with "e"),
     *    - double letters with their two letters (e.g., "œ" with "oe"),
     *    - special characters with " " (e.g., "-" with " ").
     * N.B.: it is required for the Snowball stemmer that words do not contain
     * capitalized letters, see https://snowballstem.org/texts/vowelmarking.html
     * For a full example,
     *    - "C'est-à-dire que les œufs" will be normalized as
     *    - "c est a dire que les oeufs".
     * 
     * TODO: we might want to keep, e.g., the point in "3.14", the "-" in
     * "It's -40°C out there"... (other examples of non-alphanumeric to keep ?)
     * 
     * @param str, the String to normalize.
     * @return the normalized version of str.
     */
    public static String normalize(String str) {
        if (str == null) {
            return "";
        }
        
        // Solution adapted from https://pastebin.com/FAAm6a2j
        // https://stackoverflow.com/questions/3322152/is-there-a-way-to-get-rid-of-accents-and-convert-a-whole-string-to-regular-lette
        int length = str.length();
        char oneChar;
        for (int i = 0; i < str.length(); i++) {
            oneChar = str.charAt(i);
            if (charHandeldByTable(oneChar) && (tab00c0dual.charAt((int)oneChar - '\u00c0') != ' ')) {
                length++;
            }
        }
        
        char[] newString = new char[length];
        int offset = 0;
        for (int i = 0; i < str.length(); i++) {
            oneChar = str.charAt(i);
            
            if (charHandeldByTable(oneChar)) {
                // Replaces accentuated letters with normal letters.
                oneChar = tab00c0.charAt((int)oneChar - '\u00c0');
            }
            
            if (oneChar >= 'A' && oneChar <= 'Z') {
                // The string in lower case.
                oneChar -= 'A' - 'a';
                
            } else if (!((oneChar >= 'a' && oneChar <= 'z') ||
                    (oneChar >= '0' && oneChar <= '9'))) {
                // Replaces non-alphanumeric by a space.
                oneChar = ' ';
            }
            newString[i + offset] = oneChar;
            
            // Replaces double letters with two normal letters.
            oneChar = str.charAt(i);
            if (charHandeldByTable(oneChar)) {
                oneChar = tab00c0dual.charAt((int)oneChar - '\u00c0');
                if (oneChar != ' ') {
                    offset++;
                    
                    if (oneChar >= 'A' && oneChar <= 'Z') {
                        // The string in lower case.
                        oneChar -= 'A' - 'a';
                        
                    } else if (!((oneChar >= 'a' && oneChar <= 'z') ||
                            (oneChar >= '0' && oneChar <= '9'))) {
                        // Replaces non-alphanumeric by a space.
                        oneChar = ' ';
                    }
                    newString[i + offset] = oneChar;
                }
            }
        }
        
        return new String(newString);
    }
    
    private static final int subURLMaxSizeToIndex = 30;
    private static final String subString1 = "http://";
    private static final String subString2 = "https://";
    private static final String subString3 = "mailto:";
    
    /*
     * If the string contains an URL, remove parts of the URL which are irrelevant
     * for indexing. In our case, we consider that parts between two consecutive
     * '/' are irrelevant when their size is >= subURLMaxSizeToIndex, unless
     * it is the first part of the URL (between "http(s)://" and the first '/').
     *
     * @param str, the String from which we want to remove long URL substrings.
     * @return the version of str without the irrelevant parts from its URLs.
     */
    public static String removeLongURL(String str) {
        // To find urls, there are a lot of complicated regular expressions out there.
        // Here, we KISS (keep it simple stupid).
        // https://stackoverflow.com/questions/5713558/detect-and-extract-url-from-a-string
        // http://www.regexguru.com/2008/11/detecting-urls-in-a-block-of-text/
        // https://engineering.linkedin.com/blog/2016/06/open-sourcing-url-detector--a-java-library-to-detect-and-normali
        // https://stackoverflow.com/questions/161738/what-is-the-best-regular-expression-to-check-if-a-string-is-a-valid-url
        // https://mathiasbynens.be/demo/url-regex
        // http://www.ex-parrot.com/~pdw/Mail-RFC822-Address.html
        if (!str.contains(subString1) && !str.contains(subString2) && !str.contains(subString3)) {
            return str;
        } else {
            StringBuilder sb = new StringBuilder();
            boolean isMail = false;
            
            // Index what is before the URL, including the "http(s)://".
            int length = 0;
            int index = str.indexOf(subString1);
            if (index >= 0) {
                length = subString1.length();
            } else {
                index = str.indexOf(subString2);
                if (index >= 0) {
                    length = subString2.length();
                } else {
                    // Thanks to the first if, we know that either subString1, 2 or 3 is present.
                    isMail = true;
                    index = str.indexOf(subString3);
                    length = subString3.length();
                }
            }
            sb.append(str.substring(0, index + length));
            str = str.substring(index + length);
            
            if (isMail) {
                // We are parsing a "mailto:..."
                int indexQuestion = str.indexOf("?");
                if (indexQuestion != -1) {
                    // A '?' in a "mailto:..." usually indicates parameters passed, we can drop them.
                    str = str.substring(0, indexQuestion);
                }
                if (str.length() <= subURLMaxSizeToIndex) {
                    // Index only smaller than subURLMaxSizeToIndex subURLs.
                    sb.append(str);
                }
                
            } else {
                // We are parsing a "http(s)://..."
                // Always index the base URL (between "http(s)://" and the first '/').
                index = str.indexOf("/");
                if (index == -1) {
                    sb.append(str);
                } else {
                    sb.append(str.substring(0, index + 1));
                    str = str.substring(index + 1);
                }
                
                // For every pair of '/', check what is inside.
                while (index >= 0) {
                    index = str.indexOf("/");
                    if (index == -1) {
                        // If no more '/' are found in the URL, we are in the last part of it.
                        int indexQuestion = str.indexOf("?");
                        if (indexQuestion != -1) {
                            // A '?' in the last part of the URL usually indicates parameters passed to a php page, we can drop them.
                            str = str.substring(0, indexQuestion);
                        }
                        if (str.length() <= subURLMaxSizeToIndex) {
                            // Index only smaller than subURLMaxSizeToIndex subURLs.
                            sb.append(str);
                        }
                    } else {
                        if (index <= subURLMaxSizeToIndex) {
                            // Index only smaller than subURLMaxSizeToIndex subURLs.
                            sb.append(str.substring(0, index + 1));
                        }
                        str = str.substring(index + 1);
                    }
                }
            }
            
            return sb.toString();
        }
    }
}
