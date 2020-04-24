package prediction;

import db.Connect;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class test {
    public static void main(String args[]){
        TreeMap<Integer,TreeMap<Integer,Float>> data = new TreeMap<>();
        HashMap<Integer,Float> neighbourhood = new HashMap<>();
        for(int i = 1; i<=10;i++){
            TreeMap<Integer,Float> userRatings = new TreeMap<>();
            for(int j = 1; j<=5; j++){
                userRatings.put(j,new Random().nextFloat()*5);
            }
            data.put(i,userRatings);
        }
        for(int i=1;i<=20;i++){
            neighbourhood.put(i,-(new Random().nextFloat()*5));
        }

        LinkedHashMap<Integer,Float> sorted_neighbours = neighbourhood.entrySet()
            .stream()
            .filter(map->data.get(2).containsKey(map.getKey()))
            .sorted(Map.Entry.<Integer,Float>comparingByValue(Comparator.comparingDouble(Math::abs)).reversed())
            .limit(5)
            .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue,(e1,e2)->e1,LinkedHashMap::new));
        sorted_neighbours.forEach((K,V)->System.out.println(K+" , "+V));
    }
}
