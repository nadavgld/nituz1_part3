package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
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
    @FXML
    private TextField search_min_price;
    @FXML
    private TextField search_max_price;
    @FXML
    private CheckBox search_cb_desc;
    @FXML
    private CheckBox search_cb_trade;
    @FXML
    private CheckBox search_cb_cat;
    @FXML
    private CheckBox search_cb_pack;



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
        if(keyEvent == null || keyEvent.getCode() == KeyCode.ENTER){
            String query = searchQuery.getText().toLowerCase();

            if(query.length() == 0) {
                search_list.getItems().clear();
                search_list.getItems().add(0,"No item was found..");
                return;
            }


            searchItem(query.trim());
            search_list.setVisible(true);
            searchLog(query.trim());
        }
    }

    private void searchLog(String query) {
        String timeStamp = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss").format(Calendar.getInstance().getTime());

        try {
            Table table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("userSearches");
            table.addRow(null,userID,query,timeStamp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchItem(String query) {
        boolean pack = search_cb_pack.isSelected();
        boolean toPrice = false;

        if(search_min_price.getText().length() > 0 || search_max_price.getText().length() > 0) {
            //TODO : add input check (numbers)
            if(search_min_price.getText().length() > 0){
                try{
                    Integer.parseInt(search_min_price.getText());
                }catch (Exception e){
                    c.showAlert(Alert.AlertType.ERROR,"Search Error","Min value must be a number");
                    return;
                }
            }

            if(search_max_price.getText().length() > 0){
                try{
                    Integer.parseInt(search_max_price.getText());
                }catch (Exception e){
                    c.showAlert(Alert.AlertType.ERROR,"Search Error","Max value must be a number");
                    return;
                }
            }
            toPrice = true;
        }

        String tableToCheck = pack ? "packages" : "items";
        Table table = null;
        search_list.getItems().clear();
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable(tableToCheck);
            int i = 0;
            for(Row row : table) {
                if (rowContainsQuery(row,query, pack,toPrice)) {
                    String desc = row.get("Description").toString();
                    String price = row.get("Price").toString();
                    String cat = pack ? "" : row.get("Category").toString();
                    String avaiable = row.get("isAvailable").toString().toLowerCase().equals("true") ? "is available" : "is not available";
                    String tradable = row.get("isTradable").toString().toLowerCase().equals("true") ? "is tradable" : "is not tradable";

                    String listRow = pack ? desc + " - " + price + "$ - " + avaiable + ", " + tradable : desc + " (" + cat + ") - " + price + " - " + avaiable + ", " + tradable;
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

//        return row.get("Description").toString().toLowerCase().contains(query) ||
    private boolean rowContainsQuery(Row row, String query, boolean pack, boolean price) {
//                row.get("Price").toString().toLowerCase().contains(query) ||
//                row.get("Category").toString().toLowerCase().contains(query);
        boolean desc = search_cb_desc.isSelected();
        boolean cat = search_cb_cat.isSelected();
        boolean trade = search_cb_trade.isSelected();

        boolean res = false;

        if(desc)
            res = res || row.get("Description").toString().toLowerCase().contains(query);

        if(cat && !pack)
            res = res || row.get("Category").toString().toLowerCase().contains(query);

        if(trade) {
            boolean isTradeable = row.get("isTradable").toString().toLowerCase().equals("true");
            res = res && isTradeable;
        }
//        }else{
//            boolean isTradeable = row.get("isTradable").toString().toLowerCase().equals("false");
//            res = res || isTradeable;
//        }

        //TODO: price between min-max;
        int itemPrice = pack ? Integer.parseInt(row.get("Price").toString()) : Integer.parseInt(row.get("Price").toString().substring(0,row.get("Price").toString().length()-1));
        int min = search_min_price.getText().length() == 0 ? 0 : Integer.parseInt(search_min_price.getText());
        int max = search_max_price.getText().length() == 0 ? Integer.MAX_VALUE : Integer.parseInt(search_max_price.getText());

        if(price){
                res = res && min <= itemPrice && max >= itemPrice;
        }
        return res;
    }

    public void btnSearch(ActionEvent actionEvent) {
        search(null);
    }
}
