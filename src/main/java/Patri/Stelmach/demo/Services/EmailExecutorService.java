package Patri.Stelmach.demo.Services;

import jakarta.mail.MessagingException;
import jakarta.mail.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    public void startEmailChecking(Store store)
    {
        if (scheduledFuture != null && !scheduledFuture.isDone())
        {
            System.out.println("Scheduler is already running");
        }
        else scheduledFuture = scheduler.scheduleAtFixedRate(() ->
        {
            try
            {
                emailService.checkEmails(store);
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void stopEmailChecking() throws InterruptedException {
        if(scheduledFuture == null)
        {
            System.out.println("Scheduler is not running");
        }
        if (scheduledFuture != null)
        {
            scheduledFuture.cancel(true);
        }
        scheduler.awaitTermination(3, TimeUnit.SECONDS);
    }
}
