//package Patri.Stelmach.demo.Controller;
//
//import Patri.Stelmach.demo.DTO.EmailDto;
//import Patri.Stelmach.demo.Services.EmailExecutorService;
//import Patri.Stelmach.demo.Services.EmailService;
//import jakarta.mail.MessagingException;
//import jakarta.mail.Store;
//import lombok.AllArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Collections;
//import java.util.List;
//
//@RequestMapping("/api")
//@RestController
//@AllArgsConstructor
//public class EmailController
//{
//
//    private final EmailService emailService;
//    private final EmailExecutorService emailExecutorService;
//
//
//
//    @PostMapping("/connect")
//    public ResponseEntity<String> connectEmail(@RequestParam String imap, @RequestParam String user, @RequestParam String password)
//    {
//        try
//        {
//            Store store = emailService.establishConnection(imap, user, password);
//            return ResponseEntity.ok("User" + user + "Connected successfully!");
//        } catch (MessagingException e) {
//            return ResponseEntity.status(500).body("Connection failed: " + e.getMessage());
//        }
//    }
//
//    @PostMapping("/check")
//    public ResponseEntity<String> checkEmails(@RequestParam String user)
//    {
//        try
//        {
//            Store store = emailService.storeConnection(user);
//            emailExecutorService.startEmailChecking(store);
//            return ResponseEntity.ok("Emails checked successfully!");
//        } catch (MessagingException e) {
//            return ResponseEntity.status(500).body("Error during email checking: " + e.getMessage());
//        }
//    }
//
//    @PostMapping("/stopChecking")
//    public ResponseEntity<String> stopChecking(String user) throws InterruptedException
//    {
//        try
//        {
//            Store store = emailService.storeConnection(user);
//
//            emailExecutorService.stopEmailChecking();
//            return ResponseEntity.ok().body("Checking stopped");
//        } catch (MessagingException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//    @GetMapping("/search")
//    public ResponseEntity <List<EmailDto>> searchEmails(@RequestParam String user)
//    {
//        try
//        {
//            Store store = emailService.storeConnection(user);
//            emailExecutorService.startSearching(store);
//            return ResponseEntity.ok().body(emailExecutorService.startSearching(store));
//        } catch (MessagingException e) {
//            return ResponseEntity.status(500).body(Collections.emptyList());
//        }
//    }
//
//    @GetMapping("/inboxCount")
//    public ResponseEntity<Integer> showInbox(@RequestParam String user)
//    {
//        try
//        {
//            Store store = emailService.storeConnection(user);
//            int count = emailService.inboxCount(store);
//            return ResponseEntity.ok().body(count);
//        } catch (MessagingException e) {
//            return ResponseEntity.status(500).body(0);
//        }
//    }
//
//    @PostMapping("/disconnect")
//    public ResponseEntity<String> closeConnection(@RequestParam String user)
//    {
//        try
//        {
//            emailService.closeConnection(user);
//            return ResponseEntity.ok().body("Connection closed!");
//        } catch (MessagingException e) {
//            return ResponseEntity.status(500).body("Error during closing connection: " + e.getMessage());
//        }
//    }
//}