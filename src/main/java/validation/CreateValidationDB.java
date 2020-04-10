package validation;

import db.Connect;
import db.CreateDb;
import db.CreateTables;
import simMatrix.SimMatrix;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateValidationDB {
    public static void main(String args[]){

        //creating objects
        CreateDb cdb = new CreateDb();
        String cwd = new File("").getAbsolutePath();
        String url = "jdbc:sqlite:"+cwd + "/sqlite/db/comp3208_test.db";
        CreateTables ct = new CreateTables(url);
        CreateValidationDB cvdb = new CreateValidationDB();

        //create DB and Tables
        cdb.createNewDatabase("comp3208_test.db");
        ct.createNewTable("newTrainingSet");
        ct.createNewTable("newTestingSet");
        ct.createSimTable();

        //populate tables
        cvdb.readRatings(url);
    }

    public void readRatings(String url){
        Connect c = new Connect();
        String cwd = new File("").getAbsolutePath();
        String csvFile = cwd + "/sqlite/dataset/comp3208-train-small.csv";
        String line;
        String training = "newTrainingSet";
        String testing = "newTestingSet";

        //input 1 in 10 into validation set
        int ratio = 10;
        int counter = 0;

        String sqlTrain = "INSERT INTO newTrainingSet (user_id,item_id,rating,time_stamp) VALUES(?,?,?,?)";
        String sqlTest = "INSERT INTO newTestingSet (user_id,item_id,rating,time_stamp) VALUES(?,?,?,?)";

        try{
            Connection conn = c.connect(url);
            conn.setAutoCommit(false);
            PreparedStatement pstmtTrain = conn.prepareStatement(sqlTrain);
            PreparedStatement pstmtTest = conn.prepareStatement(sqlTest);

            BufferedReader br = new BufferedReader(new FileReader(csvFile));

            while((line = br.readLine())!=null){
                String[] values = line.split(",");
                counter++;
                if(counter%ratio==0){
                    pstmtTest.setInt(1,Integer.valueOf(values[0]));
                    pstmtTest.setInt(2,Integer.valueOf(values[1]));
                    try {
                        pstmtTest.setFloat(3, Float.valueOf(values[2]));
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                    pstmtTest.setInt(4,Integer.valueOf(values[3]));
                    pstmtTest.addBatch();
                }
                else{
                    pstmtTrain.setInt(1,Integer.valueOf(values[0]));
                    pstmtTrain.setInt(2,Integer.valueOf(values[1]));
                    try {
                        pstmtTrain.setFloat(3, Float.valueOf(values[2]));
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                    pstmtTrain.setInt(4,Integer.valueOf(values[3]));
                    pstmtTrain.addBatch();
                }
            }
            //commit to db
            pstmtTest.executeBatch();
            pstmtTrain.executeBatch();
            conn.commit();

            //close connection
            conn.setAutoCommit(true);
            pstmtTest.close();
            pstmtTrain.close();
            conn.close();
            br.close();

        }catch (SQLException | FileNotFoundException e){
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
