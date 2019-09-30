/**
 * Parsing a MIME objet.
 */

package searchAndCrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

import java.util.Collections;
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
     * N.B.: If a MIME message is a multipart/alternative, we just extract the
     * text/html version.
     */
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
     * We parse the header to extract relevant parts from it.
     *
     * @param header, the header to index.
     * @param stringBuilder, the StringBuilder in which we store the relevant
     * parts of the message.
     */
    private static void takeInformation(Header header, StringBuilder stringBuilder) {
        if (header == null) {
            return;
        }
        
        // This is how you get the fields.
        for (Field field : header.getFields()) {
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
                // Index the subject field.
                if (field.getName() != null &&
                        field.getName().equalsIgnoreCase("Subject")) {
                    stringBuilder.append(((UnstructuredField) field).getValue()).append("\n");
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
     * We parse the header to extract relevant parts from it.
     * Here, we know that the entity is either a Message or a Body part, because
     * it is not a Header (it would call the previous takeInformation function).
     *
     * @param entity, the entity to index.
     * @param stringBuilder, the StringBuilder in which we store the relevant
     * parts of the message.
     */
    private static void takeInformation(Entity entity, StringBuilder stringBuilder) {
        // This is how you get the header.
        takeInformation(entity.getHeader(), stringBuilder);
        
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
                    ContentSet content0 = MimeParser.getContent(bodyParts.get(0));
                    ContentSet content1 = MimeParser.getContent(bodyParts.get(1));
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
                takeInformation(part, stringBuilder);
            }
            
            // The epilogue can be ignored.
            
        } else if (body instanceof MessageImpl) {
            // The body of the entity is another Message.
            takeInformation((MessageImpl) body, stringBuilder);
            
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
            ContentSet content = MimeParser.getContent(entity);
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
            
            stringBuilder.append(extractedText).append("\n");
            
        } else if (body instanceof BinaryBody) {
            // A binary body.
            // Do not index.
        }
    }
    
    /*
     * Reads an e-mail in the MIME format, and ouput the String that will be
     * taken into account for the indexation.
     */
    public static String parseNewMimeMessage(File file) {
        StringBuilder stringBuilder = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file)) {
            // Parse the MIME message from textual representation to mime4j.dom.Message representation.
            final Message message = builder.parse(fis).build();
            
            // Update the string builder with the content of the message.
            takeInformation(message, stringBuilder);

        } catch(FileNotFoundException e) {
            System.out.println("Unable to open file '" + file.getName() + "'.");

        } catch (IOException e) {
            System.out.println("IOException in file '" + file.getName() + "'.");
            e.printStackTrace();
        }
        return stringBuilder.toString(); 
    }
}
