package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class addItemController {
    private static Stage currentStage;
    private static int userID;
    private static String username;
    private Controller c = new Controller();

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

    public void initialize() {
        // initialization code here...
        currentStage = Controller.currentStage;
        userID = Controller.userID;
        username = Controller.username;

        LinkedList<String> s = new LinkedList<>(Arrays.asList(c.choices));
        Collections.sort(s);

        for(String type: s){
            add_cat.getItems().add(type);
        }

    }

    public void backToHome() {
        try {
            c.switchScene("home.fxml","Everything4Rent", 700,450,"style.css");
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

        try {
            Table table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("items");
            table.addRow(null,userID,desc,price+"$",available,cat,trade);

            c.showAlert(Alert.AlertType.INFORMATION,"Item Adding", "Item was added successfully");
            backToHome();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
