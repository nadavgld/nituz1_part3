package sample;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

public class Model {
    public String dbPath;
    public int userID;

    //Class Getters\Setters
    public void setDBPath(String path){
        dbPath = path;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }


    //General Functions
    public Table getDBtable(String table) throws IOException {
        return DatabaseBuilder.open(new File(dbPath)).getTable(table);
    }

    public boolean checkIfUsernameExists(String name, String tbl) throws IOException {
        Table table = getDBtable(tbl);
        for(Row row : table) {
            if (row.get("Username").toString().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public boolean checkIfMailExists(String mail, String tbl) throws IOException {
        Table table = getDBtable(tbl);
        for(Row row : table) {
            if (row.get("Email").toString().equals(mail)) {
                return true;
            }
        }

        return false;
    }

    public String getMailByUSERID(int userID, String tbl) throws IOException {
        Table table = getDBtable(tbl);

        for(Row row : table)
            if (Integer.parseInt(row.get("ID").toString()) == userID) {
                return row.get("Email").toString();
            }

        return null;
    }

    //AddItemController
    public void addItem(int userID, String desc, String price, Boolean available, String cat, Boolean trade, String loan) throws IOException {
        Table table = getDBtable("items");
        table.addRow(null,userID,desc,price+"$",available,cat,trade,loan);
    }


    //packageCreateController
    public void addPackage(String tbl, int userID, CheckBox cp_avail, CheckBox cp_trade, String packageToCreate, int summedPrice) throws IOException {
        Table table = getDBtable(tbl);
        table.addRow(null,userID,cp_avail.isSelected(), cp_trade.isSelected(), 0, packageToCreate, summedPrice);
    }

    public int getPackageIDbyOwner_Description(int userID, String packageToCreate) throws IOException {
        int p_id = -1;

        Table table = getDBtable("packages");
        for(Row row : table) {
            if (Integer.parseInt(row.get("ownerID").toString()) == userID && row.get("Description").toString().equals(packageToCreate)) {
                p_id = Integer.parseInt(row.get("ID").toString());
                break;
            }
        }

        return p_id;
    }

    public int getCalculatePackagePrice(String tbl, ArrayList<Integer> selectedIdexes) throws IOException {
        int p = 0;

        Table table = getDBtable(tbl);
        for (Row row : table) {
            if (selectedIdexes.contains(Integer.parseInt(row.get("ID").toString()))) {

                String price = row.get("Price").toString();
                p += Integer.parseInt(price.substring(0,price.length()-1));
            }
        }

        return p;
    }

    public void addItemToPackage(int i, int p_id, String tbl) throws IOException {
        Table table = getDBtable(tbl);
        table.addRow(null,p_id,i);
    }


    //packageViewController
    public ListView loadListViewOfPackages(String tbl, int p) throws IOException {
        ListView lv = new ListView();
        int i =0;

        Table table = getDBtable(tbl);

        for(Row row : table)
            if (Integer.parseInt(row.get("packageID").toString()) == p) {
                int itemID = Integer.parseInt(row.get("itemID").toString());
                lv.getItems().add(i,idToItem(itemID));
                i++;
            }

        return lv;
    }

    private String idToItem(int itemID) {
        Table table = null;
        try {
            table = getDBtable("items");

            for(Row row : table)
                if (Integer.parseInt(row.get("ID").toString()) == itemID) {
                    String desc = row.get("Description").toString();
                    String price = row.get("Price").toString();
                    String cat = row.get("Category").toString();
                    String avaiable = row.get("isAvailable").toString().toLowerCase().equals("true") ? "is Free-Loaned" : "is not Free-Loaned";
                    String tradable = row.get("isTradable").toString().toLowerCase().equals("true") ? "is tradable" : "is not tradable";

                    String listRow = desc + " (" + cat + ") - " + price + " - " + avaiable + ", " + tradable;

                    return listRow;
                }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public void removeItemFromPackage(String tbl, int p) throws IOException {
        Table table = getDBtable(tbl);
        for(Row row: table){
            if(Integer.parseInt(row.get("packageID").toString()) == p)
                table.deleteRow(row);
        }
    }

    public void removePackage(String tbl, int p) throws IOException {
        Table table = getDBtable(tbl);
        for(Row row: table){
            if(Integer.parseInt(row.get("ID").toString()) == p) {
                table.deleteRow(row);
                break;
            }
        }
    }


    //ProfileController
    public void updateUserInfo(String tbl, String name, String pass, String mail, String type, String address, String year, String gender, boolean isKosher) throws IOException {
        Table table = getDBtable(tbl);

        for(Row row : table) {
            if (Integer.parseInt(row.get("ID").toString()) == userID) {
                row.put("Username", name);
                row.put("Email", mail);
                row.put("Password", pass);
                row.put("userType", type);
                row.put("address", address);
                row.put("yearOfBirth", year);
                row.put("gender", gender);
                row.put("isKosher", isKosher);

                table.updateRow(row);
                break;
            }
        }
    }


    //updateItemController
    public void updateItemInfo(String tbl, String desc, String price, Boolean available, String cat, Boolean trade, int selectedItemId, String loan) throws IOException {
        Table table = getDBtable(tbl);
        int prevPrice = -1;

        for(Row row : table) {
            if (Integer.parseInt(row.get("ID").toString()) == selectedItemId) {
                prevPrice = Integer.parseInt(row.get("Price").toString().substring(0,row.get("Price").toString().length() -1));

                row.put("Description",desc);
                row.put("Price",price+"$");
                row.put("isAvailable",available);
                row.put("Category",cat);
                row.put("isTradable",trade);
                row.put("loanType",loan);

                table.updateRow(row);
                break;
            }
        }

        if(prevPrice != Integer.parseInt(price))
            updatePackagesCurrentPrice(Integer.parseInt(price) - prevPrice,selectedItemId);
    }

    private void updatePackagesCurrentPrice(int gap, int selectedItemId) throws IOException {
        Table table = getDBtable("itemInPackage");

        for(Row row : table) {
            if (Integer.parseInt(row.get("itemID").toString()) == selectedItemId) {
                int pckg = Integer.parseInt(row.get("packageID").toString());

                Table tablePackage = getDBtable("packages");
                for(Row r : tablePackage){
                    if(Integer.parseInt(r.get("ID").toString()) == pckg){

                        r.put("Price", Integer.parseInt(r.get("Price").toString()) + gap);
                        tablePackage.updateRow(r);
                        break;
                    }
                }

            }
        }

    }

    public void deleteItem(String tbl, int selectedItemId) throws IOException {
        Table table = getDBtable(tbl);

        for (Row row : table) {
            if (Integer.parseInt(row.get("ID").toString()) == selectedItemId) {
                int price = Integer.parseInt(row.get("Price").toString().substring(0, row.get("Price").toString().length() - 1));
                updatePackagesCurrentPrice(-price, selectedItemId);

                Table pck = getDBtable("itemInPackage");
                for (Row r : pck)
                    if (Integer.parseInt(r.get("itemID").toString()) == selectedItemId)
                        pck.deleteRow(r);

                table.deleteRow(row);
                break;
            }
        }

    }

    public boolean checkIfItemIsOrdered(String tbl, int selectedItemId) throws IOException {
        Table table = getDBtable(tbl);

        LocalDateTime now = LocalDateTime.now();
        for (Row row : table) {
            if(Integer.parseInt(row.get("itemID").toString()) == selectedItemId) {
                LocalDateTime startTime = updateItemController.strToDate(row.get("startTime").toString());
                LocalDateTime returnTime = updateItemController.strToDate(row.get("returnTime").toString());

                if(returnTime.isEqual(now) || returnTime.isAfter(now) || startTime.equals(now) || startTime.isAfter(now))
                    return true;
            }
        }

        return false;
    }

    //searchController
    public void addSearchLog(String tbl, int userID, String query, String timeStamp) throws IOException {
        Table table = getDBtable(tbl);
        table.addRow(null,userID,query,timeStamp);
    }

    public void addRequest(String tbl, LocalDateTime from, LocalDateTime to, searchController.Item i, String type, String tmura) throws IOException {
        Table table = getDBtable(tbl);
        table.addRow(null,userID,i.ownerIDOfItem(),type,i.isPackage(),i.getId(),"Pending",LocalDateTime.now(),from,to,tmura);
    }

    public boolean isDateAvailable(String tbl,HashSet<Integer> relevantIDs,LocalDateTime from, LocalDateTime to) throws IOException {
        Table table = getDBtable(tbl);

        for(Row row : table) {
            LocalDateTime startTime = updateItemController.strToDate(row.get("startTime").toString());
            LocalDateTime returnTime = updateItemController.strToDate(row.get("returnTime").toString());

            if (relevantIDs.contains(Integer.parseInt(row.get("itemID").toString())) && returnTime.isAfter(LocalDateTime.now().minusDays(1))) {

                if((from.isBefore(returnTime) && (from.isAfter(startTime) || from.isEqual(startTime)))
                        ||  (from.isBefore(returnTime) && to.isAfter(startTime))
                        || (from.isBefore(startTime) && to.isAfter(returnTime))
                        || (from.isBefore(startTime) && (to.isAfter(returnTime) || to.isEqual(returnTime))))
                    return false;
            }
        }

        return true;
    }

    public int amountOfItemsInPackage(String tbl, int packageID) throws IOException {
        int counter = 0;

        Table table = getDBtable(tbl);

        for(Row row: table){
            if(Integer.parseInt(row.get("packageID").toString()) == packageID){
                counter++;
            }
        }

        return counter;
    }


    //ViewRequestController
    public String getUserIDByUsername(int id, String tbl) throws IOException {
        Table table = getDBtable(tbl);

        for(Row row : table)
            if (Integer.parseInt(row.get("ID").toString()) == id) {
                return row.get("Username").toString();
            }

        return null;
    }

}
