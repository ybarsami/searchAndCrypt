/**
 * Parsing a MIME objet.
 */

package searchAndCrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

import java.util.Date; // For the date header.
import java.util.List;

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

/**
 *
 * @author yann
 */
public class MimeParser {
    
    /*
     * Build a Message from textual MIME representation.
     * PERMISSIVE has the following impact:
     *   Remove default maximum header length of 10k
     *   Remove default maximum line length of 1000 characters.
     *   Disable the check for header count.
    */
    private static final Message.Builder builder = Message.Builder.of().use(MimeConfig.PERMISSIVE);
    
    
    /*
     * We distinguish 3 different contents inside the body of a MIME message:
     * > text/plain (we extract the whole text)
     * > text/html (we parse it with jsoup and extract the result)
     * > attachments (we do not extract if)
     * Other kinds of content are treated as text/plain.
     */
    private static enum ContentSet {
        CONTENT_TYPE_TEXT_PLAIN,
        CONTENT_TYPE_TEXT_HTML,
        CONTENT_DISPOSITION_ATTACHMENT,
        CONTENT_OTHER,
    };
    
    /*
     * We want to know which kind of content is in the given entity.
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
     * We parse a field to extract relevant parts from it.
     *
     * @param field, the field to parse.
     * @return the String that contains the relevant parts of the field.
     */
    private static String parseField(Field field) {
        StringBuilder stringBuilder = new StringBuilder();
        if (field instanceof AddressListField) {
            // An address field (From, To, Cc, etc.)
            MailboxList list = ((AddressListField) field).getAddressList().flatten();
            StringBuilder sb = new StringBuilder();
            for (Mailbox mailbox : list) {
                sb.append(AddressFormatter.DEFAULT.format(mailbox, false)).append("\n");
            }
            stringBuilder.append(sb.toString());

        } else if (field instanceof DateTimeField) {
            // A date and time field.
            Date date = ((DateTimeField) field).getDate();
            if (date != null) {
                stringBuilder.append(date.toString()).append("\n");
            }

        } else if (field instanceof MailboxField) {
            // An address field (From, To, Cc, etc.)
            Mailbox mailbox = ((MailboxField) field).getMailbox();
            stringBuilder.append(AddressFormatter.DEFAULT.format(mailbox, false));

        } else if (field instanceof MailboxListField) {
            // An address field (From, To, Cc, etc.)
            MailboxList list = ((MailboxListField) field).getMailboxList();
            StringBuilder sb = new StringBuilder();
            for (Mailbox mailbox : list) {
                sb.append(AddressFormatter.DEFAULT.format(mailbox, false)).append("\n");
            }
            stringBuilder.append(sb.toString());

        } else if (field instanceof UnstructuredField) {
            // An unstructured field.
            // Get the subject field.
            if (field.getName() != null &&
                    field.getName().equalsIgnoreCase("Subject")) {
                stringBuilder.append(((UnstructuredField) field).getValue()).append("\n");
            }

        } else {
            // It can be either ContentDescriptionField, ContentDispositionField,
            // ContentIdField, ContentLanguageField, ContentLengthField,
            // ContentLocationField, ContentMD5Field, ContentTransferEncodingField,
            // ContentTypeField or MimeVersionField.
            // In those cases, ignore it.
        }
        return stringBuilder.toString();
    }
    
    /*
     * We parse a header to extract relevant parts from it.
     *
     * @param header, the header to parse.
     * @return the String that contains the relevant parts of the header.
     */
    private static String parseHeader(Header header) {
        if (header == null) {
            return "";
        }
        
        StringBuilder stringBuilder = new StringBuilder();
        // This is how you get the fields.
        for (Field field : header.getFields()) {
            stringBuilder.append(parseField(field));
        }
        return stringBuilder.toString();
    }
    
    /*
     * We read a text body and convert it to a string.
     *
     * @param textBody, the text body to read.
     * @return the String that contains the text of this body.
     */
    private static String stringFromTextBody(TextBody textBody) throws IOException {
        StringBuilder sb = new StringBuilder();
        Reader r = textBody.getReader();
        int c;
        while ((c = r.read()) != -1) {
            sb.append((char) c);
        }
        return sb.toString();
    }
    
    /*
     * We parse the entity to extract relevant parts from it.
     *
     * @param entity, the entity to index.
     * @return the String that contains the relevant parts of the entity.
     */
    private static String parseEntity(Entity entity) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        // Parse the header.
        stringBuilder.append(parseHeader(entity.getHeader()));
        
        // Parse the body.
        Body body = entity.getBody();
        if (body instanceof Multipart) {
            // The body of the entity is a Multipart.
            // Ignore the preamble and the epilogue, only parse the body parts.
            Multipart multipart = (Multipart) body;
            List<Entity> bodyParts = multipart.getBodyParts();
            for (Entity part : bodyParts) {
                stringBuilder.append(parseEntity(part));
            }
            
        } else if (body instanceof MessageImpl) {
            // The body of the entity is another Message.
            stringBuilder.append(parseEntity((MessageImpl) body));
            
        } else if (body instanceof TextBody) {
            // A text body.
            TextBody textBody = (TextBody) body;
            final String stringBody = stringFromTextBody(textBody);
            
            // Checks if this is text/html or text/plain or if it is an attached file.
            ContentSet content = MimeParser.getContent(entity);
            switch (content) {
                case CONTENT_TYPE_TEXT_HTML:
                    // For text/html, extract the text from html with Jsoup.
                    String htmlParsed = Jsoup.parse(stringBody).text();
                    stringBuilder.append(htmlParsed).append("\n");
                    break;
                    
                case CONTENT_DISPOSITION_ATTACHMENT:
                    // Ignore attached files.
                    break;
                    
                case CONTENT_OTHER:
                    // It might happen that a message do not have a Content-Type
                    // header. In that case, treat it as text/plain.
                case CONTENT_TYPE_TEXT_PLAIN:
                    // For text/plain, just return the text.
                    stringBuilder.append(stringBody).append("\n");
                    break;
            }
            
        } else if (body instanceof BinaryBody) {
            // A binary body.
            // Ignore it.
        }
        return stringBuilder.toString();
    }
    
    /*
     * Reads an e-mail in the MIME format, and ouput the String that will be
     * taken into account for the indexation.
     */
    public static String parseMimeMessage(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file)) {
            // Parse the MIME message from textual representation to mime4j.dom.Message representation.
            final Message message = builder.parse(fis).build();
            
            // Update the string builder with the content of the message.
            stringBuilder.append(parseEntity(message));

        } catch(FileNotFoundException e) {
            System.out.println("Unable to open file '" + file.getName() + "'.");

        } catch (IOException e) {
            System.out.println("IOException in file '" + file.getName() + "'.");
        }
        return stringBuilder.toString(); 
    }
}
