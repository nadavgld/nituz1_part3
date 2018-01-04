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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class updateItemController {
    private static Stage currentStage;
    private static int userID;
    private static String username;
    private Controller c = new Controller();
    private int selectedItemId;

    private static String itemDesc;
    private static int loanerID = -1;
    private static HashMap<Integer,String> usersIdMap = new HashMap<>();
    private static HashMap<Integer,Integer> usersIdIndexInList = new HashMap<>();
    private static long lastKeyPress;

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
    private Button b_lendItem;

    @FXML
    private ListView lend_listView;
    @FXML
    private TextField lend_filter;

    public void initialize() {
        // initialization code here...
        if(upd_cat != null) {
            currentStage = Controller.currentStage;
            userID = Controller.userID;
            username = Controller.username;
            selectedItemId = itemListController.selectedItemId;

            LinkedList<String> s = new LinkedList<>(Arrays.asList(c.choices));
            Collections.sort(s);

            for (String type : s) {
                upd_cat.getItems().add(type);
            }

            Table table = null;
            try {
                table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("items");
                for (Row row : table) {
                    if (Integer.parseInt(row.get("ID").toString()) == selectedItemId) {
                        String desc = row.get("Description").toString();
                        String price = row.get("Price").toString();
                        String cat = row.get("Category").toString();
                        boolean available = row.get("isAvailable").toString().toLowerCase().equals("true") ? true : false;
                        boolean tradable = row.get("isTradable").toString().toLowerCase().equals("true") ? true : false;

                        loanerID = Integer.parseInt(row.get("lendingID").toString());
                        itemDesc = desc;

                        upd_description.setText(desc);
                        upd_price.setText(price.substring(0, price.length() - 1));
                        upd_cat.setValue(cat);
                        upd_available.setSelected(available);
                        upd_tradable.setSelected(tradable);

                        b_lendItem.setDisable(!available);

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
        }else if(lend_filter != null){
            loadListOfUsers("");
        }
    }

    private void loadListOfUsers(String filter) {
        lend_listView.getItems().removeAll();
        Table table = null;

        usersIdMap = new HashMap<>();
        usersIdIndexInList = new HashMap<>();

        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("users");
            int i = 0;
            for(Row row : table) {
                if (row.get("Username").toString().toLowerCase().contains(filter) && Integer.parseInt(row.get("ID").toString()) != userID && !row.get("userType").toString().equals("Owner") && row.get("Verification").toString().toLowerCase().equals("true")) {
                    String user = row.get("Username").toString();
                    String type = row.get("userType").toString();
                    int id = Integer.parseInt(row.get("ID").toString());

                    String listRow = user +" (Type: " + type + ")";
                    lend_listView.getItems().add(i,listRow);

                    usersIdMap.put(id,user);
                    usersIdIndexInList.put(id,i);

                    i++;
                }
            }

            if(i==0){
                lend_listView.getItems().add(i,"Could not find any relevant Loaner..");
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

        if(desc.length() < 2){
            c.showAlert(Alert.AlertType.WARNING,"Item Adding Error", "Please fill a proper description");
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

    public void lendItem(ActionEvent actionEvent) {
        Parent root = null;
        try {
            Stage s = new Stage();
            root = FXMLLoader.load(getClass().getResource("lendItem.fxml"));
            root.getStylesheets().add(getClass().getResource("style.css").toString());
            s.setTitle("Lend " + itemDesc);
            s.setScene(new Scene(root, 300, 300));
            s.initModality(Modality.WINDOW_MODAL);
            s.initOwner(currentStage);
            s.setResizable(false);
            s.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void availabilityChange(MouseEvent mouseEvent) {
        if(!upd_available.isSelected())
            b_lendItem.setDisable(true);
        else{
            b_lendItem.setDisable(false);
        }
    }

    public void filterByName(KeyEvent keyEvent) {
        long kp = System.currentTimeMillis();

        if(kp - lastKeyPress > 1200) {
            String currentFilter = lend_filter.getText();
            loadListOfUsers(currentFilter);
        }
        lastKeyPress = kp;

    }
}
