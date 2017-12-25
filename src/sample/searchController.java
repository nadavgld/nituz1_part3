package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class searchController {
    private static Stage currentStage;
    private static int userID;
    private static String username;
    private Controller c = new Controller();

    @FXML
    private TextField searchQuery;
    @FXML
    private ListView search_list;

    public void initialize() {
        // initialization code here...
        currentStage = Controller.currentStage;
        userID = Controller.userID;
        username = Controller.username;

        search_list.setVisible(false);
    }

    public void backToHome() {
        try {
            c.switchScene("home.fxml","Everything4Rent", 700,450,"style.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void search(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER){
            String query = searchQuery.getText().toLowerCase();
            search_list.setVisible(true);

            searchItem(query);
            searchLog(query);
        }
    }

    private void searchLog(String query) {
        String timeStamp = new SimpleDateFormat("dd/MM/YYYY").format(Calendar.getInstance().getTime());

        try {
            Table table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("userSearches");
            table.addRow(null,userID,query,timeStamp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchItem(String query) {
        Table table = null;
        search_list.getItems().clear();
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("items");
            int i = 0;
            for(Row row : table) {
                if (rowContainsQuery(row,query)) {
                    String desc = row.get("Description").toString();
                    String price = row.get("Price").toString();
                    String cat = row.get("Category").toString();
                    String avaiable = row.get("isAvailable").toString().toLowerCase().equals("true") ? "is available" : "is not available";
                    String tradable = row.get("isTradable").toString().toLowerCase().equals("true") ? "is tradable" : "is not tradable";

                    String listRow = desc + " (" + cat + ") - " + price + " - " + avaiable + ", " + tradable;
                    search_list.getItems().add(i,listRow);

                    i++;
                }
            }
            if(i==0){
                search_list.getItems().add(i,"No item was found..");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean rowContainsQuery(Row row, String query) {
        return row.get("Description").toString().toLowerCase().contains(query) ||
                row.get("Price").toString().toLowerCase().contains(query) ||
                row.get("Category").toString().toLowerCase().contains(query);
    }
}
