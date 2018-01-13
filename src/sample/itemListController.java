package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class itemListController {
    private static Stage currentStage;
    private static int userID;
    private Controller c = new Controller();
    private HashMap<Integer,Integer> itemMap;

    public static int selectedItemId;
    public static String packageToCreate;
    public ObservableList<Integer> selectedItems;
    public static ArrayList<Integer> selectedIdexes;
    private Model model = Main.model;


    @FXML
    private ListView<String> itemList_list;
    @FXML
    private TextField itemList_packageName;

    public void initialize() {
        itemList_list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        itemMap = new HashMap<>();
        currentStage = Controller.currentStage;
        userID = Controller.userID;

        Table table = null;
        try {
            table = model.getDBtable("items");
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
            }
        }
    }

    public void createNewPackage() {
        if(itemList_packageName.getText().trim().length() == 0){
            c.showAlert(Alert.AlertType.WARNING,"Package Creation Error", "Must choose package name");
            return;
        }

        selectedItems = itemList_list.getSelectionModel().getSelectedIndices();

        if(selectedItems.size() == 0){
            c.showAlert(Alert.AlertType.WARNING,"Package Creation Error", "Must choose at least one item");
            return;
        }

        selectedIdexes = new ArrayList<>();

        for (int i: selectedItems) {
            int r = itemMap.get(i);
            selectedIdexes.add(r);
        }

        String pName = itemList_packageName.getText().trim();

        Table table = null;
        try {
            table = model.getDBtable("packages");
            for(Row row : table) {
                if (Integer.parseInt(row.get("ownerID").toString()) == userID) {
                    if(row.get("Description").toString().equals(pName)){
                        c.showAlert(Alert.AlertType.WARNING,"Package Creation Error", "Cannot duplicate packages name");
                        return;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        packageToCreate = pName;

        Parent root = null;
        try {
            Stage s = new Stage();
            root = FXMLLoader.load(getClass().getResource("packageCreate.fxml"));
            root.getStylesheets().add(getClass().getResource("style.css").toString());
            s.setTitle("Create new package");
            s.setScene(new Scene(root, 400, 400));
            s.initModality(Modality.WINDOW_MODAL);
            s.initOwner(currentStage);
            s.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        itemList_packageName.setText("");
        itemList_list.getSelectionModel().clearSelection();

    }

    public void showPackages() {
        Parent root = null;
        try {
            Stage s = new Stage();
            root = FXMLLoader.load(getClass().getResource("packageView.fxml"));
            root.getStylesheets().add(getClass().getResource("style.css").toString());
            s.setTitle("Your Packages");
            s.setScene(new Scene(root, 600, 400));
            s.initModality(Modality.WINDOW_MODAL);
            s.initOwner(currentStage);
            s.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addItem(MouseEvent mouseEvent) {
        try {
            c.switchScene("addItem.fxml","Everything4Rent", 700,450,"style.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
