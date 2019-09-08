/**
 * ServerHugo.java
 *
 * Created on 8 sept. 2019
 */

package searchAndCrypt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

/**
 *
 * @author yann
 */
public class ServerHugo extends ServerTest {
    
    private static int nbLinePerDoc = 30;
    
    // Name of the folder that stores the books.
    public static String folderNameBooks = datasetsFolderName + File.separatorChar +
            "Hugo";
    
    public class FileAndLineNumber {
        public File file;
        public int  lineNumber;
        
        public FileAndLineNumber(File file, int lineNumber) {
            this.file       = file;
            this.lineNumber = lineNumber;
        }
    }
    
    private String fileNameBijection;                         // Name of the file that stores the bijection idDoc / fileName + lineNumber.
    private HashMap<Integer, FileAndLineNumber> mapBijection; // HashMap that stores the bijection idDoc / fileName + lineNumber.
    
    /**
     * Creates a new instance of ServerHugo.
     */
    public ServerHugo() {
        super("Hugo", "french");
        fileNameBijection = "Hugo_bijection.txt";
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
        File file = mapBijection.get(idMessage).file;
        int lineNumber = mapBijection.get(idMessage).lineNumber;
        String tmpFilename = "tmpDoc.txt";
        try (FileWriter out = new FileWriter(tmpFilename)) {
            
            // Read file, extract the correct verse and get the text.
            StringBuilder sb = new StringBuilder();
            try (FileReader fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line;
                int idLine = 0;
                while (bufferedReader.readLine() != null && idLine < lineNumber) {
                    idLine++;
                }
                while ((line = bufferedReader.readLine()) != null && idLine < lineNumber + nbLinePerDoc) {
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
     * Export the bijection idDoc / fileName + lineNumber.
     */
    @Override
    public final void exportBijectionIdentifierMailFile() {
        final File folderBooks = new File(folderNameBooks);
        final String folderBooksAbsolutePath = folderBooks.getAbsolutePath();
        try (FileWriter out = new FileWriter(fileNameBijection)) {
            for (Integer i: mapBijection.keySet()) {
                // We take the absolute path
                final String docAbsolutePath = mapBijection.get(i).file.getAbsolutePath();
                final String docRelativePath = docAbsolutePath.substring(folderBooksAbsolutePath.length() + 1);
                out.write(i + separatorBijection + docRelativePath +
                              separatorBijection + mapBijection.get(i).lineNumber + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * Import the bijection idDoc / fileName + lineNumber.
     */
    @Override
    public final void importBijectionIdentifierMailFile() {
        mapBijection = new HashMap<>();
        final File folderBooks = new File(folderNameBooks);
        final String folderBooksAbsolutePath = folderBooks.getAbsolutePath();
        try (FileReader fileReader = new FileReader(fileNameBijection);
                BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            
            while ((line = bufferedReader.readLine()) != null) {
                int position = line.indexOf(separatorBijection);
                String stringIndex = line.substring(0, position);
                String stringFilenameAndLineNumber = line.substring(position + separatorBijection.length());
                position = stringFilenameAndLineNumber.indexOf(separatorBijection);
                String stringFilename = stringFilenameAndLineNumber.substring(0, position);
                String stringLineNumber = stringFilenameAndLineNumber.substring(position + separatorBijection.length());
                mapBijection.put(Integer.parseInt(stringIndex), new FileAndLineNumber(
                        new File(folderBooksAbsolutePath + File.separatorChar + stringFilename),
                        Integer.parseInt(stringLineNumber)));
            }
        } catch (IOException e) {
            System.out.println("IOException in file '" + fileNameBijection + "'.");
            e.printStackTrace();
        }
    }
    
    private void computeBijectionIdentifierMailFile() {
        mapBijection = new HashMap<>();
        final File folderBooks = new File(folderNameBooks);
        File[] files = folderBooks.listFiles();
        Arrays.sort(files);
        int idDoc = 1;
        for (final File fileBook : files) {
            try (FileReader fileReader = new FileReader(fileBook);
                    BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line;
                int idLine = 0;
                while ((line = bufferedReader.readLine()) != null) {
                    if (idLine % nbLinePerDoc == 0) {
                        mapBijection.put(idDoc, new FileAndLineNumber(fileBook, idLine));
                        idDoc++;
                    }
                    idLine++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
