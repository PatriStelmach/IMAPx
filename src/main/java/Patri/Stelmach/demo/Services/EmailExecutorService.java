package Patri.Stelmach.demo.Services;

import Patri.Stelmach.demo.DTO.EmailDto;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service

public class EmailExecutorService
{
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;
    private final EmailService emailService;

    public EmailExecutorService (EmailService emailService)
    {
        this.emailService = emailService;
    }

    //after 2 seconds of initial delay, every 10 seconds after the ond of the task, checkEmails is invoked
    public void startEmailChecking(Store store)
    {
        if (scheduledFuture == null || scheduledFuture.isCancelled())
        {
            scheduledFuture = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    emailService.checkEmails(store);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, 30, TimeUnit.SECONDS);
        }
    }

    public List<EmailDto> startSearching(Store store) throws MessagingException
    {
        scheduledFuture = scheduler.scheduleWithFixedDelay(() ->
        {
            try
            {
                emailService.searchEmails(store);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }, 0,10,TimeUnit.SECONDS);
        return emailService.searchEmails(store);
    }

    //interrupts the email checking after 3 seconds so the data is saved
    public void stopEmailChecking() throws InterruptedException
    {
        if (scheduledFuture != null)
        {
            scheduledFuture.cancel(true);
            emailService.stopCheckingEmails();
            scheduledFuture = null;
        }
        scheduler.awaitTermination(3, TimeUnit.SECONDS);
    }
}