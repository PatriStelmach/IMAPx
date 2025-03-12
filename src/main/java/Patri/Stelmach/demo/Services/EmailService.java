package Patri.Stelmach.demo.Services;

import Patri.Stelmach.demo.DTO.EmailDto;
import jakarta.mail.*;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.search.SubjectTerm;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import org.controlsfx.control.Notifications;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.hibernate.sql.ast.SqlTreeCreationLogger.LOGGER;

@Service
@RequiredArgsConstructor
public class EmailService
{
    private final Map<String, Store> storeMap = new ConcurrentHashMap<>();



    private String path = System.getProperty("user.home") + "/maile/";


    public void changePath(String path)
    {
       this.path = path;
    }

    //establishes connection with imap server
    public synchronized void establishConnection (String imap, String user, String password) throws MessagingException
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

        storeMap.get(user);
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
    public synchronized void closeConnection (String user) throws MessagingException, InterruptedException
    {
        if (storeMap.containsKey(user) && storeMap.get(user).isConnected())
        {
            //emailExecutorService.stopSearching(storeConnection(user));
            storeMap.get(user).close();
            storeMap.remove(user);
        }
    }

    /*searches for emails with attachments and "[RED]" in subject, then saves the subject in new folder
    (or not new, if it already exists) at /home/user, saves the attachment there,
     then moves given email to OLD-RED folder in your email box*/
    public void checkEmailsOnLogin(Store store) throws MessagingException
    {
        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_WRITE);
        try
        {
            System.out.println("Checking message");



            Message[] messages = inbox.search(new SubjectTerm("[RED]"));

            for (Message message : messages)
            {


                if (message.getSubject().contains("[RED]") &&hasAttachment(message)) {
                    System.out.println("Found message with attachment");
                    Path subjectPath = Paths.get(path + message.getSubject());

                    if(!Files.exists(subjectPath)) {
                        Files.createDirectories(subjectPath);
                    }
                    else { saveAttachments(message); }
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
    //loads every email message from the inbox and shows the first 10


    public List<EmailDto> searchAndCheckEmails(Store store) throws MessagingException, IOException
    {
        if (store.isConnected()) {
            Folder inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_WRITE);

            //counting every email
            int messageCount = inbox.getMessageCount();
            int start = Math.max(1, messageCount - 9);
            Message[] messages = inbox.getMessages(start, messageCount);

            //iterating from the back on the indexes, so the newest email messages are on the  top
            List<EmailDto> emailList = new ArrayList<>();
            for (int i = messages.length - 1; i >= 0; i--)
            {
                Address[] addresses = messages[i].getFrom();
                String sender = addresses.length > 0 ? addresses[0].toString() : "Unknown";

                String subject = messages[i].getSubject();
                emailList.add(new EmailDto(subject, sender));

                if (messages[i].getSubject().contains("[RED]") && hasAttachment(messages[i]))
                {

                    Path subjectPath = Paths.get(path + messages[i].getSubject());
                    Files.createDirectories(subjectPath);

                    saveAttachments(messages[i]);
                    moveToFolder(store, messages[i]);

                    int finalI = i;
                    Platform.runLater(() -> {
                        try {
                            Notifications.create()
                                    .title("Email Found")
                                    .text("Sender: " + messages[finalI].getFrom()[0] + "\n" +
                                            "Subject: " + messages[finalI].getSubject() + "\n" +
                                            "Attachments: " + getAttachmentCount(messages[finalI]) + "\n" +
                                            "Saved to: " + subjectPath.toString())
                                    .showInformation();
                        } catch (MessagingException | IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }


            inbox.close(false);
            return emailList;
        }
        else return null;
    }

    public int inboxCount (Store store) throws MessagingException
    {
        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_WRITE);
        LOGGER.info("Number of Messages : " + inbox.getMessageCount());
        inbox.close(true);

        return inbox.getMessageCount();
    }
    public List<EmailDto> searchOldRed(Store store) throws MessagingException, IOException
    {
        if (store.isConnected()) {
            Folder oldREd = store.getFolder("OLD-RED");
            oldREd.open(Folder.READ_WRITE);


            Message[] messages = oldREd.getMessages();

            List<EmailDto> emailList = new ArrayList<>();
            for (int i = messages.length - 1; i >= 0; i--)
            {
                Address[] addresses = messages[i].getFrom();
                String sender = addresses.length > 0 ? addresses[0].toString() : "Unknown";

                String subject = messages[i].getSubject();
                emailList.add(new EmailDto(subject, sender));

            }


            oldREd.close(false);
            return emailList;
        }
        else return null;
    }

    public int oldRedCount (Store store) throws MessagingException
    {
        Folder oldRed = store.getFolder("OLD-RED");
        oldRed.open(Folder.READ_WRITE);
        LOGGER.info("Number of Messages : " + oldRed.getMessageCount());
        oldRed.close(true);

        return oldRed.getMessageCount();
    }



    private int getAttachmentCount(Message message) throws MessagingException, IOException
    {
        int attachmentCount = 0;

        if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);


                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) ||
                        (bodyPart.getFileName() != null && !bodyPart.getFileName().isEmpty())) {
                    attachmentCount++;
                }
            }
        }

        return attachmentCount;
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

    //saves the attachments in directory with the name of the subject of the email message
    private void saveAttachments (Message message) throws MessagingException, IOException
    {
        Multipart multipart = (Multipart) message.getContent();
        for (int i = 0; i < multipart.getCount(); i++)
        {
            BodyPart part = multipart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()))
            {
                MimeBodyPart mimeBodyPart = (MimeBodyPart) part;
                File file = new File(path + message.getSubject() + "\\" + mimeBodyPart.getFileName());
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




    public String showEmail(Store store) throws MessagingException
    {
        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_WRITE);




        return "store";
    }

}
