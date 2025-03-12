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
@RequiredArgsConstructor
public class EmailExecutorService
{
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;
    private final EmailService emailService;


    //after 2 seconds of initial delay, every 10 seconds after the ond of the task, checkEmails is invoked
    public List<EmailDto> startSearching(Store store) throws MessagingException, IOException {
        scheduledFuture = scheduler.scheduleWithFixedDelay(() ->
        {
            try
            {
                emailService.searchAndCheckEmails(store);
            } catch (MessagingException | IOException e) {
                throw new RuntimeException(e);
            }
        }, 1,10,TimeUnit.SECONDS);
        return emailService.searchAndCheckEmails(store);
    }

    public List<EmailDto> startSearchingOldRed(Store store) throws MessagingException, IOException {
        scheduledFuture = scheduler.scheduleWithFixedDelay(() ->
        {
            try
            {
                emailService.searchOldRed(store);
            } catch (MessagingException | IOException e) {
                throw new RuntimeException(e);
            }
        }, 1,10,TimeUnit.SECONDS);
        return emailService.searchOldRed(store);
    }




}