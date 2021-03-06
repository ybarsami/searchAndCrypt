/**
 * Example of use of the abstract Server API, in the context of a local
 * Server where all the e-mails are stored in a specific folder (and in
 * sub-folders from this location).
 */

package searchAndCrypt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author yann
 */
public class ServerLocal extends Server {
    
    // The folder where the test datasets are located, relatively to the home
    // path of the library.
    public final static String datasetsFolderName = "datasets";
    
    /*
     * A MailSet describes a dataset consisting of e-mails.
     */
    public static class MailSet {
        
        private final String language;   // Language of the e-mails
        private final String name;       // Name for the dataset.
        private final String folderName; // Folder where the dataset is located.
        
        // Constructor
        MailSet(String language, String name, String folderName) {
            this.language = language;
            this.name = name;
            this.folderName = folderName;
        }
        MailSet(String user) {
            this("english", "Enron_" + user, datasetsFolderName + File.separatorChar + user);
        }
        
        public String getLanguage() { return language; }
        public String getName() { return name; }
        public String getFolderName() { return folderName; }
    }
    
    /* Two examples of datasets, taken from the Enron corpus of e-mails.
     * B. Klimt and Y. Yang. "The Enron Corpus: A New Dataset for Email
     * Classification Research". 2004.
     * http://dx.doi.org/10.1007/978-3-540-30115-8_22
     * We cleaned the original folders by removing the e-mails which were there
     * multiple times, as suggested in the article. The allen-p dataset contains
     * 1410 e-mails and the dasovich-j dataset contains 15748 e-mails.
     */
    public static MailSet MAILS_TEST1 = new MailSet("allen-p");
    public static MailSet MAILS_TEST2 = new MailSet("dasovich-j");
    
    private String folderNameMails;              // Name of the folder that stores the mails.
    private String folderNameIndex;              // Name of the folder that stores the index.
    private String fileNameIndex;                // Base name of the global index files, without extension.
    private String fileExtension;                // Extension of the files representing the indexes.
    private int nbIndexChunks;                   // Number of chunks of the full index currently stored.
    
    private String fileNameBijection;            // Name of the file that stores the bijection idMsg / mailName.
    private HashMap<Integer, File> mapBijection; // HashMap that stores the bijection idMsg / mailFile.
    
    private String getIndexFileName() {
        return fileNameIndex + "." + fileExtension;
    }
    private String getChunkedIndexFileName(int idChunk) {
        return fileNameIndex + "_chunk" + idChunk + "." + fileExtension;
    }
    private String getIndexFilePathName() {
        return folderNameIndex + File.separatorChar + getIndexFileName();
    }
    private String getChunkedIndexFilePathName(int idChunk) {
        return folderNameIndex + File.separatorChar + getChunkedIndexFileName(idChunk);
    }
    
    
    /**
     * Creates a new instance of ServerLocal.
     */
    public ServerLocal(MailSet mailsType) {
        fileNameIndex = "test_index";
        fileExtension = "txt";
        nbIndexChunks = 0;
        maxIdIndexedMail = 0;
        
        loadMails(mailsType);
        setLanguage(mailsType.getLanguage());
    }
    
    /*
     * Returns a list of test servers.
     */
    public static final List<Server> getTestServers() {
        // Here, just the "allen-p" dataset will be tested.
        // You can also pu the dataset "dasovich-j" which contains more e-mails.
        // Those two datasets are e-mail boxes extracted from the Enron dataset.
        MailSet[] mailsTypes = { MAILS_TEST1 };//, MAILS_TEST2 };
        List<Server> serverList = new ArrayList<>();
        for (MailSet mailsType : mailsTypes) {
            serverList.add(new ServerLocal(mailsType));
        }
        return serverList;
    }
    
    /*
     * 
     */
    private void loadMails(MailSet mailsType) {
        folderNameMails = mailsType.getFolderName();
        folderNameIndex   = mailsType.getName() + "_test";
        fileNameBijection = mailsType.getName() + "_bijection.txt";
        updateMap();
    }
    
    /*
     * Get the number of chunks of the full index currently stored on this
     * server.
     */
    @Override
    public int getNbIndexChunks() {
        return nbIndexChunks;
    }
    
    private int maxIdIndexedMail;
    
    /*
     * Get the maximum identifier of an e-mail indexed on the server.
     */
    @Override
    public int getMaxIdIndexedMail() {
        return maxIdIndexedMail;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Interaction : Client <- Server.
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * Get all the e-mail identifiers that have identifier >= minIdentifier.
     *
     * @return the wanted e-mail identifiers, as a sorted set.
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
     * Get the e-mail which has the given identifier.
     *
     * @param idMessage, the identifier of the wanted e-mail.
     * @return the wanted message.
     */
    @Override
    public File getMessage(int idMessage) {
        return mapBijection.get(idMessage);
    }
    
    /*
     * Get the index.
     *
     * @return the file containing the compressed index.
     */
    @Override
    public File getIndexFile() {
        String filename = getIndexFilePathName();
        return new File(filename);
    }
    
    private int getIndexFirstOfTwoChunks() {
        int indexMin = -1;
        long minSize = Integer.MAX_VALUE;
        for (int i = 0; i < nbIndexChunks / 2; i++) {
            String filename1 = getChunkedIndexFilePathName(i);
            String filename2 = getChunkedIndexFilePathName(i + 1);
            long sizeOfTwoChunks = new File(filename1).length() + new File(filename2).length();
            if (minSize > sizeOfTwoChunks) {
                minSize = sizeOfTwoChunks;
                indexMin = i;
            }
        }
        return indexMin;
    }
    
    /*
     * Get the two consecutive chunks with minimum file size.
     *
     * @return the two consecutive chunks with minimum file size.
     */
    @Override
    public List<File> getTwoChunksIndex() {
        if (nbIndexChunks <= 1) {
            return null;
        }
        int indexMin = getIndexFirstOfTwoChunks();
        ArrayList<File> twoChunks = new ArrayList<>();
        String filename1 = getChunkedIndexFilePathName(indexMin);
        String filename2 = getChunkedIndexFilePathName(indexMin + 1);
        twoChunks.add(new File(filename1));
        twoChunks.add(new File(filename2));
        return twoChunks;
    }
    
    /*
     * Get the list of mails removed from the server since last connexion.
     * The client can then update the index by removing those e-mails from the
     * posting lists (thus, gaining some space).
     *
     * @return the list of mails removed from the server since last connexion.
     */
    @Override
    public List<Integer> getRemovedMailIds() {
        // WARNING: ServerLocal is a *static* Server that does not handle
        // dynamic removal of e-mails.
        return new ArrayList<>();
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Interaction : Client -> Server.
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * Updates the index with a new version.
     *
     * @param newIndexFile, a new version of the index that will replace the
     * current one.
     * @return true iff the index has successfully be replaced.
     */
    @Override
    public boolean updateIndexFile(File newIndexFile) {
        new File(folderNameIndex).mkdirs();
        String filename = getIndexFilePathName();
        try {
            Files.copy(newIndexFile.toPath(),
                (new File(filename)).toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("IOException in file '" + filename + "'.");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    /*
     * Receives a new chunk of the index --- to avoid needing to build the full
     * index before sending it.
     *
     * @param chunkedIndexFile, a chunk of the index that will added to the
     *        current ones.
     * @param maxIdMail, the maximum index of the mail contained in the chunk.
     *        If set to -1, this is the result of two chunks currently on
     *        the server (in this case, replace them with the chunk), otherwise
     *        this is a new chunk (in this case, add the chunk to the list).
     * @return true iff the chunked index has been successfully added.
     */
    @Override
    public boolean addChunkedIndexFile(File chunkedIndexFile, int maxIdMail) {
        if (maxIdMail == NOT_A_NEW_CHUNK) {
            // This is a chunk coming from the merge of two chunks we already have.
            new File(folderNameIndex).mkdirs();
            // Get the two consecutive chunks with minimum file size.
            int indexMin = getIndexFirstOfTwoChunks();
            if (indexMin == -1) {
                return false;
            }
            Path source;
            // Put those two chunks at the end of the list.
            try {
                // Renaming all the chunks with greater id 2 ids before.
                for (int i = indexMin + 2; i < nbIndexChunks; i++) {
                    String filenameChunk = getChunkedIndexFilePathName(i);
                    source = Paths.get(filenameChunk);
                    String filenameChunkBefore = getChunkedIndexFileName(i - 2);
                    Files.move(source, source.resolveSibling(filenameChunkBefore), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                System.out.println("IOException when updating the chunked index files.");
                e.printStackTrace();
                return false;
            }
            // Remove the last chunk and replace the before-last chunk with the one given.
            String filename = getChunkedIndexFilePathName(nbIndexChunks - 2);
            try {
                Files.copy(chunkedIndexFile.toPath(),
                    (new File(filename)).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("IOException in file '" + filename + "'.");
                e.printStackTrace();
                return false;
            }
            new File(getChunkedIndexFilePathName(nbIndexChunks - 1)).delete();
            nbIndexChunks--;
            return true;
        } else {
            // This is a new chunk.
            if (maxIdMail <= maxIdIndexedMail) {
                // The user has started re-indexing everything from scratch.
                for (int i = 0; i < nbIndexChunks; i++) {
                    new File(getChunkedIndexFilePathName(i)).delete();
                }
                nbIndexChunks = 0;
            }
            maxIdIndexedMail = maxIdMail;
            new File(folderNameIndex).mkdirs();
            String filename = folderNameIndex + File.separatorChar +
                    getChunkedIndexFileName(nbIndexChunks);
            try {
                Files.copy(chunkedIndexFile.toPath(),
                    (new File(filename)).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("IOException in file '" + filename + "'.");
                e.printStackTrace();
                return false;
            }
            nbIndexChunks++;
            return true;
        }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Local computation by the Server (Bijection idMsg / mails)
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * The chunks have all been merged to the last chunk on the server.
     * Set the index to this last chunk.
     *
     * @return true iff the index has successfully be replaced.
     */
    @Override
    public boolean replaceIndexWithLastChunk() {
        new File(folderNameIndex).mkdirs();
        String filename = getIndexFileName();
        String filepathnameChunk = getChunkedIndexFilePathName(0);
        if (nbIndexChunks > 1) {
            return false;
        } else try {
            Path source = Paths.get(filepathnameChunk);
            Files.move(source, source.resolveSibling(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("IOException in file '" + filename + "'.");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public final static String separatorBijection = " --- ";
    
    /*
     * Export the bijection idMsg / mailName.
     */
    private void exportBijectionIdentifierMailFile() {
        final File folderMails = new File(folderNameMails);
        final String folderMailsAbsolutePath = folderMails.getAbsolutePath();
        try (FileWriter out = new FileWriter(fileNameBijection)) {
            for (Integer i: mapBijection.keySet()) {
                // We take the absolute path
                final String mailAbsolutePath = mapBijection.get(i).getAbsolutePath();
                final String mailRelativePath = mailAbsolutePath.substring(folderMailsAbsolutePath.length() + 1);
                out.write(i + separatorBijection + mailRelativePath + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * Import the bijection idMsg / mailName.
     */
    private void importBijectionIdentifierMailFile() {
        mapBijection = new HashMap<>();
        final File folderMails = new File(folderNameMails);
        final String folderMailsAbsolutePath = folderMails.getAbsolutePath();
        try (FileReader fileReader = new FileReader(fileNameBijection);
                BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            
            while ((line = bufferedReader.readLine()) != null) {
                int position = line.indexOf(separatorBijection);
                String stringIndex = line.substring(0, position);
                String stringFilename = line.substring(position + separatorBijection.length());
                mapBijection.put(Integer.parseInt(stringIndex),
                        new File(folderMailsAbsolutePath + File.separatorChar + stringFilename));
            }
        } catch (IOException e) {
            System.out.println("IOException in file '" + fileNameBijection + "'.");
            e.printStackTrace();
        }
    }
    
    private void computeBijectionIdentifierMailFile() {
        TreeSet<File> foldersTreated = new TreeSet<>();
        mapBijection = new HashMap<>();
        computeBijectionIdentifierMailFile(new File(folderNameMails), 1, foldersTreated);
    }
    
    private int computeBijectionIdentifierMailFile(File folder, int i, TreeSet<File> foldersTreated) {
        foldersTreated.add(folder);
        File[] files = folder.listFiles();
        Arrays.sort(files);
        for (final File file : files) {
            if (!file.isDirectory()) {
                mapBijection.put(i, file);
                i++;
                
            } else if (!foldersTreated.contains(file)) {
                i = computeBijectionIdentifierMailFile(file, i, foldersTreated);
            }
        }
        return i;
    }
    
    /*
     * Whenever a new mail comes or an old mail is deleted, updates the map
     * between the e-mails and the identifiers (as managed in the inverted
     * lists).
     */
    @Override
    public void updateMap() {
        // WARNING: ServerLocal is a *static* Server that does not handle
        // dynamic removal of e-mails.
        // This function thus just handles the base case where we compute the
        // map from scratch.
        File tempFile = new File(fileNameBijection);
        if (tempFile.exists()) {
            System.out.println(fileNameBijection + " already exists, I will use it.");
            importBijectionIdentifierMailFile();
        } else {
            computeBijectionIdentifierMailFile();
            exportBijectionIdentifierMailFile();
        }
    }
    
    /*
     * Whenever a lot a e-mails have been deleted (i.e., their identifier now
     * corresponds to no e-mail on the server), it can be a good idea to
     * compress the map between the e-mails and the identifiers.
     */
    @Override
    public void compressMap() {
        // WARNING: ServerLocal is a *static* Server that does not handle
        // dynamic removal of e-mails.
    }
}
