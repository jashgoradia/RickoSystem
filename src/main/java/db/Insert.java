package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.*;

/**
 *
 * @author sqlitetutorial.net
 */
public class Insert {

    /**
     * Connect to the test.db database
     *
     * @return the Connection object
     */
    /*private Connection connect() {
        // SQLite connection string
        String cwd = new File("").getAbsolutePath();
        String url = "jdbc:sqlite:"+cwd+"/sqlite/db/comp3208.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }*/


    public void insert() throws IOException{

        String cwd = new File("").getAbsolutePath();
        String url = "jdbc:sqlite:"+cwd+"/sqlite/db/comp3208.db";
        String csvFile = cwd + "/sqlite/dataset/comp3208-train.csv";
        String line;

        String sql = "INSERT INTO training_dataset(user_id,item_id,rating,time_stamp) VALUES(?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            int counter = 0;
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] values = line.split(",");
                pstmt.setInt(1, Integer.parseInt(values[0]));
                pstmt.setInt(2, Integer.parseInt(values[1]));
                if(values[2].equals("rating")){
                    values[2] = String.valueOf(0);
                }
                else pstmt.setFloat(3, Float.parseFloat(values[2]));
                pstmt.setInt(4, Integer.parseInt(values[3]));
                counter++;
                pstmt.addBatch();
                if(counter%1000==0){
                    int[] count = pstmt.executeBatch();
                    conn.commit();
                }

            }

//            conn.commit();
            br.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException{

        Insert app = new Insert();
        app.insert();
    }

}