/**
 * GlobalIndex.java
 *
 * Created on 16 janv. 2019
 */

package searchAndCrypt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.TreeSet;

import org.apache.commons.collections.primitives.ArrayIntList;

/**
 *
 * @author yann
 */
public class GlobalIndex {
    
    public TreeSet<GlobalEntry> set; // A set of entries.
    int nbMails;
    
    /*
     * Stores a GlobalEntry to be updated each time we access updateWithNewWord,
     * in order to avoid creating too much new objects and needing to call the
     * garbage collector too often.
     *
     * See https://stackoverflow.com/questions/1393486/error-java-lang-outofmemoryerror-gc-overhead-limit-exceeded/5640498#5640498
     */
    private GlobalEntry newEntry;
    
    
    /**
     * Creates a new instance of GlobalIndex.
     */
    public GlobalIndex(TreeSet<GlobalEntry> set) {
        this.set = set;
        newEntry = new GlobalEntry();
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
     * N.B.: call this method with care, as it will create a new GlobalEntry,
     * thus create a new ArrayIntList, thus potentially give impossible
     * work for the garbage collector.
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
        newEntry.name = word;
        GlobalEntry oldEntry = find(newEntry);
        
        if (oldEntry == null) {
            // If the word is not in the index, we create a new entry and add it to the index.
            ArrayIntList mailList = new ArrayIntList();
            mailList.add(idMessage);
            GlobalEntry addedEntry = new GlobalEntry(word, mailList);
            this.add(addedEntry);
            
        } else {
            // If it was already in the index, we update its associated entry.
            // Add the mail to the associated list, if it's not already there.
            // Here we use the fact that we treat the mails one by one, so if
            // this message is already in the list, it is in last position.
            // (we do not need to go through the mails in any particular order)
            if (oldEntry.mailList.get(oldEntry.mailList.size() - 1) != idMessage) {
                oldEntry.mailList.add(idMessage);
            }
        }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Import / export from files.
    ////////////////////////////////////////////////////////////////////////////
    
    public File exportToFile(String filename, String indexType, boolean includeLexicon, boolean includePostingLists, boolean inASCII) {
        if (inASCII) {
            try (FileWriter out = new FileWriter(filename)) {
                out.write("# Inverted index : for each word, the ids of documents that contain this word" +
                        " (preceded by the number of documents, in parentheses).\n");
                out.write("# In total, " + set.size() + " words.\n");
                out.write("# In total, " + nbMails + " mails.\n");
                for (GlobalEntry entry : set) {
                    if (includeLexicon) {
                        out.write(entry.name + " (" + entry.mailList.size() + ")");
                    }
                    if (includePostingLists) {
                        Tools.writeMailListASCII(indexType, out, nbMails, entry.mailList);
                    }
                    out.write("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new File(filename);
        } else {
            try (FileOutputStream fout = new FileOutputStream(filename);
                    DataOutputStream out = new DataOutputStream(fout)) {
                out.writeInt(set.size());
                out.writeInt(nbMails);
                for (GlobalEntry entry : set) {
                    if (includeLexicon) {
                        short nameLength = (short)entry.name.length();
                        out.writeShort(nameLength);
                        // writeChars will write every character as an int (4 bytes)
                        // out.writeChars(entry.name);
                        // so we convert it to one byte instead (names are guaranteed to be ASCII)
                        for (int j = 0; j < nameLength; j++)
                            out.writeByte(entry.name.charAt(j));
                        out.writeInt(entry.mailList.size());
                    }
                    if (includePostingLists) {
                        Tools.writeMailList(indexType, out, nbMails, entry.mailList);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new File(filename);
        }
    }
    
    public File exportToFile(String filename, String indexType, boolean includeLexicon, boolean includePostingLists) {
        return exportToFile(filename, indexType, includeLexicon, includePostingLists, false);
    }
    
    /*
     * Export to file --- only the lexicon.
     * Structure of the file :
     *     - one int (number of different words in the dataset)
     *     - for each word in the dataset:
     *         - one short l (length of the word)
     *         - s bytes (the characters of the word)
     *         - one int n (number of documents that contain this word)
     */
    public File exportToFileLexiconOnly(String filename, boolean inASCII) {
        return exportToFile(filename, "", true, false, inASCII);
    }
    
    /*
     * Export to file.
     * Structure of the file depends of the index type.
     */
    public File exportToFile(String filename, String indexType, boolean inASCII) {
        return exportToFile(filename, indexType, true, true, inASCII);
    }
    
    /*
     * Import from file.
     * Structure of the file depends of the index type.
     */
    public void importFromFile(File file, String indexType, boolean inASCII) {
        set.clear();
        if (inASCII) {
            try (FileReader fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line;
                bufferedReader.readLine();
                line = bufferedReader.readLine();
                int nbNames = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                line = bufferedReader.readLine();
                nbMails = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                for (int i = 0; i < nbNames; i++) {
                    GlobalEntry entry = new GlobalEntry();
                    line = bufferedReader.readLine();
                    String[] splitLine = line.split(" ");
                    entry.name = splitLine[0];
                    int nbMailsLocal = Integer.parseInt(splitLine[1].replaceAll("[^0-9]", ""));
                    if (indexType.equals("binary32")) {
                        for (int idMail = 0; idMail < nbMailsLocal; idMail++) {
                            entry.mailList.add(Integer.parseInt(splitLine[idMail + 2]));
                        }
                    } else {
                        Tools.readCodeListASCII(indexType, splitLine[1], nbMails, nbMailsLocal, entry.mailList);
                    }
                    set.add(entry);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else try (FileInputStream fin = new FileInputStream(file);
                DataInputStream in = new DataInputStream(fin)) {
            int nbNames = in.readInt();
            nbMails = in.readInt();
            for (int i = 0; i < nbNames; i++) {
                GlobalEntry entry = new GlobalEntry();
                short nameLength = in.readShort();
                entry.name = "";
                for (int j = 0; j < nameLength; j++)
                    entry.name += (char)in.readByte();
                int nbMailsLocal = in.readInt();
                Tools.readCodeList(indexType, in, nbMails, nbMailsLocal, entry.mailList);
                set.add(entry);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to open file '" + file.getName() + "'.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void importFromFile(File file, String indexType) {
        importFromFile(file, indexType, false);
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
    public File importAndMerge(File fileChunk1, File fileChunk2, String indexType, boolean inASCII) {
        set.clear();
        if (inASCII) {
            try (FileReader fileReader1 = new FileReader(fileChunk1);
                    BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
                    FileReader fileReader2 = new FileReader(fileChunk2);
                    BufferedReader bufferedReader2 = new BufferedReader(fileReader2)) {
                String line1, line2;
                bufferedReader1.readLine();
                bufferedReader2.readLine();
                line1 = bufferedReader1.readLine();
                line2 = bufferedReader2.readLine();
                int nbNames1 = Integer.parseInt(line1.replaceAll("[^0-9]", ""));
                int nbNames2 = Integer.parseInt(line2.replaceAll("[^0-9]", ""));
                line1 = bufferedReader1.readLine();
                line2 = bufferedReader2.readLine();
                int nbMails1 = Integer.parseInt(line1.replaceAll("[^0-9]", ""));
                int nbMails2 = Integer.parseInt(line2.replaceAll("[^0-9]", ""));
                nbMails = nbMails1 > nbMails2 ? nbMails1 : nbMails2;
                int i1 = 0, i2 = 0;
                boolean readFile1 = true, readFile2 = true;
                String name1 = "", name2 = "";
                int nbMailsLocal1 = 0, nbMailsLocal2 = 0;
                ArrayIntList mailList1 = new ArrayIntList(), mailList2 = new ArrayIntList();
                // The two files have their names sorted in ascending order.
                while (i1 + i2 < nbNames1 + nbNames2) {
                    GlobalEntry entry = new GlobalEntry();
                    if (readFile1) {
                        line1 = bufferedReader1.readLine();
                        String[] splitLine = line1.split(" ");
                        name1 = splitLine[0];
                        nbMailsLocal1 = Integer.parseInt(splitLine[1].replaceAll("[^0-9]", ""));
                        mailList1 = new ArrayIntList();
                        if (indexType.equals("binary32")) {
                            for (int idMail = 0; idMail < nbMailsLocal1; idMail++) {
                                mailList1.add(Integer.parseInt(splitLine[idMail + 2]));
                            }
                        } else {
                            Tools.readCodeListASCII(indexType, splitLine[1], nbMails1, nbMailsLocal1, mailList1);
                        }
                    }
                    if (readFile2) {
                        line2 = bufferedReader2.readLine();
                        String[] splitLine = line2.split(" ");
                        name2 = splitLine[0];
                        nbMailsLocal2 = Integer.parseInt(splitLine[1].replaceAll("[^0-9]", ""));
                        mailList2 = new ArrayIntList();
                        if (indexType.equals("binary32")) {
                            for (int idMail = 0; idMail < nbMailsLocal2; idMail++) {
                                mailList2.add(Integer.parseInt(splitLine[idMail + 2]));
                            }
                        } else {
                            Tools.readCodeListASCII(indexType, splitLine[1], nbMails2, nbMailsLocal2, mailList2);
                        }
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
            } catch (IOException e) {
                e.printStackTrace();
            }
            return exportToFile("tmpIndex.txt", indexType, inASCII);
        } else try (FileInputStream fin1 = new FileInputStream(fileChunk1);
                DataInputStream in1 = new DataInputStream(fin1);
                FileInputStream fin2 = new FileInputStream(fileChunk2);
                DataInputStream in2 = new DataInputStream(fin2)) {
            int nbNames1 = in1.readInt();
            int nbMails1 = in1.readInt();
            int nbNames2 = in2.readInt();
            int nbMails2 = in2.readInt();
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
                    mailList1 = new ArrayIntList();
                    Tools.readCodeList(indexType, in1, nbMails1, nbMailsLocal1, mailList1);
                }
                if (readFile2) {
                    nameLength2 = in2.readShort();
                    name2 = "";
                    for (int j = 0; j < nameLength2; j++)
                        name2 += (char)in2.readByte();
                    nbMailsLocal2 = in2.readInt();
                    mailList2 = new ArrayIntList();
                    Tools.readCodeList(indexType, in2, nbMails2, nbMailsLocal2, mailList2);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exportToFile("tmpIndex.txt", indexType, inASCII);
    }
}
