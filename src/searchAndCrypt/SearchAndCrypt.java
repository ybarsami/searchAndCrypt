package searchAndCrypt;

import java.io.File;

import java.util.List;

/**
 *
 * @author yann
 */
public class SearchAndCrypt {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // The index has to be compressed by a specific integer-compressing
        // method to save memory, thus speed when downloading / uploading the
        // index.
        // This library comes with a number of techniques which can be used by
        // choosing one of the following values for indexType: "binary32",
        // "binary", "delta", "gamma", or "interpolative".
        // See CompressionMethod.java and other files inside the package
        // compressionMethods for more details.
        String indexType = "delta";
        
        // Boolean telling whether we have to index the dataset from scratch or
        // if the index is already built and we can just read the generated
        // file.
        boolean hasToIndex = true;
        
        // Boolean telling whether we have to make a request from the index or
        // not.
        boolean hasToRequest = true;
        
        List<Server> testServers = ServerTest.getTestServers();
        
        for (Server server : testServers) {
            final Client client = new Client(server, indexType);
            
            if (hasToIndex) {
                // Create new index from scratch,
                client.indexEverything(server, indexType);
                
                // and export it to the server...
                client.exportToFile(indexType);
                
            } else {
                // ... or import the index from server.
                client.loadIndex(indexType);
            }
            
            if (hasToRequest) {
                // Do some request.
                String requestedString = "hello";
                final List<File> answer = client.search(requestedString);
                System.out.println("The request \"" + requestedString + "\" gives the following results:");
                for (File file: answer) {
                    System.out.println(file.getAbsolutePath());
                }
            }
        }
    }
    
}
