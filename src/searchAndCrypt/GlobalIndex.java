/**
 * The index is stored as a (compressed and encrypted) file on the server.
 * But, while the index is constructed, updated, and searched, it has to be
 * stored in memory. This is the data structure that handles this.
 * In memory, an index knows:
 *    > the maximum e-mail identifier of an indexed e-mail
 *    > a set of (word, list of identifiers): for each indexed word, the list of
 *      e-mail identifiers whose corresponding e-mails contain this word
 */

package searchAndCrypt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.util.TreeSet;

import org.apache.commons.collections.primitives.ArrayIntList;

import compressionMethods.CompressionMethod;

/**
 *
 * @author yann
 */
public class GlobalIndex {
    
    public TreeSet<GlobalEntry> set; // A set of entries.
    int nbMails;
    
    
    /**
     * Creates a new instance of GlobalIndex.
     */
    public GlobalIndex(TreeSet<GlobalEntry> set) {
        this.set = set;
        nbMails = 0;
    }
    public GlobalIndex() {
        this(new TreeSet<>());
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Standard data structure to find and add a new Entry in log(N) time.
    // Keeps the data structure sorted at all times.
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * Searches this.set for a GlobalEntry with word as name.
     *
     * @param  word the name to be searched for.
     * @return the GlobalEntry with the searched name, if it is contained in
     *         this.set; null if it is not.
     */
    public GlobalEntry find(String word) {
        return find(new GlobalEntry(word));
    }
    
    /*
     * Searches this.set for a GlobalEntry with the same name as entry.name.
     *
     * @param  word the name to be searched for.
     * @return the GlobalEntry with the searched name, if it is contained in
     *         this.set; null if it is not.
     */
    public GlobalEntry find(GlobalEntry entry) {
        final GlobalEntry floor = set.floor(entry);
        if (floor != null && floor.name.equals(entry.name)) {
            return floor;
        } else {
            return null;
        }
    }
    
    public void add(GlobalEntry newEntry) {
        this.set.add(newEntry);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Update the index.
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * We have a new word to add to the index.
     *
     * @param word, the word to be indexed.
     * @param msgIdBloc, the identifier of the bloc where the message is put.
     */
    public void updateWithNewWord(String word, int idMessage) {
        if (idMessage > nbMails) {
            nbMails = idMessage;
        }
        
        // Check if the word is already in the index.
        GlobalEntry newEntry = new GlobalEntry();
        newEntry.name = word;
        GlobalEntry oldEntry = find(newEntry);
        
        if (oldEntry == null) {
            // If the word is not in the index, we create a new entry and add it to the index.
            ArrayIntList mailList = new ArrayIntList();
            mailList.add(idMessage);
            GlobalEntry addedEntry = new GlobalEntry(word, mailList);
            this.add(addedEntry);
            
        } else {
            // If it was already in the index, we update its associated entry
            // (add the mail to the associated list).
            // Here, we use the fact that when analyzing a String, we return a
            // set of words. Hence, for a given couple(word, idMessage), we will
            // not call this function twice. Thus, we do not need to check if
            // idMessage was previously added in the list.
            oldEntry.mailList.add(idMessage);
        }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Import / export from files.
    ////////////////////////////////////////////////////////////////////////////
    
    private void writeStringWithLength(DataOutputStream out, String str) throws IOException {
        short nameLength = (short)str.length();
        out.writeShort(nameLength);
        // writeChars will write every character as an int (4 bytes)
        // out.writeChars(entry.name);
        // so we convert it to one byte instead (names are guaranteed to be ASCII)
        for (int j = 0; j < nameLength; j++) {
            out.writeByte(str.charAt(j));
        }
    }
    
    public File exportToFile(String filename, String indexType) {
        try (FileOutputStream fout = new FileOutputStream(filename);
                DataOutputStream out = new DataOutputStream(fout)) {
            writeStringWithLength(out, indexType);
            out.writeInt(set.size());
            out.writeInt(nbMails);
            CompressionMethod compressionMethod = CompressionMethod.createCompressionMethod(indexType, nbMails);
            for (GlobalEntry entry : set) {
                writeStringWithLength(out, entry.name);
                out.writeInt(entry.mailList.size());
                compressionMethod.writeMailList(out, entry.mailList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(filename);
    }
    
    private String readStringWithLength(DataInputStream in) throws IOException {
        short nameLength = in.readShort();
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < nameLength; j++) {
            sb.append((char)in.readByte());
        }
        return sb.toString();
    }
    
    /*
     * Import from file.
     * Structure of the file depends of the index type.
     */
    public void importFromFile(File file) {
        set.clear();
        try (FileInputStream fin = new FileInputStream(file);
                DataInputStream in = new DataInputStream(fin)) {
            String indexType = readStringWithLength(in);
            int nbNames = in.readInt();
            nbMails = in.readInt();
            CompressionMethod compressionMethod = CompressionMethod.createCompressionMethod(indexType, nbMails);
            for (int i = 0; i < nbNames; i++) {
                GlobalEntry entry = new GlobalEntry();
                entry.name = readStringWithLength(in);
                int nbMailsLocal = in.readInt();
                entry.mailList = compressionMethod.readMailList(in, nbMailsLocal);
                set.add(entry);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to open file '" + file.getName() + "'.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * Merge two sorted arrayIntLists.
     */
    private static ArrayIntList mergeArrayIntList(ArrayIntList list1, ArrayIntList list2) {
        ArrayIntList mergedList = new ArrayIntList();
        int i1 = 0, i2 = 0;
        while (i1 + i2 < list1.size() + list2.size()) {
            if (i1 == list1.size()) {
                mergedList.add(list2.get(i2));
                i2++;
            } else if (i2 == list2.size()) {
                mergedList.add(list1.get(i1));
                i1++;
            } else {
                int int1 = list1.get(i1);
                int int2 = list2.get(i2);
                // int1 and int2 should be always different in our use, but can be equal in the general case.
                if (int1 < int2) {
                    mergedList.add(int1);
                    i1++;
                } else {
                    mergedList.add(int2);
                    i2++;
                }
            }
        }
        return mergedList;
    }
    
    /*
     * Import from two chunk files, and merge them.
     * Structure of the file depends of the index type.
     */
    public File importAndMerge(File fileChunk1, File fileChunk2) {
        set.clear();
        try (FileInputStream fin1 = new FileInputStream(fileChunk1);
                DataInputStream in1 = new DataInputStream(fin1);
                FileInputStream fin2 = new FileInputStream(fileChunk2);
                DataInputStream in2 = new DataInputStream(fin2)) {
            String indexType1 = readStringWithLength(in1);
            int nbNames1 = in1.readInt();
            int nbMails1 = in1.readInt();
            CompressionMethod compressionMethod1 = CompressionMethod.createCompressionMethod(indexType1, nbMails1);
            String indexType2 = readStringWithLength(in2);
            int nbNames2 = in2.readInt();
            int nbMails2 = in2.readInt();
            CompressionMethod compressionMethod2 = CompressionMethod.createCompressionMethod(indexType2, nbMails2);
            nbMails = nbMails1 > nbMails2 ? nbMails1 : nbMails2;
            int i1 = 0, i2 = 0;
            boolean readFile1 = true, readFile2 = true;
            short nameLength1, nameLength2;
            String name1 = "", name2 = "";
            int nbMailsLocal1 = 0, nbMailsLocal2 = 0;
            ArrayIntList mailList1 = new ArrayIntList(), mailList2 = new ArrayIntList();
            // The two files have their names sorted in ascending order.
            while (i1 + i2 < nbNames1 + nbNames2) {
                GlobalEntry entry = new GlobalEntry();
                if (readFile1) {
                    nameLength1 = in1.readShort();
                    name1 = "";
                    for (int j = 0; j < nameLength1; j++)
                        name1 += (char)in1.readByte();
                    nbMailsLocal1 = in1.readInt();
                    mailList1 = compressionMethod1.readMailList(in1, nbMailsLocal1);
                }
                if (readFile2) {
                    nameLength2 = in2.readShort();
                    name2 = "";
                    for (int j = 0; j < nameLength2; j++)
                        name2 += (char)in2.readByte();
                    nbMailsLocal2 = in2.readInt();
                    mailList2 = compressionMethod2.readMailList(in2, nbMailsLocal2);
                }
                boolean write1 = false, write2 = false;
                if (i2 >= nbNames2) {
                    write1 = true;
                } else if (i1 >= nbNames1) {
                    write2 = true;
                } else if (name1.compareTo(name2) < 0) {
                    write1 = true;
                } else if (name1.compareTo(name2) > 0) {
                    write2 = true;
                } else {
                    write1 = true;
                    write2 = true;
                }
                if (write1 && write2) {
                    entry.name = name1;
                    entry.mailList = mergeArrayIntList(mailList1, mailList2);
                    i1++;
                    i2++;
                    readFile1 = i1 < nbNames1;
                    readFile2 = i2 < nbNames2;
                } else if (write1) {
                    entry.name = name1;
                    entry.mailList = mailList1;
                    i1++;
                    readFile1 = i1 < nbNames1;
                    readFile2 = false;
                } else {
                    entry.name = name2;
                    entry.mailList = mailList2;
                    i2++;
                    readFile1 = false;
                    readFile2 = i2 < nbNames2;
                }
                set.add(entry);
            }
            return exportToFile("tmpIndex.txt", indexType1);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    /*
     * Export to ASCII file --- human readable form.
     * Structure of the file :
     *     - the number of words in the index
     *     - the number of emails in the database
     *     - for each word in the index:
     *         - the word
     *         - the number of emails that contain this word
     *         - the identifiers of emails containing this word
     */
    public File exportToFileASCII(String filename) {
        try (FileWriter out = new FileWriter(filename)) {
            out.write("# Inverted index : for each word, the ids of documents that contain this word" +
                    " (preceded by the number of documents, in parentheses).\n");
            out.write("# In total, " + set.size() + " words.\n");
            out.write("# In total, " + nbMails + " mails.\n");
            for (GlobalEntry entry : set) {
                out.write(entry.name + " (" + entry.mailList.size() + ")");
                for (int i = 0; i < entry.mailList.size(); i++) {
                    out.write(" " + entry.mailList.get(i));
                }
                out.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(filename);
    }
}
