package Patri.Stelmach.demo.Frontend;

import Patri.Stelmach.demo.DTO.EmailDto;
import Patri.Stelmach.demo.Services.EmailExecutorService;
import Patri.Stelmach.demo.Services.EmailService;
import jakarta.mail.MessagingException;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
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
    private TextField changePath = new TextField();
    private TextField passwordField = new PasswordField();
    private Button pathButton = new Button("Set path");
    private Button connectButton = new Button("Connect");
    private Button disconnectButton = new Button("Disconnect");
    private Label inboxCountLabel = new Label();
    private Label oldRedCountLabel = new Label();

    private ListView<String> emailListView = new ListView<>();
    private ListView<String> oldRedView = new ListView<>();

    private EmailExecutorService emailExecutorService = new EmailExecutorService(emailService);

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(true);

        BorderPane root = new BorderPane();

        HBox topBox = new HBox();
        topBox.setPadding(new Insets(10));
        topBox.setSpacing(10);

        Button exit = getButton();

        HBox.setHgrow(connectButton, Priority.ALWAYS);
        connectButton.setMaxWidth(Double.MAX_VALUE);

        topBox.getChildren().addAll(disconnectButton, connectButton, exit);
        root.setTop(topBox);

        VBox centerBox = new VBox();
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setSpacing(5);
        centerBox.setPadding(new Insets(20));

        VBox bottomBox  = new VBox();
        bottomBox.setAlignment(Pos.BOTTOM_CENTER);
        bottomBox.setSpacing(5);
        bottomBox.setPadding(new Insets(20));

        Label title = new Label("Email Client");

        title.setFont(new Font(48));
        title.setTextFill(Color.web("#68af25"));

        centerBox.getChildren().addAll
                (title, imapField, userField, passwordField, changePath, pathButton,
                        inboxCountLabel, emailListView);
        bottomBox.getChildren().addAll(oldRedCountLabel, oldRedView);
        root.setCenter(centerBox);
        root.setBottom(bottomBox);
        root.setStyle("-fx-background-color: #333333;");

        Scene scene = new Scene(root, 1000, 1000);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        disconnectButton.getStyleClass().add("button");
        connectButton.getStyleClass().add("button");
        exit.getStyleClass().add("button");
        pathButton.getStyleClass().add("button");

        imapField.setPromptText("IMAP Server");
        userField.setPromptText("e-mail address");
        passwordField.setPromptText("password");
        changePath.setPromptText("enter path where your attachments will be saved, default path is: /home/username/ ");

        connectButton.setOnAction(e -> connectToEmail());
        disconnectButton.setOnAction(e -> disconnect());
        pathButton.setOnAction(e -> settingPath(changePath.getText()));

        oldRedCountLabel.setFont(new Font(24));
        oldRedCountLabel.setTextFill(Color.WHITE);
        oldRedView.setPrefHeight(200);

        inboxCountLabel.setFont(new Font(24));
        inboxCountLabel.setTextFill(Color.WHITE);
        emailListView.setPrefHeight(200);



        primaryStage.setScene(scene);
        primaryStage.setTitle("Email Client");
        primaryStage.show();
    }

    private Button getButton()
    {

        Button exit = new Button("Exit");
        exit.setOnAction(e -> {
            disconnect();
            Notifications.create()
                    .title("Terminated")
                    .text("The application is shutting down")
                    .showInformation();

            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(event ->
            {
                Platform.exit();
                System.exit(0);
            });
            delay.play();

        });
        return exit;
    }




    private void settingPath(String path)
    {
        emailService.changePath(path);
        Platform.runLater(() -> {
            Notifications.create()
                    .title("Path established")
                    .text("Path set to: " + path)
                    .showConfirm();
        });
    }

    private void connectToEmail()
    {
        String imap = imapField.getText();
        String user = userField.getText();
        String password = passwordField.getText();

        Task<Void> connectionTask = new Task<Void>()
        {
            @Override
            protected Void call() throws Exception {
                emailService.establishConnection(imap, user, password);
                return null;
            }
            @Override
            protected void succeeded()
            {
                Platform.runLater(() -> {
                    Notifications.create()
                            .title("Connection Successful")
                            .text("Connected to e-mail server.")
                            .showConfirm();
                    checkEmails();
                    updateInbox();
                    updateOldRed();
                });
            }
            @Override
            protected void failed()
            {
                Platform.runLater(() -> {
                    Notifications.create()
                            .title("Connection Failed")
                            .text("Failed to connect: " + getException().getMessage())
                            .showError();
                });
            }
        };

        new Thread(connectionTask).start();
    }

    private void updateInbox()
    {
        if (scheduledFuture == null || scheduledFuture.isCancelled())
        {
            scheduledFuture = scheduler.scheduleWithFixedDelay(() -> {
                Task<Void> updateInboxTask = new Task<Void>()
                {
                    @Override
                    protected Void call() throws Exception
                    {
                        String user = userField.getText();
                        int count = emailService.inboxCount(emailService.storeConnection(user));

                        Platform.runLater(() -> {
                            inboxCountLabel.setText("Inbox Count: " + count);
                        });

                        List<EmailDto> emails = emailExecutorService.startSearching(emailService.storeConnection(user));
                        Platform.runLater(() -> {
                            emailListView.getItems().clear();
                            for (EmailDto email : emails)
                            {
                                emailListView.getItems().add(email.getSender() + " - " + email.getSubject());
                            }
                        });
                        return null;
                    }
                    @Override
                    protected void failed()
                    {
                        Platform.runLater(() -> {
                            Notifications.create()
                                    .title("Error")
                                    .text("Failed to update inbox: " + getException().getMessage())
                                    .showError();
                        });
                    }
                };

                new Thread(updateInboxTask).start();
            }, 7, 10, TimeUnit.SECONDS);
        }
    }

    public void updateOldRed()
    {
        if (scheduledFuture == null || scheduledFuture.isCancelled())
    {
        scheduledFuture = scheduler.scheduleWithFixedDelay(() -> {
            Task<Void> updateInboxTask = new Task<Void>()
            {
                @Override
                protected Void call() throws Exception
                {
                    String user = userField.getText();
                    int count = emailService.oldRedCount(emailService.storeConnection(user));

                    Platform.runLater(() -> {
                        oldRedCountLabel.setText("OLD-RED Count: " + count);
                    });

                    List<EmailDto> emails = emailExecutorService.startSearchingOldRed(emailService.storeConnection(user));
                    Platform.runLater(() -> {
                        oldRedView.getItems().clear();
                        for (EmailDto email : emails)
                        {
                            oldRedView.getItems().add(email.getSender() + " - " + email.getSubject());
                        }
                    });
                    return null;
                }
                @Override
                protected void failed()
                {
                    Platform.runLater(() -> {
                        Notifications.create()
                                .title("Error")
                                .text("Failed to update OLD-RED: " + getException().getMessage())
                                .showError();
                    });
                }
            };

            new Thread(updateInboxTask).start();
        }, 10, 10, TimeUnit.SECONDS);
    }
}


    private void checkEmails()
    {
        String user = userField.getText();

        Task<Void> checkEmailsTask = new Task<Void>()
        {
            @Override
            protected Void call() throws Exception
            {
                try
                {
                    emailService.checkEmailsOnLogin(emailService.storeConnection(user));
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
            @Override
            protected void succeeded()
            {
                Platform.runLater(() -> {
                    Notifications.create()
                            .title("E-mail Checked")
                            .text("E-mails have been checked successfully.")
                            .showInformation();
                });
            }
            @Override
            protected void failed()
            {
                Platform.runLater(() -> {
                    Notifications.create()
                            .title("Error")
                            .text("Failed to check e-mails: " + getException().getMessage())
                            .showError();
                });
            }
        };

        new Thread(checkEmailsTask).start();
    }

    private void disconnect()
    {
        try
        {
            String user = userField.getText();
            emailService.closeConnection(user);
            Notifications.create()
                    .title("Disconnected")
                    .text("Disconnected from e-mail server.")
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