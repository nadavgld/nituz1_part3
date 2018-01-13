package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

public class searchController {
    private static Stage currentStage;
    private static int userID;
    private Controller c = new Controller();
    private static long lastKeyPress;
    private static Model model = Main.model;

    private static int selectedItemId;
    private static int selectedIdexnum;
    private static HashMap<Integer,Integer> itemMap;
    private static HashMap<Integer,Item> itemDescMap;

    private static Stage paymentStage;
    private static String paymentType;
    private static HashMap<Integer,Item> tradeItems;
    private static int itemInExchange;

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

    @FXML
    private Label item_desc;
    @FXML
    private Label item_price;
    @FXML
    private Label item_owner;
    @FXML
    private ToggleGroup loanType;

    @FXML
    private Label payment_type;
    @FXML
    private Pane free_pane;
    @FXML
    private Pane loan_pane;
    @FXML
    private Pane trade_pane;

    @FXML
    private DatePicker lend_from_free;
    @FXML
    private DatePicker lend_to_free;

    @FXML
    private DatePicker lend_from_loan;
    @FXML
    private DatePicker lend_to_loan;
    @FXML
    private Label loan_price;

    @FXML
    private DatePicker lend_from_trade;
    @FXML
    private DatePicker lend_to_trade;
    @FXML
    private ChoiceBox lend_itemList;
    @FXML
    private TextField lend_money;


    public void initialize() {
        currentStage = Controller.currentStage;
        userID = Controller.userID;

        if(search_list != null)
            search_list.setVisible(false);
        else if(item_desc != null){
            Item i = itemDescMap.get(selectedIdexnum);

            if(!i.isTradable)
                disableTrading();

            item_desc.setText(i.getDescription());

            if(i.getPrice().chars().allMatch(Character::isDigit))
                item_price.setText(i.getPrice() +"$");
            else
                item_price.setText(i.getPrice());

            item_owner.setText(i.ownerOfItem());

            if(paymentType != null)
                selectPayment();
        }else if(payment_type != null){
            itemInExchange = -1;

            if(lend_itemList != null)
                loadItemList();

            if(paymentType != null) {
                payment_type.setText(paymentType);
                addInfoToPane();
            }
        }
    }

    private void loadItemList() {
        tradeItems = new HashMap<>();
        lend_itemList.setTooltip(new Tooltip("+ for Item, * for Packages"));

        lend_itemList.setOnAction(event ->{
            itemInExchange = tradeItems.get(lend_itemList.getSelectionModel().getSelectedIndex()).getId();
            setPickerFromToday(lend_from_trade);
            setPickerFromToday(lend_to_trade);
        });

        int i=0;
        try {
            Table table = model.getDBtable("items");
            for(Row row: table){
                if(Integer.parseInt(row.get("userID").toString()) == userID){
                    Item item = itemRowToItem(row,false);

                    lend_itemList.getItems().add(i,"+ " + item.getDescription() + " (" + item.getCategory() +") - " + item.getPrice());
                    tradeItems.put(i,item);
                    i++;
                }
            }

            table = model.getDBtable("packages");
            for(Row row: table){
                if(Integer.parseInt(row.get("ownerID").toString()) == userID){
                    Item item = itemRowToItem(row,true);

                    lend_itemList.getItems().add(i,"* " + item.getDescription() + " (" + amountOfItemInPackage(item.getId()) +" items) - " + item.getPrice() +"$");
                    tradeItems.put(i,item);
                    i++;

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        lend_itemList.getSelectionModel().selectFirst();
        itemInExchange = tradeItems.get(0).getId();
    }

    //Searching
    public void search(KeyEvent keyEvent) {
        long kp = System.currentTimeMillis();

//        if(keyEvent == null || keyEvent.getCode() == KeyCode.ENTER){

        if(kp - lastKeyPress > 150) {

            String query = searchQuery.getText().toLowerCase().trim();

            if (query.length() == 0) {
                search_list.getItems().clear();
                search_list.getItems().add(0, "No item was found..");
                return;
            }


            searchItem(query.trim());
            search_list.setVisible(true);
            searchLog(query.trim());
        }
        lastKeyPress = kp;
//        }
    }

    private void searchLog(String query) {
        String timeStamp = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss").format(Calendar.getInstance().getTime());

        try {
            model.addSearchLog("userSearches",userID,query,timeStamp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchItem(String query) {
        boolean pack = search_cb_pack.isSelected();
        boolean toPrice = false;

        selectedItemId = -1;
        itemMap = new HashMap<>();
        itemDescMap = new HashMap<>();

        if(search_min_price.getText().length() > 0 || search_max_price.getText().length() > 0) {
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
        Table table;
        search_list.getItems().clear();
        try {
            table = model.getDBtable(tableToCheck);
            int i = 0;
            for(Row row : table) {
                if (rowContainsQuery(row,query, pack,toPrice)) {
                    String desc = row.get("Description").toString();
                    String price = row.get("Price").toString();
                    String cat = pack ? "" : row.get("Category").toString();
                    String avaiable = row.get("isAvailable").toString().toLowerCase().equals("true") ? "is available" : "is not available";
                    boolean b_a = avaiable.equals("is available") ? true : false;
                    String tradable = row.get("isTradable").toString().toLowerCase().equals("true") ? "is tradable" : "is not tradable";
                    boolean b_t = tradable.equals("is tradable") ? true : false;

                    String listRow = pack ? desc + " - " + price + "$ - " + avaiable + ", " + tradable : desc + " (" + cat + ") - " + price + " - " + avaiable + ", " + tradable;
                    search_list.getItems().add(i,listRow);

                    itemDescMap.put(i,new Item(Integer.parseInt(row.get("ID").toString()),desc,cat,b_a,b_t,price,tableToCheck));
                    itemMap.put(i,Integer.parseInt(row.get("ID").toString()));
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

    private boolean rowContainsQuery(Row row, String query, boolean pack, boolean price) {
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

        int itemPrice = pack ? Integer.parseInt(row.get("Price").toString()) : Integer.parseInt(row.get("Price").toString().substring(0,row.get("Price").toString().length()-1));
        int min = search_min_price.getText().length() == 0 ? 0 : Integer.parseInt(search_min_price.getText());
        int max = search_max_price.getText().length() == 0 ? Integer.MAX_VALUE : Integer.parseInt(search_max_price.getText());

        if(price){
            res = res && min <= itemPrice && max >= itemPrice;
        }
        return res;
    }

    public void btnSearch() {
        search(null);
    }

    public void handleItemSelecting(MouseEvent m) {
        if (m.getClickCount() == 2) {
            if(Controller.typeOfUser.equals("Owner")){
                c.showAlert(Alert.AlertType.ERROR, "Error", "An 'Owner' user type cannot loan other's items");
                return;
            }

            selectedIdexnum = search_list.getSelectionModel().getSelectedIndex();
            try {
                selectedItemId = itemMap.get(selectedIdexnum);
            } catch (Exception e) {
                return;
            }

            moveToStepInLoanProcess("itemChoose.fxml", itemDescMap.get(selectedIdexnum).getDescription() + " - CheckOut");
        }
    }

    //Checkout
    private void disableTrading() {
        for(Toggle t: loanType.getToggles()){
            RadioButton rb = (RadioButton)t;
            if(rb.getText().equals("Trade") && !itemDescMap.get(selectedIdexnum).isTradable) {
                rb.setDisable(true);
                break;
            }
        }
    }

    private void addInfoToPane() {
        //Loan Trade Free-Loan
        switch (paymentType){
            case "Loan":
                loan_pane.setVisible(true);
                loan_price.setText(loan_price.getText() + "" + itemDescMap.get(selectedIdexnum).getPrice());
                setPickerFromToday(lend_from_loan);
                setPickerFromToday(lend_to_loan);

                setReturnPlusOne(lend_from_loan,lend_to_loan);

                break;
            case "Trade":
                trade_pane.setVisible(true);
                setPickerFromToday(lend_from_trade);
                setPickerFromToday(lend_to_trade);
                setReturnPlusOne(lend_from_trade,lend_to_trade);

                break;
            case "Free-Loan":
                free_pane.setVisible(true);
                setPickerFromToday(lend_from_free);
                setPickerFromToday(lend_to_free);
                setReturnPlusOne(lend_from_free,lend_to_free);

                break;
        }
    }

    private void selectPayment() {
        for(Toggle t: loanType.getToggles()){
            RadioButton rb = (RadioButton)t;
            if(rb.getText().equals(paymentType)) {
                rb.setSelected(true);
                break;
            }
        }
    }

    public void continueButton(ActionEvent actionEvent) {
        if(Controller.username.toLowerCase().equals(item_owner.getText().toLowerCase())){
            c.showAlert(Alert.AlertType.ERROR, "Error", "You cannot loan your own items");
            return;
        }
        RadioButton rb = (RadioButton) loanType.getSelectedToggle();
        paymentType = rb.getText();

        moveToStepInLoanProcess("paymentChoose.fxml", itemDescMap.get(selectedIdexnum).getDescription() + " - Payment");

    }

    private void moveToStepInLoanProcess(String fxml, String title) {
        Parent root = null;
        try {
            if(paymentStage == null) {
                paymentStage = new Stage();
                paymentStage.initModality(Modality.WINDOW_MODAL);
                paymentStage.initOwner(currentStage);
                paymentStage.setResizable(false);
            }

            root = FXMLLoader.load(getClass().getResource(fxml));
            root.getStylesheets().add(getClass().getResource("style.css").toString());
            paymentStage.setTitle(title);
            paymentStage.setScene(new Scene(root, 400, 350));

            paymentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stepBackFromPayment(ActionEvent actionEvent) {
        moveToStepInLoanProcess("itemChoose.fxml", itemDescMap.get(selectedIdexnum).getDescription() + " - CheckOut");
    }

    public void checkOut(ActionEvent actionEvent) {
        boolean success = false;
        switch (paymentType){
            case "Loan":
                success = sendRequestToOwner(lend_from_loan,lend_to_loan,"Loan",itemDescMap.get(selectedIdexnum).price);
                break;
            case "Trade":
                int itemIdex = 0;
                int isPackage = -1;
                if(lend_itemList.isVisible()){
                    if(lend_itemList.getSelectionModel().getSelectedIndex() < 0){
                        c.showAlert(Alert.AlertType.ERROR,"Payment Error","If item-trade is checked, must choose an item");
                        return;
                    }else {
                        itemIdex = lend_itemList.getSelectionModel().getSelectedIndex();
                        isPackage = lend_itemList.getSelectionModel().getSelectedItem().toString().startsWith("+") ? 0 : 1;
                    }
                }

                int amount = 0;
                if(lend_money.isVisible()){
                    try {
                        amount = Integer.parseInt(lend_money.getText());
                    }catch (Exception e){
                        c.showAlert(Alert.AlertType.ERROR,"Payment Error","If extra-money is checked, must add a price");
                        return;
                    }
                }

                if(amount == 0 && itemIdex <= 0){
                    c.showAlert(Alert.AlertType.ERROR,"Payment Error","Must pick at least one option of exchange");
                    return;
                }


                success = sendRequestToOwner(lend_from_trade,lend_to_trade,"Trade",tradeItems.get(itemIdex).getId() +"_"+amount+"_"+isPackage);
                break;
            case "Free-Loan":
                success = sendRequestToOwner(lend_from_free,lend_to_free,"Giveaway","Free");
                break;
        }

        if(success){
            itemInExchange = -1;
            paymentType = null;

            Stage s = (Stage) payment_type.getScene().getWindow();
            s.close();
        }
    }

    private boolean sendRequestToOwner(DatePicker f, DatePicker t, String type, String tmura) {
        itemInExchange = -1;
        int extraMoney;
        int isPackage;

        if(type.equals("Trade")) {
            itemInExchange = Integer.parseInt(tmura.split("_")[0]);
            extraMoney = Integer.parseInt(tmura.split("_")[1]);
            isPackage = Integer.parseInt(tmura.split("_")[2]);

            tmura = itemInExchange > 0 && extraMoney > 0 ? tmura : itemInExchange <= 0 ? ""+extraMoney : itemInExchange+"_"+isPackage;
        }

        LocalDateTime from = null;
        LocalDateTime to = null;
        try {
            from = f.getValue().atStartOfDay();
            to = t.getValue().atStartOfDay().isEqual(from) ? t.getValue().plusDays(1).atStartOfDay() : t.getValue().atStartOfDay();
        }catch (Exception e){
            c.showAlert(Alert.AlertType.ERROR,"Date Error","Must pick Start and Return date");
            return false;
        }
        if(to.isBefore(from)){
            c.showAlert(Alert.AlertType.ERROR,"Date Error","Cannot return item before lending");
            return false;
        }

        if(type.equals("Trade") && !itemIsAvailable(itemInExchange,from,to)){
            c.showAlert(Alert.AlertType.ERROR,"Exchange Error","The item you chose is already booked in the dates");
            return false;
        }


        if(itemIsAvailable(selectedItemId,from,to)){
            try {
                model.addRequest("requests",from,to,itemDescMap.get(selectedIdexnum),type,tmura);

                Item i = itemDescMap.get(selectedIdexnum);

                String fromFormatted = updateItemController.getDateFormat(from);
                String toFormatted = updateItemController.getDateFormat(to);
                c.showAlert(Alert.AlertType.INFORMATION,"Request was sent successfully","A request for item " + i.getDescription() + " was sent to it's owner " + i.ownerOfItem() + ", from " + fromFormatted + " to " + toFormatted);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            c.showAlert(Alert.AlertType.ERROR,"Item Requesting Error","Item is already booked for the following dates");
            return false;
        }
        return true;
    }

    public void toggleMoneyField(ActionEvent actionEvent) {
        if(lend_money.isVisible())
            lend_money.setVisible(false);
        else
            lend_money.setVisible(true);
    }

    public void toggleItemList(ActionEvent actionEvent) {
        if(lend_itemList.isVisible())
            lend_itemList.setVisible(false);
        else
            lend_itemList.setVisible(true);
    }


    //Dates Validation
    private void setReturnPlusOne(DatePicker from, DatePicker to) {
        from.valueProperty().addListener((ov, oldValue, newValue) -> {
            to.setValue(newValue);
            to.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(newValue)  || dateAlreadyTaken(date.atStartOfDay()));
                }
            });
        });

        to.valueProperty().addListener((ov, oldValue, newValue) -> {
            try {
                if (newValue.isBefore(from.getValue())) {
                    to.setValue(oldValue);
                    c.showAlert(Alert.AlertType.ERROR, "Date Error", "Cannot return item before lending");
                }
            }catch (Exception e){}
        });
    }

    private void setPickerFromToday(DatePicker dp) {
        dp.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()) || dateAlreadyTaken(date.atStartOfDay()));
            }
        });
    }

    private boolean dateAlreadyTaken(LocalDateTime date) {
        if(paymentType.equals("Trade"))
            return !itemIsAvailable(selectedItemId,date.minusDays(1), date.plusDays(1)) || !itemIsAvailable(itemInExchange,date.minusDays(1), date.plusDays(1));

        return !itemIsAvailable(selectedItemId,date.minusDays(1), date.plusDays(1));
    }

    public static boolean itemIsAvailable(int id,LocalDateTime from, LocalDateTime to) {

        if(id == -1)
            return true;

        boolean isAvailable = false;
        HashSet<Integer> relevantIDs = checkIfItemInPackage(id);
        relevantIDs.add(id);


        try {
            isAvailable = model.isDateAvailable("lending",relevantIDs,from,to);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isAvailable;
    }

    private static HashSet<Integer> checkIfItemInPackage(int selectedItemId) {
        HashSet<Integer> hs = new HashSet<>();
        Table table;

        try {
            table = model.getDBtable("itemInPackage");
            for(Row row : table) {
                if ((Integer.parseInt(row.get("itemID").toString())) == selectedItemId) {
                    hs.add(Integer.parseInt(row.get("packageID").toString()));
                }
                if ((Integer.parseInt(row.get("packageID").toString())) == selectedItemId) {
                    hs.add(Integer.parseInt(row.get("itemID").toString()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hs;
    }

    public void backToHome() {
        try {
            c.switchScene("home.fxml","Everything4Rent", 700,450,"style.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tradeAmountValidation(KeyEvent keyEvent) {
        String field = lend_money.getText();
        if(!field.chars().allMatch(Character::isDigit)){
            c.showAlert(Alert.AlertType.ERROR,"Payment Error","Amount must be numbers only");
            lend_money.setText(field.substring(0,field.length()-1));
            lend_money.positionCaret(field.length()-1);
        }
    }


    //Item class
    private int amountOfItemInPackage(int packageID) {
        int counter = 0;

        try {
            counter = model.amountOfItemsInPackage("itemInPackage",packageID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return counter;
    }

    private Item itemRowToItem(Row row, boolean pack) {
        String desc = row.get("Description").toString();
        String price = row.get("Price").toString();
        String cat = pack ? "" : row.get("Category").toString();
        String avaiable = row.get("isAvailable").toString().toLowerCase().equals("true") ? "is available" : "is not available";
        boolean b_a = avaiable.equals("is available") ? true : false;
        String tradable = row.get("isTradable").toString().toLowerCase().equals("true") ? "is tradable" : "is not tradable";
        boolean b_t = tradable.equals("is tradable") ? true : false;
        String type = pack ? "packages" : "items";

        return new Item(Integer.parseInt(row.get("ID").toString()),desc,cat,b_a,b_t,price,type);
    }

    public class Item{
        private int id;
        private String description;
        private String category;
        private boolean isAvailable;
        private boolean isTradable;
        private String price;
        private boolean isPackage;

        public Item(int id, String description, String category, boolean isAvailable, boolean isTradable, String price, String type) {
            this.id = id;
            this.description = description;
            this.category = category;
            this.isAvailable = isAvailable;
            this.isTradable = isTradable;
            this.price = price;
            this.isPackage = type.equals("packages") ? true : false;
        }

        public String ownerOfItem(){
            String o = null;

            Table table = null;
            String tableToCheck = isPackage ? "packages" : "items";
            try {
                table = model.getDBtable(tableToCheck);
                for(Row row : table) {
                    if (Integer.parseInt(row.get("ID").toString()) == id) {
                        o = isPackage ? viewRequestController.userIDToUsername(Integer.parseInt(row.get("ownerID").toString()))
                                : viewRequestController.userIDToUsername(Integer.parseInt(row.get("userID").toString()));

                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return o;
        }

        public int ownerIDOfItem(){
            int o = -1;

            Table table = null;
            String tableToCheck = isPackage ? "packages" : "items";
            try {
                table = model.getDBtable(tableToCheck);
                for(Row row : table) {
                    if (Integer.parseInt(row.get("ID").toString()) == id) {
                        o = isPackage ? (Integer.parseInt(row.get("ownerID").toString()))
                                : (Integer.parseInt(row.get("userID").toString()));

                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return o;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String getCategory() {
            return category;
        }

        public boolean isAvailable() {
            return isAvailable;
        }

        public boolean isTradable() {
            return isTradable;
        }

        public String getPrice() {
            return price;
        }

        public boolean isPackage() {
            return isPackage;
        }
    }
}
