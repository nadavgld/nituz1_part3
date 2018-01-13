package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class packageCreateController {
    private Controller c = new Controller();
    private static int userID = Controller.userID;
    public static String packageToCreate = itemListController.packageToCreate;
    public static ArrayList<Integer> selectedIdexes = itemListController.selectedIdexes;
    private Model model = Main.model;

    @FXML
    private CheckBox cp_trade;
    @FXML
    private CheckBox cp_avail;

    public void createPackage(ActionEvent actionEvent) {
        Table table = null;
        int p_id;
        int summedPrice = calculateSummedPrice();

        try {
            model.addPackage("packages",userID,cp_avail,cp_trade,packageToCreate,summedPrice);

            p_id = model.getPackageIDbyOwner_Description(userID,packageToCreate);

            if(p_id != -1){
                for(int i: selectedIdexes){
                    insertItemToPackage(i,p_id);
                }
                c.showAlert(Alert.AlertType.INFORMATION,"Package Creation", "Package " + packageToCreate + " created successfully");

                Stage stage = (Stage) cp_trade.getScene().getWindow();
                stage.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int calculateSummedPrice() {
        int p = 0;

        try {
            p = model.getCalculatePackagePrice("items", selectedIdexes);
        }catch (Exception e){ }
        return p;
    }

    private void insertItemToPackage(int i, int p_id) {
        try {
            model.addItemToPackage(i,p_id,"itemInPackage");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
