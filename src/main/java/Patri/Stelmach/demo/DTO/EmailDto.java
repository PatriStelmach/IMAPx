package Patri.Stelmach.demo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;


public class EmailDto
{
    private String subject;
    private String sender;

    public EmailDto(String subject, String sender)
    {
        this.subject = subject;
        this.sender = sender;
    }

    public String getSubject() {
        return subject;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setSubject(String subject) {
        this.subject = subject;


    }
}