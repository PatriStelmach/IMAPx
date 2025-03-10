package Patri.Stelmach.demo.Controller;

import Patri.Stelmach.demo.Services.EmailExecutorService;
import Patri.Stelmach.demo.Services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
@AllArgsConstructor
public class EmailController
{

    private final EmailService emailService;
    private final EmailExecutorService emailExecutorService;


    @PostMapping("/connect")
    public ResponseEntity<String> connectEmail(@RequestParam String imap, @RequestParam String user, @RequestParam String password)
    {
        try
        {
            Store store = emailService.establishConnection(imap, user, password);
            return ResponseEntity.ok("Connected successfully!");
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("Connection failed: " + e.getMessage());
        }
    }

    @PostMapping("/check")
    public ResponseEntity<String> checkEmails(@RequestParam String imap, @RequestParam String user, @RequestParam String password)
    {
        try
        {
            Store store = emailService.establishConnection(imap, user, password);
            emailExecutorService.startEmailChecking(store);
            return ResponseEntity.ok("Emails checked successfully!");
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("Error during email checking: " + e.getMessage());
        }
    }

    @PostMapping("/stopChecking")
    public ResponseEntity<String> stopChecking() {
        emailExecutorService.stopEmailChecking();
        return ResponseEntity.ok().body("Checking stopped");
    }

    @GetMapping("/search")
    public ResponseEntity<String> searchEmails(@RequestParam String imap, @RequestParam String user, @RequestParam String password)
    {
        try
        {
            Store store = emailService.establishConnection(imap, user, password);
            emailService.searchEmails(store);
            return ResponseEntity.ok("Search completed!");
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("Search failed: " + e.getMessage());
        }
    }

    @GetMapping("/inboxCount")
    public ResponseEntity<Integer> showInbox(@RequestParam String imap, @RequestParam String user, @RequestParam String password) throws MessagingException
    {
        Store store = emailService.establishConnection(imap, user, password);
        int count = emailService.inboxCount(store);
        return ResponseEntity.ok().body(count);
    }
}
