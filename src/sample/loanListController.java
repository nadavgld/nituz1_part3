package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

public class loanListController {
    private static Stage currentStage;
    private static int userID;
    private static String username;
    private Controller c = new Controller();
    private HashMap<Integer,Integer> itemMap;

    public static int selectedItemId;

    @FXML
    private ListView<String> itemList_list;
    @FXML
    private Label totalDeals;

    public void initialize() {
        currentStage = Controller.currentStage;
        userID = Controller.userID;
        username = Controller.username;

        itemMap = new HashMap<>();

        Table table = null;
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("lending");
            int i = 0;
            int expiredDeals = 0;
            for(Row row : table) {
                if (Integer.parseInt(row.get("loanerID").toString()) == userID) {
                    LocalDateTime startTime = updateItemController.strToDate(row.get("startTime").toString());
                    LocalDateTime returnTime = updateItemController.strToDate(row.get("returnTime").toString());

                    if(returnTime.isAfter(LocalDateTime.now()) || returnTime.isEqual(LocalDateTime.now())) {

                        String price = (row.get("tmura").toString());
                        String str_price = price.equals("0") ? "free" : price.chars().allMatch(Character::isDigit) ? price + "$" : price;
                        boolean isPackage = row.get("isPackage").toString().toLowerCase().equals("true") ? true : false;
                        int itemID = Integer.parseInt(row.get("itemID").toString());

                        Pair<String, String> itemDescription_Owner = getDescriptionToItem(isPackage, itemID);

                        String listRow = itemDescription_Owner.getKey() + " (Owner: " + itemDescription_Owner.getValue() + ") - from " + updateItemController.getDateFormat(startTime) + ", to " + updateItemController.getDateFormat(returnTime) + " for " + str_price;
                        itemList_list.getItems().add(i, listRow);

                        itemMap.put(i, Integer.parseInt(row.get("ID").toString()));
                        i++;
                    }else
                        expiredDeals++;
                }
            }

            totalDeals.setText(String.valueOf(expiredDeals));

            if(i==0){
                itemList_list.getItems().add(i,"No item was found..");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getOwnerToItem(int ID) {
        String res = null;

        Table table;
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("users");
            for(Row row : table) {
                if (Integer.parseInt(row.get("ID").toString()) == ID) {
                    res = row.get("Username").toString();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    public static Pair<String,String> getDescriptionToItem(boolean isPackage, int itemID) {
        Pair<String,String> res = null;

        Table table;
        String t = isPackage ? "packages" : "items";
        String ownerId = isPackage ? "ownerID" : "userID";
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable(t);
            for(Row row : table) {
                if (Integer.parseInt(row.get("ID").toString()) == itemID) {
                    String desc = row.get("Description").toString();
                    String owner = getOwnerToItem(Integer.parseInt(row.get(ownerId).toString()));
                    res = new Pair<>(desc,owner);

                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    public void backToHome() {
        try {
            c.switchScene("home.fxml","Everything4Rent", 700,450,"style.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
