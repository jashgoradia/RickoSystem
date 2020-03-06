import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    private HashMap<Integer, HashMap<Integer,HashMap<Float,Integer>>>itemBased;
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
        String sql = "SELECT user_id, item_id, rating,time_stamp FROM "+trainingset_tablename;
        try(Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){

            //create prepared statement


            //set paramter value in SQL statement
            //pstmt.setString(1,trainingset_tablename);

            //store results of query


            itemBased = new HashMap<>();

            //loop through result set
            while (rs.next()) {
                //store result parameters to temp variables
                Integer user = rs.getInt(1);
                Integer item = rs.getInt(2);
                Float rating = rs.getFloat(3);
                Integer time_stamp = rs.getInt(4);

                HashMap<Integer, HashMap<Float,Integer>> itemRatings = itemBased.get(item);
                // check if this iem already exists. If not, create a new
                // HashMap for this item.
                if (itemRatings == null) {
                    itemRatings = new HashMap<>();
                    itemBased.put(item, itemRatings);
                }
                HashMap<Float,Integer> userRatings = itemRatings.get(user);
                userRatings = new HashMap<>();
                userRatings.put(rating,time_stamp);
                itemRatings.put(user, userRatings);
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

    public int itemCount() {
        String sql = "SELECT COUNT(DISTINCT item_id) FROM training_dataset";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
           return (rs.getInt(1));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public void simMatrix(){
        int[][] similarityMatrix = new int [itemCount()][itemCount()];


    }

    public float cosineSimilarity (int item1, int item2){
        HashSet<Integer> intersection = new HashSet<Integer>(itemBased.get(item1).keySet());
        intersection.retainAll(itemBased.get(item2).keySet());
        Iterator<Integer> it = intersection.iterator();
        while(it.hasNext()){
            float avg_rating = avgRating(it.next());

        }

        return 0;
    }

    public float avgRating(Integer userid){
        String sql = "SELECT AVG(ratings) FROM training_dataset WHERE user_id = userid";
        try(Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){

            return (rs.getFloat(1));

        }catch (SQLException e){
            System.out.println(e.getMessage());
            return 0.0f;
        }
    }

    public static void main (String[] args) {
        SimpleDB2 sdb = new SimpleDB2();
        System.out.println(sdb.avgRating(2943));
        //sdb.loadRatings();
        //System.out.println(sdb.itemCount());

    }
}
