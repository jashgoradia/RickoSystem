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
    String url;
    String tableName;
    public Insert(String url, String tableName){
        this.url=url;
        this.tableName=tableName;
    }

    public Insert() {
        String cwd = new File("").getAbsolutePath();
        url = "jdbc:sqlite:" + cwd + "/sqlite/db/comp3208.db";
        tableName = "training_dataset";
    }

    public void insert(String csvFile) throws IOException{

        String line;
        Connect c = new Connect();

        String sql = "INSERT INTO "+tableName+" (user_id,item_id,rating,time_stamp) VALUES(?,?,?,?)";

        try {
            Connection conn = c.connect(url);
             PreparedStatement pstmt = conn.prepareStatement(sql);
            conn.setAutoCommit(false);
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            int counter = 0;
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] values = line.split(",");
                pstmt.setInt(1, Integer.parseInt(values[0]));
                pstmt.setInt(2, Integer.parseInt(values[1]));
                try {
                    pstmt.setFloat(3, Float.parseFloat(values[2]));
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
                pstmt.setInt(4, Integer.parseInt(values[3]));
                counter++;
                pstmt.addBatch();
               /* if(counter%1000==0){
                    int[] count = pstmt.executeBatch();
                    conn.commit();
                    counter = 0;
                }*/
            }
            /*if(counter!=0){
                int[] count = pstmt.executeBatch();
                conn.commit();
            }*/
            pstmt.executeBatch();
            conn.commit();
            br.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insert_test(String csvFile)throws IOException{
        String line;
        Connect c = new Connect();

        String sql = "INSERT INTO "+tableName+" (user_id,item_id,time_stamp) VALUES(?,?,?)";

        try {
            Connection conn = c.connect(url);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            conn.setAutoCommit(false);
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] values = line.split(",");
                pstmt.setInt(1, Integer.parseInt(values[0]));
                pstmt.setInt(2, Integer.parseInt(values[1]));
                pstmt.setInt(3, Integer.parseInt(values[2]));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
            br.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException{

        //training dataset
        String cwd = new File("").getAbsolutePath();
        String url = "jdbc:sqlite:" + cwd + "/sqlite/db/comp3208_small.db";

        String trainCsv = cwd + "/sqlite/dataset/comp3208-train-small.csv";
        Insert app = new Insert(url,"training_dataset");
        app.insert(trainCsv);

        //test dataset

        /*CreateTables ct = new CreateTables(url);
        ct.createTestTable(tableName);*/
        String testCsv = cwd + "/sqlite/dataset/comp3208-test-small.csv";
        Insert test_app = new Insert(url,"testing_dataset");
        test_app.insert_test(testCsv);
    }

}
