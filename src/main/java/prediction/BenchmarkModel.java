package prediction;

import com.opencsv.CSVWriter;
import db.Connect;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByValue;

public class BenchmarkModel {
    String tableName;
    String url;
    String training_table;
    String csvPath;

    Map<Integer,List<Integer>> test_set = new TreeMap<>();
    Map<Integer,TreeMap<Integer,Float>> data = new TreeMap<>();
    HashMap<Integer,Float> avgItemRating = new HashMap<>();
    HashMap<Integer,Float> avgUserRating = new HashMap<>();
    Set<Integer> items = new HashSet<>();
    public BenchmarkModel(String url,String tableName,String training_table, String csvPath){
        this.tableName=tableName;
        this.url=url;
        this.training_table=training_table;
        this.csvPath = csvPath;
    }

    public void loadData() {
        System.out.println("Loading data from table " + tableName);
        int count = 0;
        String sql = "SELECT user_id, item_id FROM " + tableName;
        Connect c = new Connect();
        try {
            Connection conn = c.connect(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int user = rs.getInt(1);
                int item = rs.getInt(2);

                List<Integer> item_list = test_set.get(item);

                if (item_list == null) {
                    item_list = new ArrayList<>();
                    test_set.put(item, item_list);
                }
                item_list.add(user);
                count++;
            }
            conn.close();
            System.out.println("Loaded " + count + " ratings from " + test_set.size() + " items.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void loadTrainData(){
        System.out.println("Loading data from table "+ training_table);
        String sql = "SELECT user_id,item_id,rating FROM " + training_table+ " WHERE user_id in (SELECT DISTINCT user_id FROM "+tableName+")";

        Connect c = new Connect();
        int count=0;

        try{
            Connection conn = c.connect(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()){
                int user_id = rs.getInt(1);
                int item_id = rs.getInt(2);
                float rating = rs.getFloat(3);

                TreeMap<Integer,Float> userRatings = data.get(user_id);

                if(userRatings == null){
                    userRatings = new TreeMap<>();
                    data.put(user_id,userRatings);
                }

                userRatings.put(item_id,rating);
                count++;
            }
            conn.close();
            System.out.println("Loaded "+count+" ratings from " + data.size() + " users.");
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void itemList(){
        System.out.println("Storing list of all items that are in both sets");
        String sql = "SELECT DISTINCT item_id FROM "+training_table+" WHERE item_id IN (SELECT DISTINCT item_id from "+tableName+")";
        Connect c = new Connect();
        int count=0;
        try(Connection conn = c.connect(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
        ){
            while (rs.next()) {
                items.add(rs.getInt(1));
                count++;
            }
            conn.close();
            stmt.close();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        System.out.println("Loaded "+count+" items from training set");
    }

    public void loadAvgUserRating(){
        System.out.println("Calculating average user rating");
        String sql = "SELECT user_id, AVG(rating) FROM "+training_table+" WHERE user_id in (SELECT DISTINCT user_id FROM "+tableName+") GROUP BY user_id";
        Connect c = new Connect();
        int count = 0;
        try(Connection conn = c.connect(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){
            int flag = 0;
            while(rs.next()){
                avgUserRating.put(rs.getInt(1), rs.getFloat(2));
                count++;
            }
            conn.close();
            stmt.close();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        System.out.println("Loaded average ratings for "+count+" users.");
    }

    public void loadAvgItemRating(){
        System.out.println("Calculating average item rating");
        String sql = "SELECT item_id, AVG(rating) FROM "+training_table+" GROUP BY item_id";
        Connect c = new Connect();
        int count=0;
        try(Connection conn = c.connect(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){
            while(rs.next()){
                avgItemRating.put(rs.getInt(1), rs.getFloat(2));
                count++;
            }
            conn.close();
            stmt.close();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        System.out.println("Loaded average ratings for "+count+" items.");
    }

    public void predictionCalc(){
        LocalTime start = LocalTime.now();
        System.out.println("-----Predicting-----");
        List<String[]> preds = new ArrayList<>();
        int items_count = 0;
        int row_count = 0;
        int random_count = 0;
        int item_average_count = 0;
        int user_average_count = 0;
        int avg_count = 0;

        for(int item:test_set.keySet()){
            items_count++;
            for(int user:test_set.get(item)){
                float prediction;
                if(items.contains(item)==false&&data.containsKey(user)){
                    prediction = (avgUserRating.get(user));
                    user_average_count++;
                }
                else if(items.contains(item)==false && data.containsKey(user)==false){
                    prediction = random();
                    random_count++;
                }
                else{
                    prediction = (avgItemRating.get(item));
                    item_average_count++;
                }
                row_count++;
                preds.add(new String[]{String.valueOf(user), String.valueOf(item), String.valueOf(prediction)});
                if (preds.size() >= 10000) {
                    writeToCsv(preds);
                    preds.clear();
                }
            }
            System.out.println("Items: "+ items_count+ ", Row: "+row_count);
            if(items_count%1000==0){
                System.out.println("Time elapsed: "+ Duration.between(start,LocalTime.now()).toMinutes()+" minutes.");
            }
        }
        if(!preds.isEmpty()){
            writeToCsv(preds);
            preds.clear();
        }
        int coldstart= random_count+user_average_count+item_average_count;
        System.out.println("Total rows affected by cold start: " + coldstart);
        System.out.println("Total rows affected by not enough neighbours: "+ avg_count);
        System.out.println("Total random predictions (both user and item missing): " + random_count);
        System.out.println("Total average user rating predicted (only item missing): "+ user_average_count);
        System.out.println("Total average item rating predicted (only user missing): "+ item_average_count);
    }

    public float random(){
        Random r = new Random();
        return ((r.nextInt(9)/2)+0.5f);
    }


    public void writeToCsv(List<String[]> preds){
        File file = new File(csvPath);
        try{
            FileWriter outputfile = new FileWriter(file,true);
            CSVWriter writer = new CSVWriter(outputfile,',',CSVWriter.NO_QUOTE_CHARACTER,CSVWriter.DEFAULT_ESCAPE_CHARACTER,CSVWriter.DEFAULT_LINE_END);
            writer.writeAll(preds);

            writer.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public HashMap<Integer,Float> getNeighbourhood (int item,float threshold){
        //float threshold = 0f;
        String sql= "SELECT item2,sim FROM sim_matrix where item1 = ? and sim>" +threshold+";"; //TODO could add threshold as a parameter

        Connect c = new Connect();
        HashMap<Integer,Float> result = new HashMap<>();
        try {
            Connection conn = c.connect(url);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1,item);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int item2 = rs.getInt(1);
                float sim = rs.getFloat(2);

                result.put(item2,sim);
            }
            conn.close();
            pstmt.close();
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    public static void main(String args[]){
        String cwd = new File("").getAbsolutePath();
        String url = "jdbc:sqlite:" + cwd + "/sqlite/db/comp3208.db";
        String csvPath = cwd+"/sqlite/dataset/benchmark.csv";
        BenchmarkModel bm = new BenchmarkModel(url,"testing_dataset","training_dataset",csvPath);
        System.out.println(LocalTime.now());
        bm.loadData();
        bm.loadTrainData();
        bm.itemList();
        bm.loadAvgUserRating();
        bm.loadAvgItemRating();
        LocalTime start = LocalTime.now();
        System.out.println(start);
        /*int k_max = 50;
        int k_min = 5;
        float threshold = 0f;*/
        bm.predictionCalc();
        LocalTime end = LocalTime.now();
        System.out.println("Time elapsed: " + Duration.between(start,end).toMillis()+ " milliseconds.");
    }
}
