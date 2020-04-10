package validation;

import simMatrix.SimMatrix;

import java.io.File;
import java.time.Duration;
import java.time.LocalTime;

public class NewSimMatrix {
    public static void main(String args[]){
        String cwd = new File("").getAbsolutePath();
        String url = "jdbc:sqlite:"+cwd + "/sqlite/db/comp3208_small.db";

        SimMatrix sm = new SimMatrix(url,"training_dataset");
        sm.loadRatings();
        sm.setSorted();
        sm.checkSize();
        sm.getAvgRating();
        LocalTime start = LocalTime.now();
        System.out.println(start);
        sm.simMatrix();
        LocalTime end = LocalTime.now();
        System.out.println("Time elapsed: "+ Duration.between(start,end).toMinutes()+" minutes.");
    }
}
