import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class writeToFile {

    static String object;
    static float PERCENT;

    public writeToFile(String input, String object, float PERCENT) throws IOException {

        this.object = object;
        this.PERCENT = PERCENT;

        BufferedWriter writer = new BufferedWriter(new FileWriter("csvFiles"+object+"/data" + object + "_" + Float.toString(Math.round(PERCENT*100)) + "_compare_latency.csv", true));
        writer.write(input);
        
        writer.close();

        }   

    public writeToFile(String input) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter("csvFiles"+object+"/data" + object + "_" + Float.toString(Math.round(PERCENT*100)) + "_compare_latency.csv", true));
        writer.write(input);
        
        writer.close();

        }      
}
