import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args){
        List<Integer> num = new ArrayList<Integer>();
        for(int i = 1; i<=7; i++){
            num.add(i);
        }
        //{1,2,3,4,5,6,7}
        for(int i = 0; i< num.size();i++){
            for(int j = i+1; j<num.size();j++){
                System.out.println(num.get(i)+","+num.get(j));
            }
            System.out.println();
        }
    }
}
