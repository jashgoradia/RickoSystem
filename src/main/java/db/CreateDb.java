package db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

/**
 *
 * @author sqlitetutorial.net
 */
public class CreateDb {

    /**
     * Connect to a sample database
     *
     * @param fileName the database file name
     */
    public static void createNewDatabase(String fileName) {
        Connect c = new Connect();

        String cwd = new File("").getAbsolutePath();
        String url = "jdbc:sqlite:"+cwd + "/sqlite/db/" + fileName;

        try (Connection conn = c.connect(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        createNewDatabase("comp3208.db");
        CreateTables ct = new CreateTables();
        ct.createNewTable("training_dataset");

        Insert in = new Insert();
        String cwd = new File("").getAbsolutePath();
        String csvFile = cwd + "/sqlite/dataset/comp3208-train.csv";
        in.insert(csvFile);
    }
}
