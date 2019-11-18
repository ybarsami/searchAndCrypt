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
        // Boolean telling whether we have to index the dataset from scratch or
        // if the index is already built and we can just read the generated
        // file.
        boolean hasToIndex = true;
        
        // Boolean telling whether we have to make a request from the index or
        // not.
        boolean hasToRequest = true;
        
        List<Server> testServers = ServerLocal.getTestServers();
        
        for (Server server : testServers) {
            final Client client = new Client(server);
            
            if (hasToIndex) {
                // Create new index from scratch,
                client.updateIndex();
                
                // and export it to the server...
                client.exportToFile();
                
            } else {
                // ... or import the index from server.
                client.loadIndex();
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
