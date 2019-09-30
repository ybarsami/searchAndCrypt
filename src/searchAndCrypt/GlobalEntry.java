/**
 * An entry of the index, as stored in the memory.
 */

package searchAndCrypt;

import java.util.Comparator;
import java.util.TreeSet;

import org.apache.commons.collections.primitives.ArrayIntList;

/**
 *
 * @author yann
 */
public class GlobalEntry implements Comparable<GlobalEntry> {
    
    public String name;           // A name found in the database.
    public ArrayIntList mailList; // A set of the form (id mail1, id mail2, ...).
    
    
    /**
     * Creates a new instance of GlobalEntry.
     */
    public GlobalEntry(String name, ArrayIntList mailList) {
        this.name     = name;
        this.mailList = mailList;
    }
    public GlobalEntry(String name) {
        this(name, new ArrayIntList());
    }
    public GlobalEntry() {
        this("");
    }
    
    public TreeSet<Integer> toTreeSet() {
        final TreeSet<Integer> treeSet = new TreeSet<>();
        for (int i = 0; i < mailList.size(); i++) {
            treeSet.add(mailList.get(i));
        }
        return treeSet;
    }
    
    @Override
    public String toString() { return name; }
    
    @Override
    public int compareTo(GlobalEntry e) {
        return name.compareTo(e.name);
    }
    
    public static final Comparator<GlobalEntry> comparatorOnNames =
            (GlobalEntry e1, GlobalEntry e2) -> e1.name.compareTo(e2.name);
    
    public static final Comparator<GlobalEntry> comparatorOnOccurrences =
            (GlobalEntry e1, GlobalEntry e2) -> e1.mailList.size() - e2.mailList.size();
}
