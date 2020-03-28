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
    Map<Integer,List<Integer>> sim = new TreeMap<>();
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

                List<Integer> item_list = sim.get(item);

                if (item_list == null) {
                    item_list = new ArrayList<>();
                    sim.put(item, item_list);
                }
                item_list.add(user);
                count++;
            }
            conn.close();
            System.out.println("Loaded " + count + " ratings from " + sim.size() + " items.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void predictionCalc(){
        System.out.println("-----Predicting-----");
        List<String[]> data = new ArrayList<String[]>();
        Set<Integer> items = sim.keySet();
        int item_count=1;
        int user_count=1;
        for (int item:items) {
            List<Integer> users = sim.get(item);
            for(int user:users) {
                HashMap<Integer,Float[]> neighbourhood = getNeighbourhood(user,item);
                if (neighbourhood.isEmpty()==false) {
                    Set<Integer> neighbours = neighbourhood.keySet();
                    float prediction = 0f;
                    for (int neighbour : neighbours) {
                        /*Float rating = getRating(user, neighbour);*/
                            float sim_score = neighbourhood.get(neighbour)[0];
                            float rating = neighbourhood.get(neighbour)[1];
                            prediction += ((sim_score * rating) / sim_score);
                    }
                    data.add(new String[] {String.valueOf(user),String.valueOf(item),String.valueOf(prediction)});
                    if(data.size()>=100000){
                        writeToCsv(data);
                        data.clear();
                    }
                }
                System.out.println("Item: "+item_count+ " , User: "+ user_count);
                user_count++;
            }
            item_count++;
        }
    }

    public void writeToCsv(List<String[]> data){
        System.out.println("-----Writing to CSV-----");
        String cwd = new File("").getAbsolutePath();
        String path = cwd+"/sqlite/dataset/prediction.csv";
        File file = new File(path);
        try{
            FileWriter outputfile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputfile);
            writer.writeAll(data);

            writer.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public HashMap<Integer,Float[]> getNeighbourhood (int user,int item){
        String sql= "SELECT t.item1,t.sim,"+training_table+".rating FROM\n"
                    +"(\n"
                        +"SELECT item1,sim\n"
                        +"FROM sim_matrix\n"
                        +"WHERE item2="+item+"\n"
                        +"UNION\n"
                        +"SELECT item2,sim\n"
                        +"FROM sim_matrix\n"
                        +"WHERE item1=47984"+item+"\n"
                    +")t\n"
                    +"INNER JOIN "+training_table+"\n"
                    +"ON t.item1="+training_table+".item_id\n"
                    +"WHERE user_id = "+user+" AND sim>0 ORDER BY t.sim DESC LIMIT 100;";

        Connect c = new Connect();
        HashMap<Integer,Float[]> result = new HashMap<>();
        try {
            Connection conn = c.connect(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            Float[] temp = new Float[2];
            while (rs.next()) {
                temp[0] = rs.getFloat(2);
                temp[1] = rs.getFloat(3);
                result.put(rs.getInt(1),temp);
            }
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
        p.predictionCalc();
    }
}
