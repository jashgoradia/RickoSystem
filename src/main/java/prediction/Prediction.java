package prediction;

import com.opencsv.CSVWriter;
import db.Connect;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Prediction {
    String tableName;
    String url;
    String training_table;
    Map<Integer,List<Integer>> test_set = new TreeMap<>();
    LinkedHashMap<Integer,HashMap<Integer,Float>> data = new LinkedHashMap<>();
    HashMap<Integer,Float> avgItemRating = new HashMap<>();
    HashMap<Integer,Float> avgUserRating = new HashMap<>();
    Set<Integer> items = new HashSet<>();
    public Prediction(String url,String tableName,String training_table){
        this.tableName=tableName;
        this.url=url;
        this.training_table=training_table;
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
        //FIXME not sure whether I need all users and items from training set or just those that are also in test set
        String sql = "SELECT user_id,item_id,rating FROM " + training_table;

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

                HashMap<Integer,Float> userRatings = data.get(user_id);

                if(userRatings == null){
                    userRatings = new HashMap<>();
                    data.put(user_id,userRatings);
                }

                userRatings.put(item_id,rating);
                items.add(item_id);
                count++;
            }
            conn.close();
            System.out.println("Loaded "+count+" ratings from " + data.size() + " users.");
            System.out.println("Loaded "+items.size()+" items from "+training_table);
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void loadAvgUserRating(){
        System.out.println("Calculating average user rating");
        String sql = "SELECT user_id, AVG(rating) FROM "+training_table+" GROUP BY user_id";
        Connect c = new Connect();
        try(Connection conn = c.connect(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){
            int count = 0;
            while(rs.next()){
                try {
                    avgUserRating.put(rs.getInt(1), rs.getFloat(2));
                } catch (NullPointerException e){
                    count++;
                    System.out.println(count);
                }
            }
            conn.close();
            stmt.close();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        System.out.println("Average user rating done");
    }

    public void loadAvgItemRating(){
        System.out.println("Calculating average item rating");
        String sql = "SELECT item_id, AVG(rating) FROM "+training_table+" GROUP BY item_id";
        Connect c = new Connect();
        try(Connection conn = c.connect(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){
            int count = 0;
            while(rs.next()){
                try {
                    avgItemRating.put(rs.getInt(1), rs.getFloat(2));
                } catch (NullPointerException e){
                    count++;
                    System.out.println(count);
                }
            }
            conn.close();
            stmt.close();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        System.out.println("Average item rating done");
    }

    public void predictionCalc(){
        System.out.println("-----Predicting-----");
        List<String[]> preds = new ArrayList<>();
        int items_count = 0;
        int row_count = 0;

        for(int item:test_set.keySet()){
            HashMap<Integer,Float> neighbourhood = getNeighbourhood(item);
            items_count++;
            for(int user:test_set.get(item)){
                float prediction = 0f;
                //FIXME not sure if these checks are all valid
                if((items.contains(item)==false && data.containsKey(user)==false) || neighbourhood==null){
                    Random r = new Random();
                    prediction = (r.nextInt(10)+1)/2;
                }
                else if(items.contains(item)==false){
                    prediction = avgUserRating.get(user);
                }
                else if(data.containsKey(user)==false){
                    prediction = avgItemRating.get(item);
                }
                else {
                    Set<Integer> neighbours = data.get(user).keySet();
                    row_count++;
                    //TODO could add neighbour lower and upper limit
                    for (int neighbour : neighbours) {
                        //TODO separate the prediction calc to a separate method
                        if (neighbourhood.containsKey(neighbour)) {
                            float rating = data.get(user).get(neighbour);
                            float sim = neighbourhood.get(neighbour);
                            prediction += ((sim * rating) / sim); //FIXME this doesn't work because numerator and denominator need to be summationed separately
                        }
                    }
                }
                preds.add(new String[]{String.valueOf(user), String.valueOf(item), String.valueOf(prediction)});
                if (preds.size() >= 100000) { //TODO not sure if 100,000 writes are good
                    writeToCsv(preds);
                    preds.clear();
                }
                System.out.println("Items: "+ items_count+ ", Row: "+row_count);
            }
        }
    }


    public void writeToCsv(List<String[]> preds){
        System.out.println("-----Writing to CSV-----");
        String cwd = new File("").getAbsolutePath();
        String path = cwd+"/sqlite/dataset/prediction.csv";
        File file = new File(path);
        try{
            FileWriter outputfile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputfile,',',CSVWriter.NO_QUOTE_CHARACTER,CSVWriter.DEFAULT_ESCAPE_CHARACTER,CSVWriter.DEFAULT_LINE_END);
            writer.writeAll(preds);

            writer.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public HashMap<Integer,Float> getNeighbourhood (int item){
        //TODO could add threshold as a parameter
        String sql= "SELECT item2,sim FROM sim_matrix where item1 = ? and sim>0";

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
        Prediction p = new Prediction(url,"testing_dataset","training_dataset");

        p.loadData();
        p.loadTrainData();
        p.loadAvgUserRating();
        p.loadAvgItemRating();
        p.predictionCalc();
    }
}
