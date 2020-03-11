import java.util.*;
import java.lang.*;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

public class Test {
    public HashMap<Integer, HashMap<Integer,Float>>itemBased = new HashMap<>();
    public HashMap<Integer,Float>avgRating = new HashMap<>();
    // function to sort hashmap by values

    /*public  HashMap<Integer, HashMap<Integer,Float>> sortByValue(HashMap<Integer, HashMap<Integer,Float>> hm)
    {



        Read more: https://javarevisited.blogspot.com/2017/09/java-8-sorting-hashmap-by-values-in.html#ixzz6GOhhEoc5
        // Create a list from elements of HashMap
        List<Map.Entry<Integer,HashMap<Integer,Float>>> list =
                new LinkedList<Map.Entry<Integer, HashMap<Integer, Float>>>(hm);

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<Integer,Float>>() {
            public int compare( ,
                               int o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }*/



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

    // Driver Code
    public static void main(String[] args)
    {

        Test t= new Test();
        t.setValue();
        t.sortList();
       /* HashMap<String, Integer> hm = new HashMap<String, Integer>();

        // enter data into hashmap
        hm.put("Math", 98);
        hm.put("Data Structure", 85);
        hm.put("Database", 91);
        hm.put("Java", 95);
        hm.put("Operating System", 79);
        hm.put("Networking", 80);
        //Map<String, Integer> hm1 = t.sortByValue(hm);

        // print the sorted hashmap
        for (Map.Entry<String, Integer> en : hm1.entrySet()) {
            System.out.println("Key = " + en.getKey() +
                    ", Value = " + en.getValue());
        }*/
    }

    public void sortList(){
        LinkedHashMap sorted = itemBased.entrySet().stream().sorted(comparingByValue(Comparator.comparingInt(HashMap::size))).collect( toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        List<HashMap<Integer,Float>> printing = new ArrayList<>(sorted.values());
        printing.get(0).entrySet().forEach(entry->{
            System.out.println(entry.getKey() + " " + entry.getValue());
        });
    }
}