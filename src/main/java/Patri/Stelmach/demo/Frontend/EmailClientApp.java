package Patri.Stelmach.demo.Frontend;

import Patri.Stelmach.demo.DTO.EmailDto;
import Patri.Stelmach.demo.Services.EmailExecutorService;
import Patri.Stelmach.demo.Services.EmailService;
import jakarta.mail.MessagingException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.AllArgsConstructor;
import org.controlsfx.control.Notifications;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class EmailClientApp extends Application
{
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;

    private  EmailService emailService = new EmailService();

    private TextField imapField = new TextField();
    private TextField userField = new TextField();
    private TextField passwordField = new PasswordField();
    private Button connectButton = new Button("Connect");
    private Button checkEmailsButton = new Button("Check Emails");
    private Button stopCheckingButton = new Button("Stop Checking");
    private Button disconnectButton = new Button("Disconnect");
    private Label inboxCountLabel = new Label();
    private ListView<String> emailListView = new ListView<>();


    private EmailExecutorService emailExecutorService = new EmailExecutorService(emailService);

    @Override
    public void start(Stage primaryStage)
    {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(10);
        root.setPadding(new Insets(20));

        root.setStyle("-fx-background-color: #333333;");

        Label title = new Label("Email Client");
        title.setFont(new Font(24));
        title.setTextFill(Color.WHITE);

        imapField.setPromptText("IMAP Server");
        userField.setPromptText("e-mail address");
        passwordField.setPromptText("password");

        connectButton.setOnAction(e -> connectToEmail());
        checkEmailsButton.setOnAction(e -> checkEmails());
        stopCheckingButton.setOnAction(e -> stopChecking());
        disconnectButton.setOnAction(e -> disconnect());

        inboxCountLabel.setTextFill(Color.WHITE);
        emailListView.setPrefHeight(300);

        root.getChildren().addAll(title, imapField, userField, passwordField, connectButton,
                inboxCountLabel, emailListView, checkEmailsButton, stopCheckingButton, disconnectButton);

        Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Email Client");
        primaryStage.show();
    }



    private void connectToEmail()
    {
        String imap = imapField.getText();
        String user = userField.getText();
        String password = passwordField.getText();

        try
        {
            emailService.establishConnection(imap, user, password);
            Notifications.create()
                    .title("Connection Successful")
                    .text("Connected to email server.")
                    .showConfirm();
            checkEmails();
            updateInbox();
            Notifications.create()
                    .title("Checking Emails")
                    .text("Email checking started.")
                    .showInformation();

        } catch (Exception e) {
            Notifications.create()
                    .title("Connection Failed")
                    .text("Failed to connect: " + e.getMessage())
                    .showError();
        }
    }

    private void updateInbox()
    {
        scheduledFuture = scheduler.scheduleWithFixedDelay(() ->
        {

            try
            {
                String user = userField.getText();
                int count = emailService.inboxCount(emailService.storeConnection(user));
                Platform.runLater(() -> {
                    inboxCountLabel.setText("Inbox Count: " + count);
                });

                List<EmailDto> emails = emailExecutorService.startSearching(emailService.storeConnection(user));

                Platform.runLater(() -> {
                    emailListView.getItems().clear();
                    for (EmailDto email : emails) {
                        emailListView.getItems().add(email.getSender() + " - " + email.getSubject());
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    Notifications.create()
                            .title("Error")
                            .text("Failed to update inbox: " + e.getMessage())
                            .showError();
                });
            }
        }, 0, 10, TimeUnit.SECONDS);
    }



    private void checkEmails()
    {
        scheduledFuture = scheduler.scheduleWithFixedDelay(() ->
            {
                try {
                    String user = userField.getText();
                    Platform.runLater(() -> {
                        try {
                            emailExecutorService.startEmailChecking(emailService.storeConnection(user));
                        } catch (MessagingException e) {
                            throw new RuntimeException(e);
                        }

                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        Notifications.create()
                                .title("Error")
                                .text("Failed to start email checking: " + e.getMessage())
                                .showError();
                    });
                }
            }, 0, 10, TimeUnit.SECONDS);
        }



    private void stopChecking()
    {
        try
        {
            emailExecutorService.stopEmailChecking();
            Notifications.create()
                    .title("Stopped Checking")
                    .text("Email checking stopped.")
                    .showInformation();
        } catch (Exception e) {
            Notifications.create()
                    .title("Error")
                    .text("Failed to stop email checking: " + e.getMessage())
                    .showError();
        }
    }

    private void disconnect()
    {
        try
        {
            String user = userField.getText();
            emailService.closeConnection(user);
            Notifications.create()
                    .title("Disconnected")
                    .text("Disconnected from email server.")
                    .showConfirm();
        } catch (Exception e) {
            Notifications.create()
                    .title("Error")
                    .text("Failed to disconnect: " + e.getMessage())
                    .showError();
        }
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}