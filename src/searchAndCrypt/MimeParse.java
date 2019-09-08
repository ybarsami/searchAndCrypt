/**
 * MimeParse.java
 *
 * Created on 9 févr. 2019
 */

package searchAndCrypt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date; // For the date header.
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.address.MailboxList;
import org.apache.james.mime4j.dom.field.AddressListField;
import org.apache.james.mime4j.dom.field.ContentDispositionField;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.dom.field.DateTimeField;
import org.apache.james.mime4j.dom.field.MailboxField;
import org.apache.james.mime4j.dom.field.MailboxListField;
import org.apache.james.mime4j.dom.field.UnstructuredField;
import org.apache.james.mime4j.field.address.AddressFormatter;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeConfig;

import org.jsoup.Jsoup; // For text/html body.

import org.tartarus.snowball.SnowballStemmer;

/**
 *
 * @author yann
 */
public class MimeParse {
    
    private GlobalIndex globalIndex;
    private Server server;
    
    // Stem words. Default is no stemmer.
    private SnowballStemmer stemmer = null;
    
    /*
     * Build a Message from textual MIME representation.
     * PERMISSIVE has the following impact:
     *   Remove default maximum header length of 10k
     *   Remove default maximum line length of 1000 characters.
     *   Disable the check for header count.
    */
    private Message.Builder builder = Message.Builder.of().use(MimeConfig.PERMISSIVE);
    
    
    /**
     * Creates a new instance of MimeParse.
     */
    public MimeParse(Server server) {
        globalIndex = new GlobalIndex();
        this.server = server;
    }
    
    public void setStemmer(SnowballStemmer stemmer) {
        this.stemmer = stemmer;
    }
    
    public void setBuilder(Message.Builder builder) {
        this.builder = builder;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Index a meaningful part of a message.
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * We have a new string to add to the index.
     *
     * @param toBeIndexed, the string to be indexed.
     * @param idMessage, the identifier of the message this string belongs to.
     */
    private void updateWithNewString(String toBeIndexed, int idMessage) {
        // Normalize the String.
        String normalizedString = Tools.normalize(toBeIndexed);
        
        // Split the String into words.
        // Do not use "String[] splitLine = line.split("\\s+");" which keeps all
        // the toBeIndexed strings in memory.
        final int length = normalizedString.length();
        int begin;
        int end = -1; // Starts at -1 because we start the loop by "begin = end + 1;"
        int index;
        String word;
        do {
            begin = end + 1;
            for (index = begin; index < length; index++) {
                if (normalizedString.charAt(index) == ' ') {
                    break;
                }
            }
            end = index;
            
            // Remove empty words. It happens when we have multiple ' ' in the
            // string --- or when the string starts with ' '.
            if (begin == end) {
                continue;
            }
            
            // Create a brand new string.
            word = normalizedString.substring(begin, end);
            
            // Give the word to the stemmer, and index it if needed.
            if (stemmer == null) {
                // No stemmer.
                globalIndex.updateWithNewWord(word, idMessage);
                
            } else {
                // SnowBall stemmer.
                stemmer.setCurrent(word);
                if (stemmer.stem()) {
                    word = stemmer.getCurrent();
                    globalIndex.updateWithNewWord(word, idMessage);
                }
            }
        } while (end < length);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // MIME parsing of a message.
    ////////////////////////////////////////////////////////////////////////////
    
    private static enum ContentSet {
        CONTENT_TYPE_TEXT_PLAIN,
        CONTENT_TYPE_TEXT_HTML,
        CONTENT_DISPOSITION_ATTACHMENT,
        CONTENT_OTHER,
    };
    
    /*
     * We want to know which kind of content is in the given entity. We do not
     * explore every kind of content, we just check for text/plain, text/html,
     * or attachment.
     *
     * @param entity, the entity to check.
     * @return the content type of this entity.
     */
    private static ContentSet getContent(Entity entity) {
        for (Field field : entity.getHeader().getFields()) {
            if (field instanceof ContentDispositionField &&
                    ((ContentDispositionField) field).isAttachment()) {
                return ContentSet.CONTENT_DISPOSITION_ATTACHMENT;
            }
        }
        for (Field field : entity.getHeader().getFields()) {
            if (field instanceof ContentTypeField) {
                String mimeType = ((ContentTypeField) field).getMimeType();
                if (mimeType != null) {
                    if (mimeType.equalsIgnoreCase("text/html")) {
                        return ContentSet.CONTENT_TYPE_TEXT_HTML;
                    } else if (mimeType.equalsIgnoreCase("text/plain")) {
                        return ContentSet.CONTENT_TYPE_TEXT_PLAIN;
                    }
                }
            }
        }
        return ContentSet.CONTENT_OTHER;
    }
    
    /*
     * We parse the header to index relevant parts from it.
     *
     * @param header, the header to index.
     * @param idMessage, the id of the message which has this header.
     */
    private void takeInformation(Header header, int idMessage) {
        if (header == null) {
            return;
        }
        // TODO: check that all fields that need to be indexed are indexed.
        
        // This is how you get the fields.
        for (Field field : header.getFields()) {
            if (field instanceof AddressListField) {
                // An address field (From, To, Cc, etc.)
                MailboxList list = ((AddressListField) field).getAddressList().flatten();
                StringBuilder sb = new StringBuilder();
                for (Mailbox mailbox : list) {
                    sb.append(AddressFormatter.DEFAULT.format(mailbox, false)).append("\n");
                }
                updateWithNewString(sb.toString(), idMessage);
                
            } else if (field instanceof DateTimeField) {
                // A date and time field.
                Date date = ((DateTimeField) field).getDate();
                if (date != null) {
                    updateWithNewString(date.toString(), idMessage);
                }
                
            } else if (field instanceof MailboxField) {
                // An address field (From, To, Cc, etc.)
                Mailbox mailbox = ((MailboxField) field).getMailbox();
                updateWithNewString(AddressFormatter.DEFAULT.format(mailbox, false), idMessage);
                
            } else if (field instanceof MailboxListField) {
                // An address field (From, To, Cc, etc.)
                MailboxList list = ((MailboxListField) field).getMailboxList();
                StringBuilder sb = new StringBuilder();
                for (Mailbox mailbox : list) {
                    sb.append(AddressFormatter.DEFAULT.format(mailbox, false)).append("\n");
                }
                updateWithNewString(sb.toString(), idMessage);
                
            } else if (field instanceof UnstructuredField) {
                // An unstructured field.
                // Index the subject field.
                if (field.getName() != null &&
                        field.getName().equalsIgnoreCase("Subject")) {
                    updateWithNewString(((UnstructuredField) field).getValue(), idMessage);
                }
                
            } else {
                // It can be either ContentDescriptionField, ContentDispositionField,
                // ContentIdField, ContentLanguageField, ContentLengthField,
                // ContentLocationField, ContentMD5Field, ContentTransferEncodingField,
                // ContentTypeField or MimeVersionField.
                // In those cases, do not index.
            }
        }
    }
    
    /*
     * We parse the entity to index relevant parts from it.
     * Here, we know that the entity is either a Message or a Body part, because
     * it is not a Header (it would call the previous takeInformation function).
     *
     * @param entity, the entity to index.
     * @param idMessage, the id of the message which is this entity.
     */
    private void takeInformation(Entity entity, int idMessage) {
        // This is how you get the header.
        takeInformation(entity.getHeader(), idMessage);
        
        // This is how you get the body.
        Body body = entity.getBody();
        
        if (body instanceof Multipart) {
            // The body of the entity is a Multipart.
            Multipart multipart = (Multipart) body;
            
            // The preamble can be ignored.
            
            List<Entity> bodyParts = multipart.getBodyParts();
            
            if (multipart.getSubType().equals("alternative")) {
                // multipart/alternative message. If this is a text/plain + text/html,
                // only index the html version, so we remove the text/plain version.
                if (bodyParts.size() == 2) {
                    ContentSet content0 = MimeParse.getContent(bodyParts.get(0));
                    ContentSet content1 = MimeParse.getContent(bodyParts.get(1));
                    if (content0 == ContentSet.CONTENT_TYPE_TEXT_PLAIN &&
                            content1 == ContentSet.CONTENT_TYPE_TEXT_HTML) {
                        // Most probable case, plain should be before html.
                        // See https://tools.ietf.org/html/rfc2046#section-5.1.4
                        bodyParts = Collections.singletonList(bodyParts.get(1));
                        
                    } else if (content1 == ContentSet.CONTENT_TYPE_TEXT_PLAIN &&
                            content0 == ContentSet.CONTENT_TYPE_TEXT_HTML) {
                        // In case the html version was put before the plain text.
                        bodyParts = Collections.singletonList(bodyParts.get(0));
                    }
                }
            }
            
            // Index the needed body parts.
            for (Entity part : bodyParts) {
                takeInformation(part, idMessage);
            }
            
            // The epilogue can be ignored.
            
        } else if (body instanceof MessageImpl) {
            // The body of the entity is another Message.
            takeInformation((MessageImpl) body, idMessage);
            
        } else if (body instanceof TextBody) {
            // A text body. Index its contents.
            TextBody textBody = (TextBody) body;
            StringBuilder sb = new StringBuilder();
            try {
                Reader r = textBody.getReader();
                int c;
                while ((c = r.read()) != -1) {
                    sb.append((char) c);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // Checks if this is text/html or text/plain or if it is an attached file.
            ContentSet content = MimeParse.getContent(entity);
            String extractedText = "";
            switch (content) {
                case CONTENT_TYPE_TEXT_HTML:
                    // For text/html, extract the text from html with Jsoup.
                    extractedText = Jsoup.parse(sb.toString()).text();
                    break;
                    
                case CONTENT_DISPOSITION_ATTACHMENT:
                    // For attached files, do not index.
                    break;
                    
                case CONTENT_OTHER:
                    // It might happen that a message do not have a Content-Type header.
                    // (20 of my personal e-mails fall in this category and still have to be indexed)
                    // In that case, treat it as text/plain.
                case CONTENT_TYPE_TEXT_PLAIN:
                    // For text/plain, just index the string.
                    extractedText = sb.toString();
                    break;
            }
            
            // Split by multiple spaces.
            String[] strings = extractedText.split("\\s+");
            
            // Strip long http(s):// sub-strings (usually weird stuff inside).
            for (String str: strings) {
                updateWithNewString(Tools.removeLongURL(str), idMessage);
            }
            
        } else if (body instanceof BinaryBody) {
            // A binary body.
            // Do not index.
        }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Interactions with local data.
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * Export the index to the server, using the compression scheme specified
     * in indexType.
     *
     * @param indexType, the String representing the compression scheme for the
     * index.
     * For valid indexType Strings, see GlobalIndex.exportToFile.
     */
    public void exportToFile(String indexType, boolean inASCII) {
        String tmpIndexFilename = "tmpIndex.txt";
        File tmpIndexFile;
        switch(indexType) {
            case "lexicon":
                tmpIndexFile = globalIndex.exportToFileLexiconOnly(tmpIndexFilename, inASCII);
                break;
            default:
                tmpIndexFile = globalIndex.exportToFile(tmpIndexFilename, indexType, inASCII);
                break;
        }
        server.updateIndexFile(tmpIndexFile, inASCII ? "ASCII" : indexType);
        tmpIndexFile.delete();
    }
    public void exportToFile(String indexType) {
        exportToFile(indexType, false);
    }
    
    /*
     * Export the index to the server, using the compression scheme specified
     * in indexType.
     *
     * @param indexType, the String representing the compression scheme for the
     * index.
     * For valid indexType Strings, see GlobalIndex.exportToFile.
     */
    public void exportToChunk(String indexType, boolean inASCII) {
        String tmpIndexFilename = "tmpIndex.txt";
        File tmpIndexFile;
        switch(indexType) {
            case "lexicon":
                tmpIndexFile = globalIndex.exportToFileLexiconOnly(tmpIndexFilename, inASCII);
                break;
            default:
                tmpIndexFile = globalIndex.exportToFile(tmpIndexFilename, indexType, inASCII);
                break;
        }
        server.addChunkedIndexFile(tmpIndexFile, indexType + (inASCII ? "_ASCII" : ""), globalIndex.nbMails);
        tmpIndexFile.delete();
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Interactions with a Server.
    ////////////////////////////////////////////////////////////////////////////
    
    /*
     * Download an e-mail from the server that is not yet indexed, and
     * add it in the index.
     *
     * @param idMessage, the ID of the e-mail.
     */
    public void updateWithNewMimeMessage(File file, int idMessage) {
        if (builder == null) {
            // If there is no builder, then we just read the full message.
            try (FileReader fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line;
                
                while ((line = bufferedReader.readLine()) != null) {
                    updateWithNewString(line, idMessage);
                }
            } catch (IOException e) {
                System.out.println("IOException in file '" + file.getName() + "'.");
                e.printStackTrace();
            }
            
        } else {
            // There is a message builder.
            try (FileInputStream fis = new FileInputStream(file)) {
                // Parse the MIME message from textual representation to mime4j.dom.Message representation.
                final Message message = builder.parse(fis).build();
                
                // Update the index.
                takeInformation(message, idMessage);
                
            } catch(FileNotFoundException e) {
                System.out.println("Unable to open file '" + file.getName() + "'.");
                
            } catch (IOException e) {
                System.out.println("IOException in file '" + file.getName() + "'.");
                e.printStackTrace();
            }
        }
    }
    
    public static int nbMailsBeforePrint = 1024;
    public static int nbMailsBeforeSave = 512; //1000000;
    
    /*
     * Download the e-mails from the server that are not yet indexed, and
     * add them in the index.
     */
    public void indexEverything(Server server, String indexType, boolean inASCII) {
        // Clear the index.
        globalIndex = new GlobalIndex();
        int nbIndexedMessages = 0;
        
        // TIMING
        long time1 = System.currentTimeMillis();
        long time2;
        
        // Get the maximum identifier of an e-mail indexed on the server.
        int maxIdIndexedMail = server.getMaxIdIndexedMail();
        // Get all new mails from the server and index them.
        final TreeSet<Integer> allMessageIdentifiers = server.getAllMessageIdentifiers(maxIdIndexedMail);
        for (int i : allMessageIdentifiers) {
            updateWithNewMimeMessage(server.getMessage(i), i);
            // Testing...
            if (SearchAndCrypt.isDebugMode) {
                if (i % nbMailsBeforePrint == 0) {
                    System.out.printf("Handling mail number %d.\n", i);
                    time2 = System.currentTimeMillis();
                    System.out.printf("Time needed to handle the last %d mails : %dms.\n", nbMailsBeforePrint, time2 - time1);
                    time1 = time2;
                }
            }
            nbIndexedMessages++;
            // Export a chunk of the index.
            if (nbIndexedMessages == nbMailsBeforeSave) {
                exportToChunk(indexType, inASCII);
                globalIndex = new GlobalIndex();
                nbIndexedMessages = 0;
            }
        }
        // Export a chunk of the index.
        if (nbIndexedMessages > 0) {
            exportToChunk(indexType, inASCII);
        }
        
        // Merge the chunks of the index.
        while (server.getNbIndexChunks() > 1) {
            List<File> chunks12 = server.getTwoChunksIndex(indexType);
            File merged = globalIndex.importAndMerge(chunks12.get(0), chunks12.get(1), indexType, inASCII);
            server.addChunkedIndexFile(merged, inASCII ? "ASCII" : indexType, Server.NOT_A_NEW_CHUNK);
        }
        
        // Store the full index on the server.
        server.replaceIndexWithLastChunk(inASCII ? "ASCII" : indexType);
    }
    public void indexEverything(Server server, String indexType) {
        indexEverything(server, indexType, false);
    }
    
    /*
     * Import the index from the server, using the compression scheme specified
     * in indexType.
     *
     * @param indexType, the String representing the compression scheme for the
     * index.
     * For valid indexType Strings, see GlobalIndex.importFromFile.
     */
    public void loadIndex(String indexType) {
        File indexFile = server.getIndexFile(indexType);
        globalIndex.importFromFile(indexFile, indexType);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Sorting a list of messages according to interlocutor then date.
    ////////////////////////////////////////////////////////////////////////////
    
    private static class MessageEntry implements Comparable<MessageEntry> {
    
        public String interlocutor; // The interlocutor of the message.
        public Date date;           // The date of the message.
        public File file;           // The file containing the message.
        
        
        /**
         * Creates a new instance of MessageEntry.
         */
        public MessageEntry(String interlocutor, Date date, File file) {
            this.interlocutor = interlocutor;
            this.date         = date;
            this.file         = file;
        }
        
        @Override
        public int compareTo(MessageEntry e) {
            final int comparison = interlocutor.compareTo(e.interlocutor);
            if (comparison == 0) {
                if (date == null) {
                    return -1;
                } else if (e.date == null) {
                    return 1;
                } else {
                    return date.compareTo(e.date);
                }
            } else {
                return comparison;
            }
        }
    }
    
    private static class UserEntry implements Comparable<UserEntry> {
    
        public String userAddress; // The user address.
        public int nbMails;        // The number of times this user exchanges e-mails in the current set.
        
        
        /**
         * Creates a new instance of UserEntry.
         */
        public UserEntry(String userAddress, int nbMails) {
            this.userAddress = userAddress;
            this.nbMails     = nbMails;
        }
        public UserEntry(String userAddress) {
            this(userAddress, 1);
        }
        
        @Override
        public int compareTo(UserEntry e) {
            return userAddress.compareTo(e.userAddress);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Standard data structure to find and add a new Entry in log(N) time.
    // Keeps the data structure sorted at all times.
    ////////////////////////////////////////////////////////////////////////////
    
    private static class UserIndex {
        
        public TreeSet<UserEntry> set; // A set of entries.
        
        
        /**
         * Creates a new instance of UserIndex.
         */
        public UserIndex(TreeSet<UserEntry> set) {
            this.set = set;
        }
        public UserIndex() {
            this(new TreeSet<>());
        }
        
        /*
         * Searches this.set for a UserEntry with the same name as entry.userAddress.
         *
         * @param  word the name to be searched for.
         * @return the UserEntry with the searched name, if it is contained in
         *         this.set; null if it is not.
         */
        public UserEntry find(UserEntry entry) {
            final UserEntry floor = set.floor(entry);
            if (floor != null && floor.userAddress.equals(entry.userAddress)) {
                return floor;
            } else {
                return null;
            }
        }
        
        public void add(UserEntry newEntry) {
            this.set.add(newEntry);
        }
        
        /*
         * We have a new userAddress to add to the index.
         *
         * @param userAddress, the user address to be added.
         */
        public void updateWithNewAddress(String userAddress) {
            // Check if the userAddress is already in the index.
            UserEntry newUser = new UserEntry(userAddress.toLowerCase());
            UserEntry oldEntry = find(newUser);
            
            if (oldEntry == null) {
                // If the userAddress is not in the index, we add it to the index.
                this.add(newUser);
                
            } else {
                // If it was already in the index, we update its associated entry.
                oldEntry.nbMails += 1;
            }
        }
        
        public UserEntry getMostUsedEntry() {
            UserEntry user = this.set.first();
            for (UserEntry userEntry : this.set) {
                if (userEntry.nbMails > user.nbMails) {
                    user = userEntry;
                }
            }
            return user;
        }
    }
    
    public static File[] sortInterlocutor(HashMap<Integer, File> mapBijection) {
        final File[] files = new File[mapBijection.size()];
        final MessageEntry[] messages = new MessageEntry[mapBijection.size()];
        Message.Builder localBuilder = Message.Builder.of().use(MimeConfig.PERMISSIVE);
        // Automatically detect the user e-mail.
        UserIndex userAddresses = new UserIndex();
        for (File file : mapBijection.values()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                final Message message = localBuilder.parse(fis).build();
                if (message.getSender() != null) {
                    userAddresses.updateWithNewAddress(message.getSender().getAddress());
                }
                for (int i = 0; i < message.getFrom().size(); i++) {
                    userAddresses.updateWithNewAddress(message.getFrom().get(i).getAddress());
                }
                if (message.getTo() != null) {
                    for (int i = 0; i < message.getTo().size(); i++) {
                        userAddresses.updateWithNewAddress(message.getTo().get(i).toString());
                    }
                }
                if (message.getCc() != null) {
                    for (int i = 0; i < message.getCc().size(); i++) {
                        userAddresses.updateWithNewAddress(message.getCc().get(i).toString());
                    }
                }
                if (message.getBcc() != null) {
                    for (int i = 0; i < message.getBcc().size(); i++) {
                        userAddresses.updateWithNewAddress(message.getBcc().get(i).toString());
                    }
                }
            } catch(FileNotFoundException e) {
                System.out.println("Unable to open file '" + file.getName() + "'.");
                
            } catch (IOException e) {
                System.out.println("IOException in file '" + file.getName() + "'.");
                e.printStackTrace();
            }
        }
        String userAddress = userAddresses.getMostUsedEntry().userAddress;
        System.out.print("I found the following user adress : " + userAddress + "\n");
        // Sort by interlocutor.
        int index = 0;
        for (File file : mapBijection.values()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                final Message message = localBuilder.parse(fis).build();
                String interlocutor = "";
                String sender = "";
                if (message.getSender() != null) {
                    sender = message.getSender().getAddress();
                } else {
                    for (int i = 0; i < message.getFrom().size(); i++) {
                        sender = message.getFrom().get(i).getAddress();
                        if (!sender.isEmpty()) {
                            break;
                        }
                    }
                }
                if (sender.equals(userAddress)) {
                    if (message.getTo() != null && !message.getTo().isEmpty()) {
                        interlocutor = message.getTo().get(0).toString();
                    } else if (message.getCc() != null && !message.getCc().isEmpty()) {
                        interlocutor = message.getCc().get(0).toString();
                    } else if (message.getBcc() != null && message.getBcc().isEmpty()) {
                        interlocutor = message.getBcc().get(0).toString();
                    }
                } else {
                    interlocutor = sender;
                }
                messages[index] = new MessageEntry(interlocutor.toLowerCase(), message.getDate(), file);
                index++;
            } catch(FileNotFoundException e) {
                System.out.println("Unable to open file '" + file.getName() + "'.");
                
            } catch (IOException e) {
                System.out.println("IOException in file '" + file.getName() + "'.");
                e.printStackTrace();
            }
        }
        Arrays.sort(messages);
        for (int i = 0; i < mapBijection.size(); i++) {
            files[i] = messages[i].file;
        }
        return files;
    }
}
