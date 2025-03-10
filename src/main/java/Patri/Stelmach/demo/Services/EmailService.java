package Patri.Stelmach.demo.Services;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static org.hibernate.sql.ast.SqlTreeCreationLogger.LOGGER;

@Service
public class EmailService
{
    private final Map<String, Store> storeMap = new ConcurrentHashMap<>();


    public synchronized Store establishConnection (String imap, String user, String password) throws MessagingException
    {
        if (!storeMap.containsKey(user) || !storeMap.get(user).isConnected())
        {
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imaps");

            Session session = Session.getDefaultInstance(props, null);

            Store store = session.getStore("imaps");
            store.connect(imap, user, password);

            storeMap.put(user, store);
        }

        return storeMap.get(user);
    }

    public synchronized Store storeConnection (String user) throws MessagingException
    {
        if (!storeMap.containsKey(user) || !storeMap.get(user).isConnected()) {
            throw new MessagingException("User " + user + " not connected");
        }
        return storeMap.get(user);
    }

    public synchronized void closeConnection (String user) throws MessagingException
    {
        if (storeMap.containsKey(user) && storeMap.get(user).isConnected()){
            storeMap.get(user).close();
            storeMap.remove(user);
    }
    }
    public void checkEmails (Store store) throws MessagingException, IOException
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
                Path subjectPath = Paths.get("C:\\Users" + message.getSubject());
                    Files.createDirectories(subjectPath);

                saveAttachments(message);
                moveToFolder(store, message);
            }
        }

        inbox.close(true);
    }

    private boolean hasAttachment (Message message) throws MessagingException, IOException
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

    private void saveAttachments (Message message) throws MessagingException, IOException
    {
        Multipart multipart = (Multipart) message.getContent();
        for (int i = 0; i < multipart.getCount(); i++)
        {
            BodyPart part = multipart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()))
            {
                MimeBodyPart mimeBodyPart = (MimeBodyPart) part;
                File file = new File("C:\\Users" + message.getSubject() + "\\" + mimeBodyPart.getFileName());
                mimeBodyPart.saveFile(file);
            }
        }
    }

    static void moveToFolder (Store store, Message message) throws MessagingException
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

    public int inboxCount (Store store) throws MessagingException
    {
        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_ONLY);
        LOGGER.info("Number of Messages : " + inbox.getMessageCount());
        inbox.close(true);

        return inbox.getMessageCount();
    }


    public void searchEmails (Store store) throws MessagingException
    {
        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_ONLY);

        //liczenie wszystkich wiadomości
       int messageCount = inbox.getMessageCount();
       int start = Math.max(1, messageCount - 9);

        //tablica indeksów - od ostatniej wiadomości do 10 wiadomości
       Message[] messages = inbox.getMessages(start,messageCount);


       //iterowanie od tyłu, aby wyświetlały się od najnowszych
        for (int i = messages.length - 1; i >= 0; i--)
        {
            LOGGER.info("Subject: " + messages[i].getSubject());
            LOGGER.info("From: " + Arrays.toString(messages[i].getFrom()));
        }

        inbox.close(false);
    }






}

