package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * code adopted from hhtps://www.sqlitetutorial.net/sqlite-java
 */

public class Connect {
    public Connection connect(String url){
        try {
            Connection conn = DriverManager.getConnection(url);
            return conn;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
