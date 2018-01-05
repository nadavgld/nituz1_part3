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
    private static Stage currentStage;
    private static int userID;
    private static String username;
    private Controller c = new Controller();
    private static int selectedItemId;

    private static String itemDesc;
    private static int loanerID = -1;
    private static HashMap<Integer,String> usersIdMap = new HashMap<>();
    private static HashMap<Integer,Integer> usersIdIndexInList = new HashMap<>();
    private static long lastKeyPress;

    private static Stage lendMenu;

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
//    @FXML
//    private Button b_lendItem;

    @FXML
    private ListView lend_listView;
    @FXML
    private TextField lend_filter;
    @FXML
    private DatePicker lend_from;
    @FXML
    private DatePicker lend_to;

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

//                        b_lendItem.setDisable(!available);

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
            //Initiate full list of users
            loadListOfUsers("");

            //Validate From cannot be before today
            lend_from.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(LocalDate.now()));
                }
            });

            lend_to.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(LocalDate.now()));
                }
            });

            //Return day is Taking day +1
            lend_from.valueProperty().addListener((ov, oldValue, newValue) -> {
                lend_to.setValue(newValue.plusDays(1));
                lend_to.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        setDisable(empty || date.isBefore(newValue));
                    }
                });
            });

            //Validate Return date is not before taking
            lend_to.valueProperty().addListener((ov, oldValue, newValue) -> {
                try {
                    if (newValue.isBefore(lend_from.getValue())) {
                        lend_to.setValue(oldValue);
                        c.showAlert(Alert.AlertType.ERROR, "Date Error", "Cannot return item before lending");
                    }
                }catch (Exception e){}
            });
        }
    }

    private void loadListOfUsers(String filter) {
        lend_listView.getItems().clear();

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
                    usersIdIndexInList.put(i,id);

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
            lendMenu = new Stage();
            root = FXMLLoader.load(getClass().getResource("lendItem.fxml"));
            root.getStylesheets().add(getClass().getResource("style.css").toString());
            lendMenu.setTitle("Lend " + itemDesc);
            lendMenu.setScene(new Scene(root, 450, 350));
            lendMenu.initModality(Modality.WINDOW_MODAL);
            lendMenu.initOwner(currentStage);
            lendMenu.setResizable(false);
            lendMenu.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    public void availabilityChange() {
//        if(!upd_available.isSelected())
//            b_lendItem.setDisable(true);
//        else{
//            b_lendItem.setDisable(false);
//        }
    }

    public void filterByName() {
        long kp = System.currentTimeMillis();

        if(kp - lastKeyPress > 120) {
            String currentFilter = lend_filter.getText();
            loadListOfUsers(currentFilter);
        }
        lastKeyPress = kp;

    }

    public void checkToLend() {
        ObservableList<Integer> userToLend = lend_listView.getSelectionModel().getSelectedIndices();

        if(userToLend.size() != 1){
            c.showAlert(Alert.AlertType.ERROR,"Item lending Error","Must choose exactly one user");
            return;
        }

        LocalDateTime from = lend_from.getValue().atStartOfDay();
        LocalDateTime to = lend_to.getValue().atStartOfDay().isEqual(from) ? lend_to.getValue().plusDays(1).atStartOfDay() : lend_to.getValue().atStartOfDay();

        if(to.isBefore(from)){
            c.showAlert(Alert.AlertType.ERROR,"Date Error","Cannot return item before lending");
            return;
        }

        if(itemIsAvailable(from,to)){
            try {
                Table table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("lending");

                String fromFormmated = getDateFormat(from);
                String toFormmated = getDateFormat(to);

                table.addRow(null,selectedItemId,false,usersIdIndexInList.get(userToLend.get(0)),from.toString(),to.toString());
                c.showAlert(Alert.AlertType.INFORMATION,"Item Lending Success","Item " + itemDesc + " was lent to " + usersIdMap.get(usersIdIndexInList.get(userToLend.get(0))) + " from " + fromFormmated + " to " + toFormmated);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            c.showAlert(Alert.AlertType.ERROR,"Item Lending Error","Item is already booked for the following dates");
            return;
        }

        lendMenu.close();
        lendMenu = null;
    }

    public static boolean itemIsAvailable(LocalDateTime from, LocalDateTime to) {
        Table table;
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("lending");
            for(Row row : table) {
                if (Integer.parseInt(row.get("itemID").toString()) != selectedItemId) {
                    LocalDateTime startTime = strToDate(row.get("startTime").toString());
                    LocalDateTime returnTime = strToDate(row.get("returnTime").toString());

                    if((from.isBefore(returnTime) && (from.isAfter(startTime) || from.isEqual(startTime)))
                    ||  (from.isBefore(returnTime) && to.isAfter(startTime))
                    || (from.isBefore(startTime) && to.isAfter(returnTime))
                    || (from.isBefore(startTime) && (to.isAfter(returnTime) || to.isEqual(returnTime))))
                        return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
*/
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
