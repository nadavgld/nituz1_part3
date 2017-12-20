package com.company;

import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.util.ImportUtil;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
	// write your code here
        System.out.println("hello");
        String fileName = ".\\ndb.mdb";

        Table table = null;
        try {
            table = DatabaseBuilder.open(new File(fileName)).getTable("users");
            for(Row row : table) {
//                if(row.get("ID").toString().equals("4")){
////                    table.deleteRow(row);
//                    row.put("Username","sus");
//                    row.put("Password","123");
//                    table.updateRow(row);
////                    table.addRow(new Row)
//                }
                System.out.println("Column 'ID' has value: " + row.get("ID"));
                System.out.println("Column 'Username' has value: " + row.get("Username"));
                System.out.println("Column 'Password' has value: " + row.get("Password"));
            }


//            table.addRow(null,"aaa","111");
        } catch (IOException e) {
            e.printStackTrace();
        }
//        new InsertData().insert();
    }
}
