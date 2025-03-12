package Patri.Stelmach.demo.Services;

import Patri.Stelmach.demo.DTO.EmailDto;
import jakarta.mail.*;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.search.SubjectTerm;
import javafx.application.Platform;
import org.controlsfx.control.Notifications;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.hibernate.sql.ast.SqlTreeCreationLogger.LOGGER;

@Service
public class EmailService
{
    private final Map<String, Store> storeMap = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    public void stopCheckingEmails()
    {
        running = false;
    }

    public void startCheckingEmails()
    {
        running = true;
    }

    //establishes connection with imap server
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

    //stores the connection established in establishConnection in a hashMap, that you can refer to using user -> email address
    public synchronized Store storeConnection (String user) throws MessagingException
    {
        if (!storeMap.containsKey(user) || !storeMap.get(user).isConnected()) {
            throw new MessagingException("User " + user + " not connected");
        }
        return storeMap.get(user);
    }

    //closes the connection of given user using a hashMap
    public synchronized void closeConnection (String user) throws MessagingException
    {
        if (storeMap.containsKey(user) && storeMap.get(user).isConnected())
        {
            storeMap.get(user).close();
            storeMap.remove(user);
        }
    }

    /*searches for emails with attachments and "[RED]" in subject, then saves the subject in new folder
    (or not new, if it already exists) at /home/user, saves the attachment there,
     then moves given email to OLD-RED folder in your email box*/
    public void checkEmails(Store store)
    {
        Folder inbox = null;
        try
        {
            System.out.println("Checking message");
            inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_WRITE);


            Message[] messages = inbox.search(new SubjectTerm("[RED]"));

            for (Message message : messages) {


                if (hasAttachment(message)) {
                    System.out.println("Found message with attachment");
                    Path subjectPath = Paths.get("C:\\Users\\stelmach.p\\OneDrive - Gdańskie Centrum Informatyczne\\Dokumenty\\GitHub\\cotam\\" + message.getSubject());
                    Files.createDirectories(subjectPath);

                    saveAttachments(message);
                    moveToFolder(store, message);

                    Platform.runLater(() -> {
                        try {
                            Notifications.create()
                                    .title("Email Found")
                                    .text("Sender: " + message.getFrom()[0] + "\n" +
                                            "Subject: " + message.getSubject() + "\n" +
                                            "Attachments: " + getAttachmentCount(message) + "\n" +
                                            "Saved to: " + subjectPath.toString())
                                    .showInformation();
                        } catch (MessagingException | IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        } finally {
            if (inbox != null && inbox.isOpen()) {
                try {
                    inbox.close(false);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private int getAttachmentCount(Message message) throws MessagingException, IOException
    {
        int count = 0;
        if(message.isMimeType("multipart/**"))
        {
            Multipart multipart = (Multipart) message.getContent();

            for(int i = 0; i < multipart.getCount(); i++)
            {
                BodyPart part = multipart.getBodyPart(i);
                if(Part.ATTACHMENT.equalsIgnoreCase(part.getDescription()))
                {
                    count++;
                }
            }
        }
        return count;
    }
    //searches for multiparts - attachements in email message
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

    //saves the attachements in directory with the name of the subject of the email message
    private void saveAttachments (Message message) throws MessagingException, IOException
    {
        Multipart multipart = (Multipart) message.getContent();
        for (int i = 0; i < multipart.getCount(); i++)
        {
            BodyPart part = multipart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()))
            {
                MimeBodyPart mimeBodyPart = (MimeBodyPart) part;
                File file = new File("C:\\Users\\stelmach.p\\OneDrive - Gdańskie Centrum Informatyczne\\Dokumenty\\GitHub\\cotam\\" + message.getSubject() + "\\" + mimeBodyPart.getFileName());
                mimeBodyPart.saveFile(file);
            }
        }
    }

    //moves the message from inbox to OLD-RED and creates it if it doesn't exist
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

    //returns value of emails in the inbox
    public int inboxCount (Store store) throws MessagingException
    {
        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_ONLY);
        LOGGER.info("Number of Messages : " + inbox.getMessageCount());
        inbox.close(true);

        return inbox.getMessageCount();
    }

    //loads every email message from the inbox and shows the first 10
    public List<EmailDto> searchEmails (Store store) throws MessagingException
    {

        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_ONLY);

        //counting every email
        int messageCount = inbox.getMessageCount();
        int start = Math.max(1, messageCount - 9);
        Message[] messages = inbox.getMessages(start,messageCount);

        //iterating from the back on the indexes, so the newest email messages are on the  top
        List<EmailDto> emailList = new ArrayList<>();
        for (int i = messages.length - 1; i >= 0; i--)
        {
            Address[] addresses = messages[i].getFrom();
            String sender = addresses.length > 0 ? addresses[0].toString() : "Unknown";
            String subject = messages[i].getSubject();
            emailList.add(new EmailDto(subject, sender));
        }

        inbox.close(false);
        return emailList;
    }

    public String showEmail(Store store) throws MessagingException
    {
        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_ONLY);




        return "store";
    }

}
