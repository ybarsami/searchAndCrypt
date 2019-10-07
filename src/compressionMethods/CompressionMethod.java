package compressionMethods;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.apache.commons.collections.primitives.ArrayIntList;

/**
 *
 * @author yann
 */
public abstract class CompressionMethod {
    
    public static CompressionMethod createCompressionMethod(String indexType, int nbMails) {
        switch(indexType) {
            case "binary32":
                return new MethodBinary32();
            case "binary":
                return new MethodBinary(nbMails);
            case "delta":
                return new MethodDelta();
            case "gamma":
                return new MethodGamma();
            case "interpolative":
                return new MethodInterpolative(nbMails);
            default:
                System.out.println(indexType + " is not a valid index type.");
                System.out.println("Valid index types are: binary32, binary, "
                        + "delta, gamma, interpolative.");
                return null;
        }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // An index compression method converts a mailList to and from a file.
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * Writes the mailList in a dataOutputStream, by first encoding them with
     * the given compression method. All the numbers in the mailList are
     * supposed to be in sorted order.
     */
    public abstract void writeMailList(DataOutputStream dataOutputStream, ArrayIntList mailList);
    
    /*
     * Read a dataInputStream to extract nbMailsLocal mails encoded with the
     * given compression method.
     */
    public abstract ArrayIntList readMailList(DataInputStream dataInputStream, int nbMailsLocal);

}
