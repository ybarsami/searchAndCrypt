/**
 * ServerTest.java
 *
 * Created on 14 févr. 2019
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
public class ServerTest extends Server {
    
    public final static String datasetsFolderName = "datasets";
    
    public static enum MailSet {
        MAILS_ENRON1("allen-p"),
        MAILS_ENRON2("dasovich-j"),
        TEXTS_FABLES("french", "Fables", datasetsFolderName + File.separatorChar +
                "Fables"), // 240 fables
        TEXTS_HUGO("french", "Hugo", datasetsFolderName + File.separatorChar +
                "Hugo"); // 6 000 documents
        
        private final String language;
        private final String name;
        private final String folderName;
        
        // Constructor
        MailSet(String language, String name, String folderName) {
            this.language = language;
            this.name = name;
            this.folderName = folderName;
        }
        MailSet(String user) {
            this.language = "english";
            this.name = "Enron_" + user;
            this.folderName = datasetsFolderName + File.separatorChar +
                user;
        }
        
        public String getLanguage() { return language; }
        public String getName() { return name; }
        public String getFolderName() { return folderName; }
    }
    
    public static enum MailSort {
        FOLDER_NAMES, // Leave the sorting as it has been done by the folders created by the user.
        INTERLOCUTOR, // Sort by interlocutor, then by time.
        TIME;         // Sort by time.
    }
    private MailSort mailSort;                   // How e-mails are sorted.
    
    public MailSort getMailSort() {
        return mailSort;
    }
    
    private String folderNameMails;              // Name of the folder that stores the mails.
    private String folderNameIndex;              // Name of the folder that stores the index.
    private String fileNameIndex;                // Base name of the global index files, without extension.
    private String fileExtension;                // Extension of the files representing the indexes.
    private int nbIndexChunks;                   // Number of chunks of the full index currently stored.
    
    private String fileNameBijection;            // Name of the file that stores the bijection idMsg / mailName.
    private HashMap<Integer, File> mapBijection; // HashMap that stores the bijection idMsg / mailFile.
    
    private String language;                     // Main language in which the mails are written.
    
    private String getIndexFileName(String indexName) {
        return fileNameIndex + indexName + "." + fileExtension;
    }
    private String getChunkedIndexFileName(String indexName, int idChunk) {
        return fileNameIndex + indexName + "_chunk" + idChunk + "." + fileExtension;
    }
    private String getIndexFilePathName(String indexName) {
        return folderNameIndex + File.separatorChar + getIndexFileName(indexName);
    }
    private String getChunkedIndexFilePathName(String indexName, int idChunk) {
        return folderNameIndex + File.separatorChar + getChunkedIndexFileName(indexName, idChunk);
    }
    
    
    /**
     * Creates a new instance of ServerTest.
     */
    public ServerTest(MailSet mailsType) {
        fileNameIndex = "test_";
        fileExtension = "txt";
        nbIndexChunks = 0;
        maxIdIndexedMail = 0;
        
        switch (mailsType) {
            case TEXTS_FABLES:
            case TEXTS_HUGO:
                mailSort = MailSort.FOLDER_NAMES;
                break;
            default:
                mailSort = MailSort.INTERLOCUTOR;
                break;
        }
        
        loadMails(mailsType);
        language = mailsType.getLanguage();
    }
    public ServerTest(String mailsType, String language) {
        fileNameIndex = "test_";
        fileExtension = "txt";
        nbIndexChunks = 0;
        maxIdIndexedMail = 0;
        mailSort = MailSort.FOLDER_NAMES;
        folderNameIndex = mailsType + "_test";
        this.language = language;
    }
    
    public static final List<Server> getTestServers() {
        MailSet[] mailsTypes = { MailSet.MAILS_ENRON1, MailSet.MAILS_ENRON2 };
//        MailSet[] mailsTypes = { MailSet.TEXTS_FABLES };
        List<Server> serverList = new ArrayList<>();
        for (MailSet mailsType : mailsTypes) {
            serverList.add(new ServerTest(mailsType));
        }
        return serverList;
    }
    
    private final void loadMails(MailSet mailsType) {
        folderNameMails = mailsType.getFolderName();
        switch (mailSort) {
            case FOLDER_NAMES:
                folderNameIndex   = mailsType.getName() + "_test";
                fileNameBijection = mailsType.getName() + "_bijection.txt";
                break;
            case INTERLOCUTOR:
                folderNameIndex   = mailsType.getName() + "_test_sort";
                fileNameBijection = mailsType.getName() + "_bijection_sort.txt";
                break;
        }
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
     * @param indexName, the String representing the compression scheme for the
     * index.
     * @return the index compressed with this scheme.
     */
    @Override
    public File getIndexFile(String indexName) {
        String filename = getIndexFilePathName(indexName);
        return new File(filename);
    }
    
    private int getIndexFirstOfTwoChunks(String indexName) {
        int indexMin = -1;
        long minSize = Integer.MAX_VALUE;
        for (int i = 0; i < nbIndexChunks / 2; i++) {
            String filename1 = getChunkedIndexFilePathName(indexName, i);
            String filename2 = getChunkedIndexFilePathName(indexName, i + 1);
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
     * @param indexName, the String representing the compression scheme for the
     * index.
     * @return the two consecutive chunks with minimum file size.
     */
    @Override
    public List<File> getTwoChunksIndex(String indexName) {
        if (nbIndexChunks <= 1) {
            return null;
        }
        int indexMin = getIndexFirstOfTwoChunks(indexName);
        ArrayList<File> twoChunks = new ArrayList<>();
        String filename1 = getChunkedIndexFilePathName(indexName, indexMin);
        String filename2 = getChunkedIndexFilePathName(indexName, indexMin + 1);
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
        // TODO
        return new ArrayList<>();
    }
    
    /*
     * Get the language used.
     */
    @Override
    public String getLanguage() {
        return language;
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
    public boolean updateIndexFile(File newIndexFile, String indexName) {
        new File(folderNameIndex).mkdirs();
        String filename = getIndexFilePathName(indexName);
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
     * @param indexName, the String representing the compression scheme for the
     *        index.
     * @param maxIdMail, the maximum index of the mail contained in the chunk.
     *        If set to -1, this is the result of two chunks currently on
     *        the server (in this case, replace them with the chunk), otherwise
     *        this is a new chunk (in this case, add the chunk to the list).
     * @return true iff the chunked index has been successfully added.
     */
    @Override
    public boolean addChunkedIndexFile(File chunkedIndexFile, String indexName, int maxIdMail) {
        if (maxIdMail == NOT_A_NEW_CHUNK) {
            // This is a chunk coming from the merge of two chunks we already have.
            new File(folderNameIndex).mkdirs();
            // Get the two consecutive chunks with minimum file size.
            int indexMin = getIndexFirstOfTwoChunks(indexName);
            if (indexMin == -1) {
                return false;
            }
            Path source;
            String filename1 = getChunkedIndexFilePathName(indexName, indexMin);
            String filename2 = getChunkedIndexFilePathName(indexName, indexMin + 1);
            // Put those two chunks at the end of the list.
            try {
                // Renaming the two chunks.
                source = Paths.get(filename1);
                Path tmpPath1 = Files.move(source, source.resolveSibling("tmp_chunk1." + fileExtension), StandardCopyOption.REPLACE_EXISTING);
                source = Paths.get(filename2);
                Path tmpPath2 = Files.move(source, source.resolveSibling("tmp_chunk2." + fileExtension), StandardCopyOption.REPLACE_EXISTING);
                // Renaming all the chunks with greater id 2 ids before.
                for (int i = indexMin + 2; i < nbIndexChunks; i++) {
                    String filenameChunk = getChunkedIndexFilePathName(indexName, i);
                    source = Paths.get(filenameChunk);
                    String filenameChunkBefore = getChunkedIndexFileName(indexName, i - 2);
                    Files.move(source, source.resolveSibling(filenameChunkBefore), StandardCopyOption.REPLACE_EXISTING);
                }
                // Renaming the two tmp files to the last chunk files.
                source = tmpPath1;
                filename1 = getChunkedIndexFileName(indexName, nbIndexChunks - 2);
                Files.move(source, source.resolveSibling(filename1), StandardCopyOption.REPLACE_EXISTING);
                source = tmpPath2;
                filename2 = getChunkedIndexFileName(indexName, nbIndexChunks - 1);
                Files.move(source, source.resolveSibling(filename2), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("IOException when updating the chunked index files.");
                e.printStackTrace();
                return false;
            }
            // Remove the last chunk and replace the before-last chunk with the one given.
            String filename = getChunkedIndexFilePathName(indexName, nbIndexChunks - 2);
            try {
                Files.copy(chunkedIndexFile.toPath(),
                    (new File(filename)).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("IOException in file '" + filename + "'.");
                e.printStackTrace();
                return false;
            }
            new File(getChunkedIndexFilePathName(indexName, nbIndexChunks - 1)).delete();
            nbIndexChunks--;
            return true;
        } else {
            // This is a new chunk.
            new File(folderNameIndex).mkdirs();
            String filename = folderNameIndex + File.separatorChar +
                    fileNameIndex + indexName + "_chunk" + nbIndexChunks + "." + fileExtension;
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
            maxIdIndexedMail = maxIdMail > maxIdIndexedMail ? maxIdMail : maxIdIndexedMail;
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
     * @param indexName, the String representing the compression scheme for the
     * index.
     * @return true iff the index has successfully be replaced.
     */
    @Override
    public boolean replaceIndexWithLastChunk(String indexName) {
        new File(folderNameIndex).mkdirs();
        String filename = getIndexFileName(indexName);
        String filepathnameChunk = getChunkedIndexFilePathName(indexName, 0);
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
    public void exportBijectionIdentifierMailFile() {
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
    public void importBijectionIdentifierMailFile() {
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
    
    private void computeBijectionIdentifierMailFile(MailSort mailSort) {
        TreeSet<File> foldersTreated = new TreeSet<>();
        mapBijection = new HashMap<>();
        computeBijectionIdentifierMailFile(new File(folderNameMails), 1, foldersTreated);
        switch (mailSort) {
            case INTERLOCUTOR:
                File[] files = MimeParse.sortInterlocutor(mapBijection);
                mapBijection = new HashMap<>();
                for (int i = 0; i < files.length; i++) {
                    mapBijection.put(i + 1, files[i]);
                }
                break;
        }
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
        // TODO : for now, this just handles the base case where we compute the map from scratch.
        File tempFile = new File(fileNameBijection);
        if (tempFile.exists()) {
            System.out.println(fileNameBijection + " already exists, I will use it.");
            importBijectionIdentifierMailFile();
        } else {
            computeBijectionIdentifierMailFile(mailSort);
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
        // TODO
    }
}
