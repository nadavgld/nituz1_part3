package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class packageCreateController {
    private Controller c = new Controller();
    private static int userID = Controller.userID;
    public static String packageToCreate = itemListController.packageToCreate;
    public static ArrayList<Integer> selectedIdexes = itemListController.selectedIdexes;

    @FXML
    private CheckBox cp_trade;
    @FXML
    private CheckBox cp_avail;

    public void createPackage(ActionEvent actionEvent) {
        Table table = null;
        int p_id = -1;
        int summedPrice = calculateSummedPrice();

        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("packages");
            table.addRow(null,userID,cp_avail.isSelected(), cp_trade.isSelected(), 0, packageToCreate, -1, summedPrice);

            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("packages");
            for(Row row : table) {
                if (Integer.parseInt(row.get("ownerID").toString()) == userID && row.get("Description").toString().equals(packageToCreate)) {
                    p_id = Integer.parseInt(row.get("ID").toString());
                    break;
                }
            }

            if(p_id != -1){
                for(int i: selectedIdexes){
                    insertItemToPackage(i,p_id);
                }
                c.showAlert(Alert.AlertType.INFORMATION,"Package Creation", "Package " + packageToCreate + " created successfully");

                Stage stage = (Stage) cp_trade.getScene().getWindow();
                stage.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int calculateSummedPrice() {
        int p = 0;

        Table table;

        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("items");
            for (Row row : table) {
                if (selectedIdexes.contains(Integer.parseInt(row.get("ID").toString()))) {

                    String price = row.get("Price").toString();
                    p += Integer.parseInt(price.substring(0,price.length()-1));
                }
            }
        }catch (Exception e){ }
        return p;
    }

    private void insertItemToPackage(int i, int p_id) {
        Table table = null;
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("itemInPackage");
            table.addRow(null,p_id,i);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
