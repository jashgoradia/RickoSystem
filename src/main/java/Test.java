import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Test{
    public static void main(String[] args) {
        try {
            String cwd = new File("").getAbsolutePath();
            String file = cwd + "/sqlite/dataset/simMatrix.csv";
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String lastline = "";
            while((line=br.readLine())!=null){
                //lastline = line;
                System.out.println(line);
            }

        } catch (Exception e){
            e.getStackTrace();
        }
    }
}