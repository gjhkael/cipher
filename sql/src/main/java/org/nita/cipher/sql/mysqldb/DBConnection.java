package org.nita.cipher.sql.mysqldb;

import org.nita.cipher.sql.util.LoadDBConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guojianhua on 3/28/15.
 */
public class DBConnection {

    // database config
    private static LoadDBConfig dbConfig = new LoadDBConfig("/db.properties");

    private static Connection connection = null;

    static {
        try {
            Class.forName(dbConfig.getDriverClass());
            connection = DriverManager.getConnection(dbConfig.getURL(), dbConfig.getUserName(), dbConfig.getPassword());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }



    public static void main(String... args) {
        Connection conn = getConnection();
        try {
            Statement stmt = conn.createStatement();
            List<String> list = new ArrayList<String>();

            ResultSet rs = stmt.executeQuery("select name from name_label");
            while (rs.next()) {
                list.add(rs.getString("name"));
            }

            System.out.println(list.toString());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
