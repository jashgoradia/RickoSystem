package testSimilarity;

import db.CreateTables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class test {

    public HashMap<Integer, HashMap<Integer,Float>>itemBased = new HashMap<>();
    public HashMap<Integer,Float>avgRating = new HashMap<>();
    //private HashMap<Integer, Float>avgRating;


    public static void main(String[] args) {
        test t = new test();
        t.setValue();

        double result = t.cosineSimilarity(3,4);
        result = Math.round(result * 1000.0) / 1000.0;

        if (result == -0.816){
            System.out.println("success");

        }

        else {
            System.out.println("-0.816 vs " + result);
        }

    }

    public void setValue(){
        avgRating.put(1,5.67f);
        avgRating.put(2,4.75f);
        avgRating.put(3,5.4f);
        avgRating.put(4,5.2f);
        avgRating.put(5,4.6f);
        avgRating.put(6,5.8f);

        HashMap<Integer,Float>item1 = new HashMap<>();
        item1.put(1,8f);
        item1.put(2,2f);
        item1.put(3,5f);
        item1.put(4,7f);
        item1.put(5,1f);
        item1.put(6,8f);
        itemBased.put(1,item1);

        HashMap<Integer,Float>item2 = new HashMap<>();
        item2.put(3,4f);
        item2.put(4,1f);
        item2.put(5,7f);
        item2.put(6,3f);
        itemBased.put(2,item2);

        HashMap<Integer,Float>item3 = new HashMap<>();
        item3.put(2,5f);
        item3.put(3,7f);
        item3.put(4,7f);
        item3.put(5,4f);
        item3.put(6,8f);
        itemBased.put(3,item3);

        HashMap<Integer,Float>item4 = new HashMap<>();
        item4.put(1,2f);
        item4.put(2,7f);
        item4.put(3,4f);
        item4.put(4,3f);
        item4.put(5,6f);
        item4.put(6,3f);
        itemBased.put(4,item4);

        HashMap<Integer,Float>item5 = new HashMap<>();
        item5.put(1,7f);
        item5.put(2,5f);
        item5.put(3,7f);
        item5.put(4,8f);
        item5.put(5,5f);
        item5.put(6,7f);
        itemBased.put(5,item5);

    }


    public double cosineSimilarity (int item1, int item2) {

        double numerator = 0.0;
        double denominator_left = 0.0;
        double denominator_right = 0.0;

        HashSet<Integer> user_set1 = new HashSet<Integer>(itemBased.get(item1).keySet());
        //intersection.retainAll(itemBased.get(item2).keySet());

        Iterator<Integer> i = user_set1.iterator();
        while (i.hasNext()) {
            System.out.println(i.next());
        }

        double result = 0.0;
        for(int user: user_set1){
            if(itemBased.get(item2).keySet().contains(user)) {
                //ratings.put(itemBased.get(item1).get(user),itemBased.get(item2).get(user));
                double avg_rating = avgRating.get(user);
                double item1_rating = itemBased.get(item1).get(user);
                double item2_rating = itemBased.get(item2).get(user);

                numerator += ((item1_rating - avg_rating) * (item2_rating - avg_rating));
                denominator_left += Math.pow((item1_rating - avg_rating), 2);
                denominator_right += Math.pow((item2_rating - avg_rating), 2);
            }
        }
        denominator_left = Math.sqrt(denominator_left);
        denominator_right = Math.sqrt(denominator_right);

        result = (numerator / (denominator_left * denominator_right));
        return result;
    }

}