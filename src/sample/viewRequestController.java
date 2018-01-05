package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;

public class viewRequestController {
    private Controller c = new Controller();
    private int userID = Controller.userID;
    private String typeOfUser = c.typeOfUser;

    private HashMap<Integer,Integer> ownerRequests_id_index;
    private int rowHeight = 60;
    @FXML
    private Accordion pv_accordion;

    public void initialize() {

        loadYourRequests();

        if(!typeOfUser.equals("Loaner"))
            loadRequestToApprove();

    }

    private void loadRequestToApprove() {
        ownerRequests_id_index = new HashMap<>();
        ListView lv = generateListView_r();
        lv.setPrefHeight(lv.getItems().size()*rowHeight);
        lv.setId("req_lv_app");
        TitledPane tp = new TitledPane("Requests Waiting for your action",lv);
        tp.setPrefHeight(lv.getPrefHeight());
        pv_accordion.getPanes().add(tp);
    }

    private void loadYourRequests() {
        ListView lv = generateListView_y();
        lv.setPrefHeight(lv.getItems().size()*rowHeight);
        lv.setId("req_lv_y");
        TitledPane tp = new TitledPane("Your Requests",lv);
        tp.setPrefHeight(lv.getPrefHeight());
        pv_accordion.getPanes().add(tp);
        pv_accordion.setExpandedPane(tp);
    }

    private ListView generateListView_r() {
        ListView lv = new ListView();
        Table table = null;

        int i=0;

        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("requests");

            for(Row row : table) {
                String status = row.get("status").toString();
                if (Integer.parseInt(row.get("to").toString()) == userID && status.equals("Pending")) {
                    boolean isPackage = row.get("isPackage").toString().toLowerCase().equals("true") ? true : false;
                    int itemID = Integer.parseInt(row.get("itemID").toString());
                    int reqID = Integer.parseInt(row.get("ID").toString());

                    Pair<String, String> itemDescription_Owner = loanListController.getDescriptionToItem(isPackage, itemID);
                    String tmura = row.get("tmura").toString().chars().allMatch(Character::isDigit) ? row.get("tmura").toString() + "$" : row.get("tmura").toString();

                    String loaner = userIDToUsername(Integer.parseInt(row.get("from").toString()));

                    String reqTime = updateItemController.getDateFormat(updateItemController.strToDate(row.get("dateOfRequest").toString()));
                    String startDate = updateItemController.getDateFormat(updateItemController.strToDate(row.get("startDate").toString()));
                    String returnDate = updateItemController.getDateFormat(updateItemController.strToDate(row.get("returnDate").toString()));


                    String s = listViewRow(itemDescription_Owner, loaner, tmura, startDate, reqTime, returnDate, status);

                    Label l = new Label();
                    l.setText(s);

                    Label approve = new Label();
                    approve.setText("V");
                    approve.getStyleClass().add("approve");
                    approve.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent t) {
                            actToRequest(true);
                        }
                    });

                    Label decline = new Label();
                    decline.setText("X");
                    decline.getStyleClass().add("decline");
                    decline.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent t) {
                            actToRequest(false);
                        }
                    });

                    HBox hb = new HBox();
                    hb.setSpacing(10);
                    hb.getChildren().addAll(l, approve, decline);

                    lv.getItems().add(i, hb);
                    ownerRequests_id_index.put(i, reqID);
                    i++;
                }
            }

            if(i==0){
                lv.getItems().add(i,"Could not find any relevant request..");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lv;

    }

    private void actToRequest(boolean approved) {
        ListView req_lv_app = (ListView) pv_accordion.getPanes().get(1).getContent();
        int req_idx = req_lv_app.getSelectionModel().getSelectedIndex();
        int reqID = ownerRequests_id_index.get(req_idx);

        Table table = null;
        String newStatus = approved ? "Approved" : "Declined";

        boolean changed = false;
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("requests");

            for (Row row : table) {
                if (Integer.parseInt(row.get("ID").toString()) == reqID) {
                    row.put("status", newStatus);
                    table.updateRow(row);

                    Table lend = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("lending");
                    lend.addRow(null,row.get("itemID").toString(),row.get("isPackage"),
                            Integer.parseInt(row.get("from").toString()),row.get("startDate").toString(),
                            row.get("returnDate").toString(),row.get("tmura").toString());

                    c.showAlert(Alert.AlertType.INFORMATION,"Everything4Rent",newStatus + " Successfully");
                    changed = true;
                    break;
                }
            }
        }catch (Exception e){}

        if(changed){
            pv_accordion.getPanes().remove(1);
            loadRequestToApprove();
        }
    }

    public static String userIDToUsername(int id) {
        Table table = null;
        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("users");

            for(Row row : table)
                if (Integer.parseInt(row.get("ID").toString()) == id) {
                    return row.get("Username").toString();
                }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ListView generateListView_y() {
        ListView lv = new ListView();
        Table table = null;

        int i=0;

        try {
            table = DatabaseBuilder.open(new File(Controller.dbPath)).getTable("requests");

            for(Row row : table)
                if (Integer.parseInt(row.get("from").toString()) == userID) {
                    boolean isPackage = row.get("isPackage").toString().toLowerCase().equals("true") ? true : false;
                    int itemID = Integer.parseInt(row.get("itemID").toString());

                    Pair<String, String> itemDescription_Owner = loanListController.getDescriptionToItem(isPackage, itemID);
                    String tmura = row.get("tmura").toString().chars().allMatch(Character::isDigit) ? row.get("tmura").toString()+"$" : row.get("tmura").toString();
                    String status = row.get("status").toString();

                    String reqTime = updateItemController.getDateFormat(updateItemController.strToDate(row.get("dateOfRequest").toString()));
                    String startDate = updateItemController.getDateFormat(updateItemController.strToDate(row.get("startDate").toString()));
                    String returnDate = updateItemController.getDateFormat(updateItemController.strToDate(row.get("returnDate").toString()));


                    lv.getItems().add(i,listViewRow(itemDescription_Owner,tmura,startDate,reqTime,returnDate,status));
                    i++;
                }

            if(i==0){
                lv.getItems().add(i,"Could not find any relevant request..");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lv;
    }

    private String listViewRow(Pair<String, String> itemDescription_owner, String tmura, String startDate, String reqTime, String returnDate, String status) {
        return "[" +reqTime + "] Status: " + status + " | " + itemDescription_owner.getKey() + " (Owner: " + itemDescription_owner.getValue() + "), " + startDate + "-" + returnDate + " for " + tmura;
    }

    private String listViewRow(Pair<String, String> itemDescription_owner,String loaner, String tmura, String startDate, String reqTime, String returnDate, String status) {
        return "[" +reqTime + "] Status: " + status + " | " + itemDescription_owner.getKey() + " (Loaner: " + loaner + "), " + startDate + "-" + returnDate + " for " + tmura;
    }

}
