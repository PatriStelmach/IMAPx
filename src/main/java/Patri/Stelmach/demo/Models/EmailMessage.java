package Patri.Stelmach.demo.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "EmailMessage")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessage
{
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO )
    private long id;


    @Column(name = "from")
    private String from;

    @Column(name = "to")
    private String to;

    @Column(name = "subject")
    private String subject;

    @Column(name = "body")
    private String body;

    @Column(name = "cc")
    private String cc;

    @Column(name = "bcc")
    private String bcc;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "is_read")
    private boolean isRead;

    public List<>

    @Column(name = "folder")
    @Enumerated(EnumType.STRING)
    private Folder folder;

}
