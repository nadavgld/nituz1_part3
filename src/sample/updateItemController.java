package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class updateItemController {
    private static Stage currentStage;
    private static int userID;
    private static String username;
    private Controller c = new Controller();
    private int selectedItemId;

    @FXML
    private TextField upd_description;
    @FXML
    private TextField upd_price;
    @FXML
    private ChoiceBox upd_cat;
    @FXML
    private CheckBox upd_available;
    @FXML
    private CheckBox upd_tradable;

    public void initialize() {
        // initialization code here...
        currentStage = Controller.currentStage;
        userID = Controller.userID;
        username = Controller.username;
        selectedItemId = itemListController.selectedItemId;

        LinkedList<String> s = new LinkedList<>(Arrays.asList(c.choices));
        Collections.sort(s);

        for(String type: s){
            upd_cat.getItems().add(type);
        }

        Table table = null;
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("items");
            for(Row row : table) {
                if (Integer.parseInt(row.get("ID").toString()) == selectedItemId) {
                    String desc = row.get("Description").toString();
                    String price = row.get("Price").toString();
                    String cat = row.get("Category").toString();
                    boolean available = row.get("isAvailable").toString().toLowerCase().equals("true") ? true : false;
                    boolean tradable = row.get("isTradable").toString().toLowerCase().equals("true") ? true : false;

                    upd_description.setText(desc);
                    upd_price.setText(price.substring(0,price.length()-1));
                    upd_cat.setValue(cat);
                    upd_available.setSelected(available);
                    upd_tradable.setSelected(tradable);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void backToHome() {
        try {
            c.switchScene("itemList.fxml","Everything4Rent", 700,450,"style.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateItem(ActionEvent actionEvent) {
        String desc = upd_description.getText();
        String price = upd_price.getText();
        String cat = upd_cat.getValue().toString();
        Boolean trade = upd_tradable.isSelected();
        Boolean available = upd_available.isSelected();

        try{
            Integer.parseInt(price);
        }catch (Exception e){
            c.showAlert(Alert.AlertType.WARNING,"Item Updating Error", "Price must be a number");
            return;
        }

        Table table = null;
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("items");
            for(Row row : table) {
                if (Integer.parseInt(row.get("ID").toString()) == selectedItemId) {
                    row.put("Description",desc);
                    row.put("Price",price+"$");
                    row.put("isAvailable",available);
                    row.put("Category",cat);
                    row.put("isTradable",trade);

                    table.updateRow(row);
                    c.showAlert(Alert.AlertType.INFORMATION,"Item Update Complete","Item update completed successfully");
                    backToHome();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void deleteItem(ActionEvent actionEvent) {
        Table table = null;
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("items");
            for(Row row : table) {
                if (Integer.parseInt(row.get("ID").toString()) == selectedItemId) {

                    table.deleteRow(row);
                    c.showAlert(Alert.AlertType.INFORMATION,"Item Deleted","Item deleted successfully");
                    backToHome();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
