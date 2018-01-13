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
    private static int userID = Controller.userID;
    private Model model = Main.model;

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

    }

    private ListView loadListView(int p) {
        ListView lv = new ListView();

        try {
            lv = model.loadListViewOfPackages("itemInPackage",p);
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

        lv.getItems().add(lv.getItems().size(),l);
        return lv;
    }

    private void removePackage(int p) throws IOException {
        model.removeItemFromPackage("itemInPackage",p);
        model.removePackage("packages",p);

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

    private HashMap<Integer,String> loadPackages() {
        HashMap<Integer,String> arr = new HashMap<>();

        Table table = null;
        try {
            table = model.getDBtable("packages");

            for(Row row : table)
                if (Integer.parseInt(row.get("ownerID").toString()) == userID)
                    arr.put(Integer.parseInt(row.get("ID").toString()),(row.get("Description").toString()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return arr;
    }
}
