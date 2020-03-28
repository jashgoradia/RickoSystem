package validation;

import simMatrix.SimMatrix;

import java.io.File;

public class NewSimMatrix {
    public static void main(String args[]){
        String cwd = new File("").getAbsolutePath();
        String url = "jdbc:sqlite:"+cwd + "/sqlite/db/comp3208_test.db";

        SimMatrix sm = new SimMatrix(url,"newTrainingSet");
        sm.loadRatings();
        sm.setSorted();
        sm.checkSize();
        sm.getAvgRating();
        sm.simMatrix();
    }
}
