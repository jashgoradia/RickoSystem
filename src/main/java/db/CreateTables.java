package db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author sqlitetutorial.net
 */
public class CreateTables {

    /**
     * Create a new table in the test database
     *
     */
    public static void createNewTable() {
        // SQLite connection string
        String cwd = new File("").getAbsolutePath();
        String url = "jdbc:sqlite:"+cwd+"/sqlite/db/comp3208.db";

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS training_dataset (\n"
                + "    id INTEGER PRIMARY KEY,\n"
                + "    user_id integer NOT NULL,\n"
                + "    item_id integer NOT NULL,\n"
                + "    rating real NOT NULL,\n"
                + "    time_stamp datetime NOT NULL\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        createNewTable();
    }

}