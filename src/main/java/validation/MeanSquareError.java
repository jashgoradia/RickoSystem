package validation;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import db.Connect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class MeanSquareError {
    String tableName;
    String url;
    String csvPath;
    TreeMap<Integer,TreeMap<Integer,Float>> data = new TreeMap<>();
    List<String[]> allPredictions = new ArrayList<>();
    public MeanSquareError(String tableName, String url, String csvPath){
        this.tableName=tableName;
        this.url = url;
        this.csvPath = csvPath;
    }
    public static void main(String args[]){
        String tableName = "testing_dataset";
        String cwd = new File("").getAbsolutePath();
        String url = "jdbc:sqlite:" + cwd + "/sqlite/db/comp3208_test.db";
        String csvPath = cwd+"/sqlite/dataset/benchmark.csv";
        MeanSquareError mse = new MeanSquareError(tableName,url,csvPath);

        mse.loadTestSet();
        mse.readCSV();
        System.out.println("Calculating MSE");
        mse.calculateMSE();
    }

    public void loadTestSet(){
        System.out.println("Loading data from table "+tableName);
        String sql = "SELECT user_id,item_id,rating FROM "+tableName;
        Connect c = new Connect();
        int count=0;

        try(Connection conn = c.connect(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
        ){
            while(rs.next()){
                int user = rs.getInt(1);
                int item = rs.getInt(2);
                float rating = rs.getFloat(3);

                TreeMap<Integer,Float> userRatings = data.get(user);
                if(userRatings==null){
                    userRatings = new TreeMap<>();
                    data.put(user,userRatings);
                }
                userRatings.put(item,rating);
                count++;
            }
            System.out.println("Loaded "+count+" ratings from "+data.size()+" users.");
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void readCSV() {
        try {
            FileReader fileReader = new FileReader(csvPath);
            CSVReader csvReader = new CSVReader(fileReader);
            allPredictions = csvReader.readAll();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void calculateMSE(){
        int accessed = 0;
        double mse =0;
        double mae = 0;
        int count=1;
        for(String[] row:allPredictions){

            //if(count>942575) {
                int user = Integer.valueOf(row[0]);
                int item = Integer.valueOf(row[1]);
                float prediction = Float.valueOf(row[2]);

                accessed++;
                float rating = data.get(user).get(item);
                mse += Math.pow((rating - prediction), 2);
                mae += Math.abs(rating - prediction);
            /*}
            else
                count++;*/
        }
        mse = mse/accessed;
        mae = mae/accessed;
        System.out.println("Rows accessed = " + accessed);
        System.out.println("Mean Square Error = "+mse);
        System.out.println("Mean Absolute Error = "+mae);
        System.out.println("Root Mean Square Error = "+Math.sqrt(mse));
    }
}
