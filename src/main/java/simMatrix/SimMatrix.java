package simMatrix;

import db.Connect;
import db.CreateTables;

import java.io.File;
import java.sql.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

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
public class SimMatrix {
    String url;
    String trainingset_tablename;
    public SimMatrix() {
        String cwd = new File("").getAbsolutePath();
        this.url = "jdbc:sqlite:" + cwd + "/sqlite/db/comp3208.db";
        this.trainingset_tablename = "training_dataset";
    }

    public SimMatrix(String url, String trainingset_tablename){
        this.url=url;
        this.trainingset_tablename=trainingset_tablename;
    }

    //object to establish connection to database
    Connect c = new Connect();
    /**
     * The data is stored in a HashMap, which allows fast access.
     */
    private HashMap<Integer, HashMap<Integer,Float>>itemBased;
    private HashMap<Integer,Float>avgRating = new HashMap<>();
    private LinkedHashMap<Integer,HashMap<Integer,Float>> sorted;

    /**
     * Open an existing database.
     */
    /*public Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(url);
            return conn;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }*/

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
        String sql = "SELECT user_id, item_id, rating FROM " + trainingset_tablename;
        try {
            Connection conn = c.connect(url);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

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
            conn.close();
            System.out.println("Loaded " + count + " ratings from " + itemBased.size() + " items.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setSorted(){
        sorted = itemBased.entrySet().stream().sorted(comparingByValue(Comparator.comparingInt(HashMap::size))).collect( toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    }

    public void getAvgRating(){
        System.out.println("Calculating average rating");
        String sql = "SELECT user_id, AVG(rating) FROM "+trainingset_tablename+" GROUP BY user_id";
        try(Connection conn = c.connect(url);
            Statement stmt = conn.createStatement();
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
            conn.close();
            stmt.close();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        System.out.println("Average rating done");
    }

    public void simMatrix(){
        System.out.println("Calculating sim matrix");
        CreateTables ct = new CreateTables(url);
        ct.createSimTable();
        List<Integer> items = new ArrayList<>(sorted.keySet());

        String sql = "INSERT INTO sim_matrix(item1,item2,sim) VALUES(?,?,?)";

        try {
            Connection conn = c.connect(url);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            conn.setAutoCommit(false);
            int counter = 0;
            //List<String[]> lines = new ArrayList<>();

            for (int i = 0; i < sorted.keySet().size()-1; i++) {
                for (int j = i + 1; j < sorted.keySet().size(); j++) {
                    Float res = cosineSimilarity(items.get(i), items.get(j));
                    //res = Math.round(res * 100.0) / 100.0;
                    if (res >= -1 && res <= 1 && res != 0) {

                        //lines.add(new String[]{String.valueOf(item1),String.valueOf(item2),String.valueOf(res)});
                        pstmt.setInt(1, items.get(i));
                        pstmt.setInt(2, items.get(j));
                        pstmt.setFloat(3, res);

                        pstmt.addBatch();

                        pstmt.setInt(1,items.get(j));
                        pstmt.setInt(2,items.get(i));
                        pstmt.setFloat(3,res);

                        pstmt.addBatch();

                        counter+=2;

                        if (counter == 100000) {
                            pstmt.executeBatch();
                            conn.commit();
                            counter = 0;
                            //System.out.println("Writing");
                        }
                    }
                    else{
                        continue;
                    }
                }
                System.out.println(i);
                /*if(sorted.containsKey(i)) {
                    sorted.remove(i);
                }*/
            }
            if(counter!=0){
                int[] count = pstmt.executeBatch();
                conn.commit();
            }
            conn.setAutoCommit(true);
            conn.close();
            pstmt.close();
        } catch(SQLException e){
            e.printStackTrace();
        }

    }

    public Float cosineSimilarity (int item1, int item2) {

        double numerator = 0.0;
        double denominator_left = 0.0;
        double denominator_right = 0.0;

        HashSet<Integer> user_set1 = new HashSet<Integer>(sorted.get(item1).keySet());
        //intersection.retainAll(itemBased.get(item2).keySet());

        double result = 0.0;
        for(int user: user_set1){
            if(sorted.get(item2).keySet().contains(user)) {
                //ratings.put(itemBased.get(item1).get(user),itemBased.get(item2).get(user));
                double avg_rating = avgRating.get(user);
                double item1_rating = sorted.get(item1).get(user);
                double item2_rating = sorted.get(item2).get(user);

                numerator += ((item1_rating - avg_rating) * (item2_rating - avg_rating));
                denominator_left += Math.pow((item1_rating - avg_rating), 2);
                denominator_right += Math.pow((item2_rating - avg_rating), 2);
            }
        }
        denominator_left = Math.sqrt(denominator_left);
        denominator_right = Math.sqrt(denominator_right);

        result = (numerator / (denominator_left * denominator_right));
        return (float)result;
    }

   /* public void addDb(List<String[]> lines){
        try{
            Connection conn = c.connect(url);
            String sql = "INSERT INTO sim_matrix(item1,item2,sim) VALUES(?,?,?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            conn.setAutoCommit(false);
            int cnt = 0;
            for (String[] line : lines){
                pstmt.setInt(1,Integer.valueOf(line[0]));
                pstmt.setInt(2,Integer.valueOf(line[1]));
                pstmt.setFloat(3, Float.valueOf(line[2]));

                pstmt.addBatch();
                if (cnt==1000){
                    pstmt.executeBatch();
                    conn.commit();
                    cnt=0;
                }
            }
            if(cnt!=0){
                pstmt.executeBatch();
                conn.commit();
            }
            conn.setAutoCommit(true);
            conn.close();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }*/

   public void itemIndex(){
       List<Integer> items = new ArrayList<>(itemBased.keySet());
       System.out.println("Index of 1242 "+ items.indexOf(1242));
       System.out.println("Index of 36601 "+ items.indexOf(36601));
   }

    public void checkSize() {
       System.out.println(itemBased.keySet().size()-sorted.keySet().size());
    }

    public static void main (String[] args) {
        SimMatrix sdb = new SimMatrix();
        sdb.loadRatings();
        sdb.setSorted();
        sdb.checkSize();
        sdb.getAvgRating();
        LocalTime start = LocalTime.now();
        System.out.println(start);
        sdb.simMatrix();
        LocalTime end = LocalTime.now();
        System.out.println("Time elapsed: "+Duration.between(start,end).toMinutes()+" minutes.");
        //sdb.itemIndex();

    }
}
