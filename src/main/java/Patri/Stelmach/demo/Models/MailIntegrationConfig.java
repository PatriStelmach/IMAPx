package Patri.Stelmach.demo.Models;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.mail.*;
import org.springframework.integration.mail.dsl.Mail;

import java.util.Properties;

@Configuration
@EnableIntegration
public class MailIntegrationConfig {

    @Bean
    public DirectChannel inputChannel() {
        return new DirectChannel();
    }

    @Bean
    public ImapMailReceiver mailReceiver() {
        ImapMailReceiver mailReceiver = new ImapMailReceiver("imaps://user:password@imap.example.com:993/inbox");
        mailReceiver.setJavaMailProperties(javaMailProperties());
        return mailReceiver;
    }


    private Properties javaMailProperties() {
        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        javaMailProperties.setProperty("mail.imap.socketFactory.fallback", "false");
        return javaMailProperties;
    }
}
