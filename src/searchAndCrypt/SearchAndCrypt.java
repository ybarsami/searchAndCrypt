/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package searchAndCrypt;

import java.io.File;

import java.util.ArrayList;

/**
 *
 * @author yann
 */
public class SearchAndCrypt {
    
    public static final boolean isDebugMode = true;
    
    public static int EXPORT_NONE            = 0;
    public static int EXPORT_LEXICON         = 1 << 0;
    public static int EXPORT_ASCII           = 1 << 1;
    public static int EXPORT_BINARY32        = 1 << 2;
    public static int EXPORT_BINARY          = 1 << 3;
    public static int EXPORT_DELTA           = 1 << 4;
    public static int EXPORT_GAMMA           = 1 << 5;
    public static int EXPORT_INTERPOLATIVE   = 1 << 6;
    public static int EXPORT_VARIABLE_BYTE   = 1 << 7;
    public static int EXPORT_VARIABLE_NIBBLE = 1 << 8;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int exportTypes = EXPORT_ASCII;//EXPORT_DELTA; // | EXPORT_BINARY32 | EXPORT_BINARY | EXPORT_VARIABLE_NIBBLE | EXPORT_LEXICON | EXPORT_ASCII;
        boolean hasToIndex = true;
        boolean hasToRequest = false;
        
        ArrayList<Server> testServers = new ArrayList<>();
        testServers.addAll(ServerTest.getTestServers());
        testServers.add(new ServerHugo());
//        testServers.add(new ServerBible());
        
        for (Server server : testServers) {
            final Client client = new Client(server);
            if (((ServerTest)server).getMailSort() == ServerTest.MailSort.FOLDER_NAMES) {
                // Those tests contain files which are not on the MIME format.
                client.mimeParse.setBuilder(null);
            }
            
            if (hasToIndex) {
                // Create new index from scratch.
                client.mimeParse.indexEverything(server, "delta");//, true);
                
            } else {
                // Import the indexes.
                client.mimeParse.loadIndex("binary32");
            }
            
            // Export the indexes.
            if ((exportTypes & EXPORT_LEXICON) != 0) {
                client.mimeParse.exportToFile("lexicon");
            }
            if ((exportTypes & EXPORT_ASCII) != 0) {
                client.mimeParse.exportToFile("binary32", true);
            }
            if ((exportTypes & EXPORT_BINARY32) != 0) {
                client.mimeParse.exportToFile("binary32");
            }
            if ((exportTypes & EXPORT_BINARY) != 0) {
                client.mimeParse.exportToFile("binary");
            }
            if ((exportTypes & EXPORT_DELTA) != 0) {
                client.mimeParse.exportToFile("delta");
            }
            if ((exportTypes & EXPORT_GAMMA) != 0) {
                client.mimeParse.exportToFile("gamma");
            }
            if ((exportTypes & EXPORT_INTERPOLATIVE) != 0) {
                client.mimeParse.exportToFile("interpolative");
            }
            if ((exportTypes & EXPORT_VARIABLE_BYTE) != 0) {
                client.mimeParse.exportToFile("variable_byte");
            }
            if ((exportTypes & EXPORT_VARIABLE_NIBBLE) != 0) {
                client.mimeParse.exportToFile("variable_nibble");
            }
            
            if (hasToRequest) {
                // Do some request.
                final ArrayList<File> answer = client.request.search("coucou");
                for (File file: answer) {
                    System.out.println(file.getName());
                }
            }
        }
    }
    
}
