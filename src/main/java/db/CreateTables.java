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

    // SQLite connection string
    String cwd = new File("").getAbsolutePath();
    public String url = "jdbc:sqlite:"+cwd+"/sqlite/db/comp3208.db";
    /**
     * Create a new table in the test database
     *
     */
    public void createNewTable() {

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS training_dataset (\n"
                + "    id INTEGER PRIMARY KEY,\n"
                + "    user_id integer NOT NULL,\n"
                + "    item_id integer NOT NULL,\n"
                + "    rating real NOT NULL,\n"
                + "    time_stamp integer NOT NULL\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createSimTable(){

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS sim_matrix (\n"
                //+ "    id INTEGER PRIMARY KEY,\n"
                + "    item1 integer NOT NULL,\n"
                + "    item2 integer NOT NULL,\n"
                + "    sim real NOT NULL\n"
                //+ "    time_stamp integer NOT NULL\n"
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
        //createNewTable();
        CreateTables ct = new CreateTables();
        ct.createSimTable();
        //ct.createNewTable();
    }

}