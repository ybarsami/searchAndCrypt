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
        // Valid index types are: binary32, binary, delta, gamma, interpolative.
        // See IndexCompressionMethod.java for more details.
        String indexType = "delta";
        
        // Boolean telling whether we have to index the dataset from scratch or
        // if the index is already built and we can just read the generated
        // file.
        boolean hasToIndex = true;
        
        // Boolean telling whether we have to make a request from the index or
        // not.
        boolean hasToRequest = false;
        
        List<Server> testServers = ServerTest.getTestServers();
        
        for (Server server : testServers) {
            final Client client = new Client(server, indexType);
            
            if (hasToIndex) {
                // Create new index from scratch.
                client.indexEverything(server, indexType);
                
            } else {
                // Import the indexes.
                client.loadIndex(indexType);
            }
            
            // Export the index.
            client.exportToFile(indexType);
            
            if (hasToRequest) {
                // Do some request.
                final List<File> answer = client.search("coucou");
                for (File file: answer) {
                    System.out.println(file.getName());
                }
            }
        }
    }
    
}
