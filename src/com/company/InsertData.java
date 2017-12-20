package com.company;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.*;

class InsertData {
    public void insert() {
        String fileName = ".\\ndb.accdb";

        try {
            //loading the driver class
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");

            //creating connection string
            String url = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=" + fileName;

            //establishing connection
            Connection con = DriverManager.getConnection(url);
            Statement st = con.createStatement();

            String name = "Smith";
            String pass = "25";
            int i = st.executeUpdate("INSERT INTO users(name,age) VALUES('"
                    + name + "','" + pass + "')");
            System.out.println("Row is added");

            if(con!=null){
                System.out.println("Connection Successful!");
                con.close();
            }

        } catch (Exception e) {
            System.out.println(e);
        }

//        try{
////            Connection
////                con = DriverManager
////                .getConnection(".\\ndb.accdb");
////            Class.forName("com.mysql.jdbc.Driver");
//
//            Class.forName("com.mysql.jdbc.Driver");
//            String url = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ="+fileName;
//            Connection con = DriverManager.getConnection(url,"","");
//             Statement st = con.createStatement();
//
//            String name = "Smith";
//            String pass = "25";
//            int i = st.executeUpdate("INSERT INTO users(name,age) VALUES('"
//                    + name + "','" + pass + "')");
//            System.out.println("Row is added");
//        } catch (Exception e) {
//            System.out.println(e);
//        }
    }
}