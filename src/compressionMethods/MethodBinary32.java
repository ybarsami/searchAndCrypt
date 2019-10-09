/**
     * Index not compressed: the mail identifiers in plain int format (32 bits).
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
public class MethodBinary32 extends CompressionMethod {
    
    @Override
    public final void writeMailList(DataOutputStream dataOutputStream, ArrayIntList mailList) {
        try {
            for (int i = 0; i < mailList.size(); i++) {
                int idMail = mailList.get(i);
                dataOutputStream.writeInt(idMail);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public ArrayIntList readMailList(DataInputStream dataInputStream, int nbMailsLocal) {
        ArrayIntList mailList = new ArrayIntList();
        try {
            for (int j = 0; j < nbMailsLocal; j++) {
                int idMail = dataInputStream.readInt();
                mailList.add(idMail);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mailList;
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof MethodBinary32;
    }
    
    @Override
    public int hashCode() {
        return MethodBinary32.class.getName().hashCode();
    }

}
