package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sun.plugin.javascript.navig.Anchor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class packageViewController {
    private Controller c = new Controller();
    private static int userID = Controller.userID;

    @FXML
    private Accordion pv_accordion;

    public void initialize() {

        HashMap<Integer,String> packges = loadPackages();
        int rowHeight = 25;

        for(int p: packges.keySet()){
            ListView lv = loadListView(p);
            lv.setPrefHeight(lv.getItems().size()*rowHeight);
            TitledPane tp = new TitledPane("Package " + packges.get(p),lv);
            tp.setPrefHeight(lv.getPrefHeight()*1.2);
            pv_accordion.getPanes().add(tp);
        }
//        pv_accordion.setPrefHeight(maxInList*rowHeight*packges.size());

    }

    private ListView loadListView(int p) {
        ListView lv = new ListView();

        Table table = null;
        int i=0;
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("itemInPackage");

            for(Row row : table)
                if (Integer.parseInt(row.get("packageID").toString()) == p) {
                    int itemID = Integer.parseInt(row.get("itemID").toString());
                    lv.getItems().add(i,idToItem(itemID));
                    i++;
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Label l = new Label();
        l.setText("Remove Package");
        l.getStyleClass().add("packageRemoveLink");

        l.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                try {
                    removePackage(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        lv.getItems().add(i,l);
        return lv;
    }

    private void removePackage(int p) throws IOException {
        Table table;

        table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("itemInPackage");
        for(Row row: table){
            if(Integer.parseInt(row.get("packageID").toString()) == p)
                table.deleteRow(row);

        }

        table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("packages");
        for(Row row: table){
            if(Integer.parseInt(row.get("ID").toString()) == p) {
                table.deleteRow(row);
                break;
            }
        }
//
        pv_accordion.getPanes().removeAll();
        Stage st = (Stage)pv_accordion.getScene().getWindow();
        st.close();

        Stage s = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("packageView.fxml"));
        root.getStylesheets().add(getClass().getResource("style.css").toString());
        s.setTitle("Your Packages");
        s.setScene(new Scene(root, 600, 400));
        s.initModality(Modality.WINDOW_MODAL);
        s.initOwner(st);
        s.show();

    }

    private String idToItem(int itemID) {
        Table table = null;
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("items");

            for(Row row : table)
                if (Integer.parseInt(row.get("ID").toString()) == itemID) {
                    String desc = row.get("Description").toString();
                    String price = row.get("Price").toString();
                    String cat = row.get("Category").toString();
                    String avaiable = row.get("isAvailable").toString().toLowerCase().equals("true") ? "is available" : "is not available";
                    String tradable = row.get("isTradable").toString().toLowerCase().equals("true") ? "is tradable" : "is not tradable";

                    String listRow = desc + " (" + cat + ") - " + price + " - " + avaiable + ", " + tradable;

                    return listRow;
                }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private HashMap<Integer,String> loadPackages() {
        HashMap<Integer,String> arr = new HashMap<>();

        Table table = null;
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("packages");

            for(Row row : table)
                if (Integer.parseInt(row.get("ownerID").toString()) == userID)
                    arr.put(Integer.parseInt(row.get("ID").toString()),(row.get("description").toString()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return arr;
    }
}
