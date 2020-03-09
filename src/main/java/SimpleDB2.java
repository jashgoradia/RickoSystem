import com.opencsv.CSVWriter;

import javax.swing.plaf.nimbus.State;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * A few simple database manipulations using the SQLite4Java wrapper for the
 * Recommender System coursework.
 *
 * It assumes that a database exists which is named "comp3208.db" and this
 * database is already loaded with all the training data set into a table called
 * "TRAININGSET", which is assumed to have the following columns: "UserID",
 * "ItemID" and "Rating" Note that SQL is largely case insensitive (except the
 * data itself if these are strings).
 *
 * @author Enrico Gerding
 *
 */
public class SimpleDB2 {
    String cwd = new File("").getAbsolutePath();
    final String url = "jdbc:sqlite:" + cwd + "/sqlite/db/comp3208.db";
    final String trainingset_tablename = "training_dataset";
    public Connection conn;
    //private int nItems = itemCount();
    /**
     * The data is stored in a HashMap, which allows fast access.
     */
    private HashMap<Integer, HashMap<Integer,Float>>itemBased;
    private HashMap<Integer,Float>avgRating = new HashMap<>();
    //private HashMap<Integer, Float>avgRating;

    /**
     * Open an existing database.
     */
    public SimpleDB2() {
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * <p>
     * The data is loaded into a HashMap where the key is the user, and the
     * value is another HashMap where the key is the item. This makes it very
     * fast to look up all the items belonging to a particular user. If you need
     * to look up items, this is the other way around. Note that you can also
     * use a TreeMap.
     */
    public void loadRatings() {
        System.out.println("Loading data from table " + trainingset_tablename);
        int count = 0;
        String sql = "SELECT user_id, item_id, rating FROM "+trainingset_tablename;
        try(Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){

            itemBased = new HashMap<>();

            //loop through result set
            while (rs.next()) {
                //store result parameters to temp variables
                Integer user = rs.getInt(1);
                Integer item = rs.getInt(2);
                Float rating = rs.getFloat(3);
                //Integer time_stamp = rs.getInt(4);

                HashMap<Integer, Float> itemRatings = itemBased.get(item);
                // check if this iem already exists. If not, create a new
                // HashMap for this item.
                if (itemRatings == null) {
                    itemRatings = new HashMap<>();
                    itemBased.put(item, itemRatings);
                }
                //HashMap<Float,Integer> userRatings = itemRatings.get(user);
                //userRatings = new HashMap<>();
                //userRatings.put(rating,time_stamp);
                itemRatings.put(user, rating);
                count++;
            }
            /* Don't think this is needed
                // don't forget to dispose any prepared statements
                stat.dispose();
            */
            System.out.println("Loaded " + count + " ratings from " + itemBased.size() + " items.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void getAvgRating(){
        System.out.println("Calculating average rating");
        String sql = "SELECT user_id, AVG(rating) FROM training_dataset GROUP BY user_id";
        try(Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){
            int count = 0;
            while(rs.next()){
                try {
                    avgRating.put(rs.getInt(1), rs.getFloat(2));
                } catch (NullPointerException e){
                    count++;
                    System.out.println(count);
                }
            }

        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        System.out.println("Average rating done");
    }

    public void simMatrix(){
        System.out.println("Calculating sim matrix");
        List<Integer> items = new ArrayList<>(itemBased.keySet());
        File file = new File(cwd+"/sqlite/dataset/simMatrix.csv");
        try {
            FileWriter outputfile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputfile);

            List<String[]> data = new ArrayList<>();

            for (int i = 0; i < items.size(); i++) {
                for (int j = i + 1; j < items.size(); j++) {
                    //System.out.println(cosineSimilarity(items.get(i),items.get(j)));
                    int item1 = items.get(i);
                    int item2 = items.get(j);
                    float res = cosineSimilarity(item1,item2);
                    data.add(new String[]{String.valueOf(item1),String.valueOf(item2),String.valueOf(res)});
                }
                writer.writeAll(data);
                System.out.println("Writing to file");
            }
        } catch(IOException e){
            e.printStackTrace();
        }

    }

    public float cosineSimilarity (int item1, int item2){

        float numerator = 0.0f;
        float denominator_left = 0.0f;
        float denominator_right = 0.0f;

        HashSet<Integer> intersection = new HashSet<Integer>(itemBased.get(item1).keySet());
        intersection.retainAll(itemBased.get(item2).keySet());

        if(intersection!=null) {

            for (int user : intersection) {
                //ratings.put(itemBased.get(item1).get(user),itemBased.get(item2).get(user));
                float avg_rating = avgRating.get(user);
                float item1_rating = itemBased.get(item1).get(user);
                float item2_rating = itemBased.get(item2).get(user);

                numerator += ((item1_rating - avg_rating) * (item2_rating - avg_rating));
                denominator_left += (float) Math.pow((item1_rating - avg_rating), 2);
                denominator_right += (float) Math.pow((item2_rating - avg_rating), 2);
            }
            denominator_left = (float) Math.sqrt(denominator_left);
            denominator_right = (float) Math.sqrt(denominator_right);

            return (numerator / (denominator_left * denominator_right));
        }

        else {
            return 0.0f;
        }
    }

    public static void main (String[] args) {
        SimpleDB2 sdb = new SimpleDB2();
        sdb.loadRatings();
        sdb.getAvgRating();
        sdb.simMatrix();

    }
}
