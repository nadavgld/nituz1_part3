package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;


public class Controller {
    private static String username = Main.username;
    private static String verifyUser;
    private Stage currentStage = Main.currentStage;
    private String dbPath = ".\\ndb.mdb";

    public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String lower = upper.toLowerCase();
    public static final String digits = "0123456789";
    public static final String alphanum = upper + lower + digits;

    @FXML
    private Button b_reg;
    @FXML
    private Button b_log;
    @FXML
    private Button b_login;
    @FXML
    private Button b_clear;
    @FXML
    private Button b_register;
    @FXML
    private Label reg_label;
    @FXML
    private TextField reg_name;
    @FXML
    private PasswordField reg_pass;
    @FXML
    private TextField reg_email;
    @FXML
    private TextField log_name;
    @FXML
    private PasswordField log_pass;
    @FXML
    private TextField ver_key;

    public void initialize() {
        // initialization code here...
    }

    public void registerPage(){
        try {
            switchScene("register.fxml","Everything4Rent Register", 700,450,"style.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loginPage(){

        if(username != null)
            try {
                switchScene("home.fxml","Everything4Rent", 700,450,"style.css");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }

        try {
            switchScene("login.fxml","Everything4Rent Login", 700,450,"style.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchScene(String fxml, String title, int width, int height, String style) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        root.getStylesheets().add(getClass().getResource(style).toString());
        currentStage.setTitle(title);
        currentStage.setScene(new Scene(root, width, height));
        currentStage.show();
    }

    public void backToHomePage() {
        try {
            switchScene("sample.fxml","Everything4Rent", 700,450,"style.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerNewUser(ActionEvent actionEvent) {
        String name = reg_name.getText().trim();
        String pass = reg_pass.getText().trim();
        String mail = reg_email.getText().trim();

        if(name.length() == 0 || pass.length() == 0 || mail.length() == 0){
            showAlert(Alert.AlertType.WARNING,"Registration Error","All fields must be filled");
        }else{
            insertNewUser(name,pass,mail);
        }
    }

    private void insertNewUser(String name, String pass, String mail) {
        Boolean found = false;
        Table table = null;
        try {
            table = DatabaseBuilder.open(new File(dbPath)).getTable("users");
            for(Row row : table) {
                if (row.get("Username").toString().equals(name)) {
                    showAlert(Alert.AlertType.ERROR, "Registration Error", "Username already exists");
                    found = true;
                    break;
                }
            }

            if(!found) {
                String userKey = generateKey();
                table.addRow(null, name, pass,mail,0,userKey);
                sendMail(mail, name, userKey);
                showAlert(Alert.AlertType.CONFIRMATION,"Registration completed", name + ", Welcome to Everything4Rent\nan E-Mail with details is on it's way\n(You might find it on 'spam' folder)");

                backToHomePage();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String generateKey() {
        StringBuilder sb = new StringBuilder();
        Random r = new Random();

        for(int i=0;i<10;i++) {
            sb.append(alphanum.charAt(r.nextInt(alphanum.length()) + 1));
            if(i==4)
                sb.append("-");
        }

        return sb.toString();
    }

    private void sendMail(String mail, String name, String key) throws IOException, MessagingException {

        String smtpServer = "smtp.gmail.com";
        int port = 587;
        final String userid = "everythingforrent2017@gmail.com";//change accordingly
        final String password = "dang1029";//change accordingly
        String contentType = "text/html";
        String subject = "Welcome to Everything4Rent";
        String from = "Everything4Rent <everythingforrent2017@gmail.com>";
        String to = mail;//some invalid address
        String bounceAddr = mail;//change accordingly
        String body = "Hello " + name + ", Welcome to Everything4Rent.\n Please verify your registration with the following code in first login: \n" + key;

        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.smtp.port", "587");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.from", bounceAddr);

        Session mailSession = Session.getInstance(props,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userid, password);
                }
            }
        );

        MimeMessage message = new MimeMessage(mailSession);
        message.addFrom(InternetAddress.parse(from));
        message.setRecipients(Message.RecipientType.TO, to);
        message.setSubject(subject);
        message.setContent(body, contentType);

        Transport transport = mailSession.getTransport();

        new Thread(()-> {
            try {
                System.out.println("Sending ....");
                transport.connect(smtpServer, port, userid, password);
                transport.sendMessage(message,
                        message.getRecipients(Message.RecipientType.TO));
                System.out.println("Sending done ...");
            } catch (Exception e) {
                System.err.println("Error Sending: ");
                e.printStackTrace();

            }
            try {
                transport.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void showAlert(Alert.AlertType a, String title, String header){
        Alert alert = new Alert(a);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    public void clearRegistrationFields(ActionEvent actionEvent) {
        reg_pass.setText("");
        reg_name.setText("");
        reg_email.setText("");
    }

    public void loginUser(ActionEvent actionEvent) {
        String name = log_name.getText().trim();
        String pass = log_pass.getText().trim();

        int found = 0;

        Table table = null;
        try {
            table = DatabaseBuilder.open(new File(dbPath)).getTable("users");
            for(Row row : table) {
                if (row.get("Username").toString().toLowerCase().equals(name.toLowerCase()) && row.get("Password").toString().equals(pass)) {
                    boolean verify = (Boolean) row.get("Verification");

                    if(!verify){
                        verifyUser = name;
                        switchScene("verify.fxml","Everything4Rent Login", 700,450,"style.css");
                    }else{
                        username = name;
                        switchScene("home.fxml","Everything4Rent", 700,450,"style.css");
                    }

                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void verifyUser(ActionEvent actionEvent) {
        String key = ver_key.getText().trim();

        Table table = null;
        try {
            table = DatabaseBuilder.open(new File(dbPath)).getTable("users");
            for(Row row : table) {
                if (row.get("Username").toString().toLowerCase().equals(verifyUser.toLowerCase()) && row.get("verificationPass").toString().equals(key)) {
                    row.put("Verification",true);
                    table.updateRow(row);

                    showAlert(Alert.AlertType.CONFIRMATION,"Everything4Rent", "Your user has been verified");
                    switchScene("home.fxml","Everything4Rent", 700,450,"style.css");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        username = verifyUser;
        Main.username = username;
        verifyUser = null;
    }
}