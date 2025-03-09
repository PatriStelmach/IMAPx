package Patri.Stelmach.demo.Services;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.SortTerm;
import jakarta.mail.*;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.search.SubjectTerm;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;

import static org.hibernate.sql.ast.SqlTreeCreationLogger.LOGGER;

@Service
public class EmailService
{


    public Store establishConnection(String imap, String user, String password) throws MessagingException
    {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");

        Session session = Session.getDefaultInstance(props, null);

        Store store = session.getStore("imaps");
        store.connect(imap, user, password);

        return store;
    }
    public void checkEmails(Store store) throws MessagingException, IOException
    {
        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_WRITE);

        Message[] messages = inbox.search(new SubjectTerm("[RED]"));

        for (Message message : messages)
        {
            System.out.println( "checking for message");

            if (hasAttachment(message))
            {
                System.out.println( "there is something");
                Path subjectPath = Paths.get("C:\\Users\\ASUS\\Documents\\GitHub\\" + message.getSubject());
                    Files.createDirectories(subjectPath);

                saveAttachments(message);
                moveToFolder(store, message);


            }
        }

        inbox.close(true);
    }

    private boolean hasAttachment(Message message) throws MessagingException, IOException
    {

        if (message.isMimeType("multipart/*"))
        {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++)
            {
                BodyPart part = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void saveAttachments(Message message) throws MessagingException, IOException
    {
        Multipart multipart = (Multipart) message.getContent();
        for (int i = 0; i < multipart.getCount(); i++)
        {
            BodyPart part = multipart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()))
            {
                MimeBodyPart mimeBodyPart = (MimeBodyPart) part;
                File file = new File("C:\\Users\\ASUS\\Documents\\GitHub\\" + message.getSubject() + "\\" + mimeBodyPart.getFileName());
                mimeBodyPart.saveFile(file);
            }
        }
    }

    static void moveToFolder(Store store, Message message) throws MessagingException
    {
        Folder destinationFolder = store.getFolder("OLD-RED");
        if (!destinationFolder.exists())
        {
            destinationFolder.create(Folder.HOLDS_MESSAGES);
        }
        Message[] messagesToMove = new Message[] { message };
        message.getFolder().copyMessages(messagesToMove, destinationFolder);
        message.setFlag(Flags.Flag.DELETED, true);
    }

    static void emailCount(Store store) throws MessagingException
    {
        Folder inbox = store.getFolder("inbox");
        Folder spam = store.getFolder("[Gmail]/Spam");
        inbox.open(Folder.READ_ONLY);
        LOGGER.info("No of Messages : " + inbox.getMessageCount());
        LOGGER.info("No of Unread Messages : " + inbox.getUnreadMessageCount());
        LOGGER.info("No of Messages in spam : " + spam.getMessageCount());
        LOGGER.info("No of Unread Messages in spam : " + spam.getUnreadMessageCount());
        inbox.close(true);
    }


    public void searchEmails(Store store) throws MessagingException
    {
        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_ONLY);

        //liczenie wszystkich wiadomości
       int messageCount = inbox.getMessageCount();
       int start = Math.max(1, messageCount - 9);

        //tablica indeksów - od ostatniej wiadomości do 10 wiadomości
       Message[] messages = inbox.getMessages(start,messageCount);


       //iterowanie od tyłu, aby wyświetlały się od najnowszych
        for (int i = messages.length - 1; i >= 0; i--) {
            LOGGER.info("Subject: " + messages[i].getSubject());
            LOGGER.info("From: " + Arrays.toString(messages[i].getFrom()));
        }

        inbox.close(false);
    }






}

