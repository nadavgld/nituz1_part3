package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jdk.nashorn.internal.runtime.ECMAException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class updateItemController {
    private Controller c = new Controller();
    private static int selectedItemId;
    private Model model = Main.model;

    private static int loanerID = -1;


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
    @FXML
    private ToggleGroup loanType;

    public void initialize() {
        if(upd_cat != null) {
            selectedItemId = itemListController.selectedItemId;

            LinkedList<String> s = new LinkedList<>(Arrays.asList(c.choices));
            Collections.sort(s);

            for (String type : s) {
                upd_cat.getItems().add(type);
            }

            Table table = null;
            try {
                table = model.getDBtable("items");
                for (Row row : table) {
                    if (Integer.parseInt(row.get("ID").toString()) == selectedItemId) {
                        String desc = row.get("Description").toString();
                        String price = row.get("Price").toString();
                        String cat = row.get("Category").toString();
                        boolean available = row.get("isAvailable").toString().toLowerCase().equals("true") ? true : false;
                        boolean tradable = row.get("isTradable").toString().toLowerCase().equals("true") ? true : false;

                        for(Toggle r: loanType.getToggles()) {
                            RadioButton r_Type = (RadioButton) r;
                            String type = r_Type.getText();

                            if(type.equals(row.get("loanType").toString())){
                                r_Type.setSelected(true);
                                break;
                            }
                        }

                        upd_description.setText(desc);
                        upd_price.setText(price.substring(0, price.length() - 1));
                        upd_cat.setValue(cat);
                        upd_available.setSelected(available);
                        upd_tradable.setSelected(tradable);

                        if (loanerID != -1)
                            upd_available.setDisable(true);
                        else
                            upd_available.setDisable(false);

                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void backToHome() {
        try {
            c.switchScene("itemList.fxml","Everything4Rent", 700,450,"style.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateItem() {
        String desc = upd_description.getText();
        String price = upd_price.getText();
        String cat = upd_cat.getValue().toString();
        Boolean trade = upd_tradable.isSelected();
        Boolean available = upd_available.isSelected();

        RadioButton l_Gender = (RadioButton)loanType.getSelectedToggle();
        String loan = l_Gender.getText();
        try{
            Integer.parseInt(price);
        }catch (Exception e){
            c.showAlert(Alert.AlertType.WARNING,"Item Updating Error", "Price must be a number");
            return;
        }

        if(desc.length() < 2){
            c.showAlert(Alert.AlertType.WARNING,"Item Adding Error", "Please fill a proper description");
            return;
        }

        try {
            model.updateItemInfo("items",desc,price,available,cat,trade,selectedItemId,loan);
        } catch (IOException e) {
            e.printStackTrace();
        }
        c.showAlert(Alert.AlertType.INFORMATION,"Item Update Complete","Item update completed successfully");
        backToHome();

    }

    public void deleteItem() {

        boolean isOrdered = false;
        try {
            isOrdered = model.checkIfItemIsOrdered("lending",selectedItemId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(isOrdered){
            c.showAlert(Alert.AlertType.ERROR,"Item Deletion Error","Cannot delete item because it has future orders");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Item Deletion");
        alert.setHeaderText("Are you sure you want to delete this item?");
        Optional<ButtonType> result = alert.showAndWait();

        if(result.get() == ButtonType.OK) {
            try {
                model.deleteItem("items", selectedItemId);
            } catch (IOException e) {
                e.printStackTrace();
            }
            c.showAlert(Alert.AlertType.INFORMATION, "Item Deleted", "Item deleted successfully");
            backToHome();
        }
    }

    public static String getDateFormat(LocalDateTime to) {
        String[] sp =  to.toString().split("-");
        String year = sp[0];
        String month = sp[1];

        sp = sp[2].split("T");
        String day = sp[0];


        return day + "/" + month + "/" + year;
    }

    public static LocalDateTime strToDate(String returnTime) {
        LocalDateTime l = null;
        try{

            String[] sp = returnTime.split("-");
            int year = Integer.parseInt(sp[0].trim());
            int month = Integer.parseInt(sp[1].trim());

            sp = sp[2].split("T");
            int day = Integer.parseInt(sp[0].trim());

            sp = sp[1].split(":");
            int hour = Integer.parseInt(sp[0].trim());
            int min = Integer.parseInt(sp[1].trim());

            l = LocalDateTime.of(year,month,day,hour,min);

        }catch (Exception e){}

        return l;
    }


}
