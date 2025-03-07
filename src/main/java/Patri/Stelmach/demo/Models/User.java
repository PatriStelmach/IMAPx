package Patri.Stelmach.demo.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.MailReceiver;

@Entity
@Table(name = "user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long id;

    @Email(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "IMAP" )
    private String IMAP;

    @Column(name = "IMAP_port")
    private long imapPort;

    public ImapMailReceiver(String "")

}
