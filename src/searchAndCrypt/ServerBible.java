/**
 * ServerBible.java
 *
 * Created on 7 sept. 2019
 */

package searchAndCrypt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;
import java.util.TreeSet;

/**
 *
 * @author yann
 */
public class ServerBible extends ServerTest {
    
    // Name of the file that stores the verses.
    public static String fileNameVerses = datasetsFolderName + File.separatorChar +
            "Bible" + File.separatorChar + "pg30.txt";
    
    public class LineNumbers {
        public int lineNumberBegin;
        public int lineNumberEnd;
        
        public LineNumbers(int lineNumberBegin, int lineNumberEnd) {
            this.lineNumberBegin = lineNumberBegin;
            this.lineNumberEnd   = lineNumberEnd;
        }
    }
    
    private String fileNameBijection;                   // Name of the file that stores the bijection idVerse / lineNumberBegin + lineNumberEnd.
    private HashMap<Integer, LineNumbers> mapBijection; // HashMap that stores the bijection idVerse / lineNumberBegin + lineNumberEnd.
    
    /**
     * Creates a new instance of ServerBible.
     */
    public ServerBible() {
        super("Bible", "english");
        fileNameBijection = "Bible_bijection.txt";
        updateMap();
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Interaction : Client <- Server.
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * Get all the e-mail identifiers that have identifier >= minIdentifier.
     */
    @Override
    public TreeSet<Integer> getAllMessageIdentifiers(int minIdentifier) {
        TreeSet<Integer> allMessageIdentifiers = new TreeSet<>();
        for (int i : mapBijection.keySet()) {
            if (i >= minIdentifier) {
                allMessageIdentifiers.add(i);
            }
        }
        return allMessageIdentifiers;
    }
    
    /*
     * Get the verse which has the given identifier.
     *
     * @param idMessage, the identifier of the wanted verse.
     */
    @Override
    public File getMessage(int idMessage) {
        int lineNumberBegin = mapBijection.get(idMessage).lineNumberBegin;
        int lineNumberEnd = mapBijection.get(idMessage).lineNumberEnd;
        String tmpFilename = "tmpDoc.txt";
        try (FileWriter out = new FileWriter(tmpFilename)) {
            
            // Read file, extract the correct verse and get the text.
            StringBuilder sb = new StringBuilder();
            try (FileReader fileReader = new FileReader(fileNameVerses);
                    BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line;
                int idLine = 0;
                while (bufferedReader.readLine() != null && idLine < lineNumberBegin) {
                    idLine++;
                }
                while ((line = bufferedReader.readLine()) != null && idLine < lineNumberEnd) {
                    sb.append(line).append("\n");
                    idLine++;
                }
                out.write(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(tmpFilename);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Local computation by the Server (Bijection idMsg / mails)
    ////////////////////////////////////////////////////////////////////////////
    
    public final static String separatorBijection = " --- ";
    
    /*
     * Export the bijection idVerse / lineNumberBegin + lineNumberEnd.
     */
    @Override
    public final void exportBijectionIdentifierMailFile() {
        try (FileWriter out = new FileWriter(fileNameBijection)) {
            for (Integer i: mapBijection.keySet()) {
                final int lineNumberBegin = mapBijection.get(i).lineNumberBegin;
                final int lineNumberEnd = mapBijection.get(i).lineNumberEnd;
                out.write(i + separatorBijection + lineNumberBegin + separatorBijection + lineNumberEnd + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * Import the bijection idVerse / lineNumberBegin + lineNumberEnd.
     */
    @Override
    public final void importBijectionIdentifierMailFile() {
        mapBijection = new HashMap<>();
        try (FileReader fileReader = new FileReader(fileNameBijection);
                BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            
            while ((line = bufferedReader.readLine()) != null) {
                int position = line.indexOf(separatorBijection);
                String stringIndex = line.substring(0, position);
                String stringLineNumbers = line.substring(position + separatorBijection.length());
                position = stringLineNumbers.indexOf(separatorBijection);
                String stringLineNumberBegin = stringLineNumbers.substring(0, position);
                String stringLineNumberEnd = stringLineNumbers.substring(position + separatorBijection.length());
                mapBijection.put(Integer.parseInt(stringIndex), new LineNumbers(
                        Integer.parseInt(stringLineNumberBegin), Integer.parseInt(stringLineNumberEnd)));
            }
        } catch (IOException e) {
            System.out.println("IOException in file '" + fileNameBijection + "'.");
            e.printStackTrace();
        }
    }
    
    private void computeBijectionIdentifierMailFile() {
        mapBijection = new HashMap<>();
        int nbVersesTotal = 31102; // Among 66 books
        try (FileReader fileReader = new FileReader(fileNameVerses);
                BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            int idVerse = 1;
            int idLine = 0;
            while (bufferedReader.readLine() != null && idLine < 94) {
                // Beginning of the book "Genesis"
                idLine++;
            }
            int lineNumberBegin = 0;
            while ((line = bufferedReader.readLine()) != null && idVerse < nbVersesTotal) {
                if (line.length() >= 11) {
                    if (line.charAt(2) == ':' && line.charAt(6) == ':') {
                        lineNumberBegin = idLine;
                    }
                } else if (lineNumberBegin > 0) {
                    mapBijection.put(idVerse, new LineNumbers(lineNumberBegin, idLine));
                    lineNumberBegin = 0;
                    idVerse++;
                }
                idLine++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * Whenever a new mail comes or an old mail is deleted, updates the map
     * between the e-mails and the identifiers (as managed in the inverted
     * lists).
     */
    @Override
    public void updateMap() {
        // TODO : for now, this just handles the base case where we compute the map from scratch.
        File tempFile = new File(fileNameBijection);
        if (tempFile.exists()) {
            System.out.println(fileNameBijection + " already exists, I will use it.");
            importBijectionIdentifierMailFile();
        } else {
            computeBijectionIdentifierMailFile();
            exportBijectionIdentifierMailFile();
        }
    }
}
