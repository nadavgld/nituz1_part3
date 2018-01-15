package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class addItemController {
    private static int userID;
    private Controller c = new Controller();
    private Model model = Main.model;

    @FXML
    private TextField add_description;
    @FXML
    private TextField add_price;
    @FXML
    private ChoiceBox add_cat;
    @FXML
    private CheckBox add_available;
    @FXML
    private CheckBox add_tradable;
    @FXML
    private ToggleGroup loanType;

    public void initialize() {
        userID = Controller.userID;

        LinkedList<String> s = new LinkedList<>(Arrays.asList(c.choices));
        Collections.sort(s);

        for(String type: s){
            add_cat.getItems().add(type);
        }

    }

    public void backToHome() {
        try {
            c.switchScene("itemList.fxml","Everything4Rent", 700,450,"style.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addItem(ActionEvent actionEvent) {
        String desc = add_description.getText();
        String price = add_price.getText();
        String cat = add_cat.getValue().toString();
        Boolean trade = add_tradable.isSelected();
        Boolean available = add_available.isSelected();

        RadioButton l_Gender = (RadioButton)loanType.getSelectedToggle();
        String loan = l_Gender.getText();

        try{
            Integer.parseInt(price);
        }catch (Exception e){
            c.showAlert(Alert.AlertType.WARNING,"Item Adding Error", "Price must be a number");
            return;
        }

        if(desc == null || price == null || cat == null || trade == null || available == null){
            c.showAlert(Alert.AlertType.WARNING,"Item Adding Error", "Please fill all the fileds in the form");
            return;
        }

        if(desc.length() < 2){
            c.showAlert(Alert.AlertType.WARNING,"Item Adding Error", "Please fill a proper description");
            return;
        }

        try {

            model.addItem(userID,desc,price,available,cat,trade, loan);
            String mail = model.getMailByUSERID(userID,"users");

            String body = "Hey There! You've added new item ("+desc+" - Priced "+price+"$) to your profile at " + LocalDateTime.now() +".";
            try {
                c.sendMail(mail, body);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (MessagingException e) {
                e.printStackTrace();
            }

            c.showAlert(Alert.AlertType.INFORMATION,"Item Adding", "Item was added successfully");
            backToHome();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
