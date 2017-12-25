package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

public class itemListController {
    private static Stage currentStage;
    private static int userID;
    private static String username;
    private Controller c = new Controller();
    private HashMap<Integer,Integer> itemMap;
    public static int selectedItemId;

    @FXML
    private ListView<String> itemList_list;

    public void initialize() {
        // initialization code here...
        itemMap = new HashMap<>();
        currentStage = Controller.currentStage;
        userID = Controller.userID;
        username = Controller.username;

        Table table = null;
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("items");
            int i = 0;
            for(Row row : table) {
                if (Integer.parseInt(row.get("userID").toString()) == userID) {
                    String desc = row.get("Description").toString();
                    String price = row.get("Price").toString();
                    String cat = row.get("Category").toString();
                    String avaiable = row.get("isAvailable").toString().toLowerCase().equals("true") ? "is available" : "is not available";
                    String tradable = row.get("isTradable").toString().toLowerCase().equals("true") ? "is tradable" : "is not tradable";

                    String listRow = desc + " (" + cat + ") - " + price + " - " + avaiable + ", " + tradable;
                    itemList_list.getItems().add(i,listRow);

                    itemMap.put(i,Integer.parseInt(row.get("ID").toString()));
                    i++;
                }
            }

            if(i==0){
                itemList_list.getItems().add(i,"No item was found..");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void backToHome() {
        try {
            c.switchScene("home.fxml","Everything4Rent", 700,450,"style.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleItemSelecting(MouseEvent mouseEvent) {

        if(mouseEvent.getClickCount() == 2) {
            int idx = itemList_list.getSelectionModel().getSelectedIndex();
            try {
                selectedItemId = itemMap.get(idx);
            }catch (Exception e){
                return;
            }

            try {
                c.switchScene("updateItem.fxml","Everything4Rent", 700,450,"style.css");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
