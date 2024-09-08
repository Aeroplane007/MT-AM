
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import com.esotericsoftware.kryo.io.Input;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class Moran implements MapFunction<Tuple2<Integer, String>, Tuple2<Tuple2<Integer, String>, Double[][]>> {


    float PERCENT;
    int NEIGBORHOOD_SIZE;
    String object;
    final static boolean DEBUGGING = false;
    static BufferedImage thermalImage;

    public Moran(int NEIGBORHOOD_SIZE, float PERCENT,String object){
        this.NEIGBORHOOD_SIZE = NEIGBORHOOD_SIZE;
        this.PERCENT = PERCENT;
        this.object = object;
    }

    private static void printer(String print){
        if(DEBUGGING){
            System.out.println(print);
        }
        
    }


    private static double[][] GetImageData(String image){
        double image_data[][];
        BufferedImage img;
        try{
            
            img = ImageIO.read(new File(image));
            
            thermalImage = img;
            image_data = new double[img.getHeight()][img.getWidth()];
            new writeToFile(Integer.toString(img.getHeight() * img.getWidth())  + ",");
            
            for(int i = 0; i < img.getHeight(); i++){
                for(int j = 0; j < img.getWidth(); j++){
                    
                    int color = img.getRGB(j, i);

                    //red
                    int red = (color>>16) & 0xFF;
                    //green
                    int green = (color>>8) & 0xFF;
                    //blue
                    int blue = (color>>0) & 0xFF;

                    //convert to grayscale using NTSC formula
                    image_data[i][j] = (0.299 * red + 0.587 * green + 0.114 * blue) / 255;

                }
            }



        }
        catch(IOException e){
            printer("Error loading image");
            image_data = new double[0][0];
        } 
        
        
        
        return image_data;
    }
    
    public static double[][] calculateZScore(double[][] image_data) {
        int width = image_data[0].length;
        int height = image_data.length;
        double[][] zScores = new double[height][width];

        // Calculate mean
        double sum = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sum += image_data[y][x];
            }
        }
        double mean = sum / (width * height);

        // Calculate standard deviation
        double sumSquaredDiff = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double pixelValue = image_data[y][x];
                sumSquaredDiff += Math.pow(pixelValue - mean, 2);
            }
        }
        double stdDev = Math.sqrt(sumSquaredDiff / (width * height));

        // Calculate z-scores
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double pixelValue = image_data[y][x];
                zScores[y][x] = (pixelValue - mean) / stdDev;
            }
        }

        return zScores;
    }
    private double[][] GetAvgZScore(double[][] z_scores, int s_neigh){
        int width = z_scores[0].length;
        int height =z_scores.length;

        double[][] avg_zScores = new double[height][width];
        //
        int count = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int count_neigh = 0;
                // Calculate the mean and standard deviation of all pixels
                for (int dy = -(s_neigh/2); dy <= (s_neigh/2); dy++) {
                    
                    for (int dx = -(s_neigh/2); dx <= (s_neigh/2); dx++) {
                        
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height && nx != x && ny != y) {
                            avg_zScores[y][x] += z_scores[ny][nx];
                            count_neigh++;
                        }
                    }
                }
                avg_zScores[y][x] = avg_zScores[y][x] / count_neigh;


            }

        }
        return avg_zScores;

    }


    private void ColorPixels(Double[][] pixels_to_color, BufferedImage image, String filename, Integer ID){

        //convert image to RGB
        BufferedImage img = new BufferedImage(
                image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
    
   
        Graphics2D g2d = img.createGraphics();
    

        g2d.drawImage(image, 0, 0, null);

        for(int i = 0; i < pixels_to_color.length; i++){
            img.setRGB( pixels_to_color[i][0].intValue(), pixels_to_color[i][1].intValue(), Color.RED.getRGB());

        }
        // Create a JFrame
        JFrame frame = new JFrame("Display Image " + ID);

        // Load the image
        ImageIcon icon = new ImageIcon(img);

        // Create a JLabel to display the image
        JLabel label = new JLabel(icon);

        // Add the JLabel to the JFrame
        frame.add(label);

        // Set JFrame properties
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack(); // Adjusts the frame size to fit the image
        frame.setVisible(true);
    }

    private Double[][] GetOutliers(double[][] dist, float percent_outliers){
        //copy dist with coordinates
        Double[][] dist_coor = new Double[dist.length * dist[0].length][3];

        for(int i = 0; i < dist.length; i++){
            for(int j = 0; j < dist[0].length; j++){
                dist_coor[j + (i * dist[0].length)] = new Double[]{ (double) j, (double) i, dist[i][j]};
            }
        }

        Arrays.sort( dist_coor, new Comparator<Double[]>() {
            @Override
            public int compare(Double[] o1, Double[] o2) {

                if(o1[2] < o2[2]){
                    return 1;
                }
                else if(o1[2] > o2[2]){
                    return -1;
                }
                return 0;
            }
        });

        printer(dist_coor[0][1] + " " + dist_coor[1][1] + " " + dist_coor[2][1]);

        Double[][] temp = new Double[(int) (dist_coor.length * percent_outliers)][3];

        for(int i = 0; i < (int) (dist_coor.length * percent_outliers); i++){
            temp[i] = dist_coor[i];
        }

        return temp;
    }

    private Double[][] MoranPlot(double avg_zscore[][], double [][] z_score, BufferedImage image, String filename, Integer ID){

        double[][] dist = new double[z_score.length][z_score[0].length];

        for(int i = 0; i < z_score.length; i++){
            for(int j = 0; j < z_score[0].length; j++){
                dist[i][j] = z_score[i][j] - avg_zscore[i][j];
                //dist[i][j] = Math.abs(z_score[i][j] - avg_zscore[i][j]);
            }
        }

        Double[][] outliers = GetOutliers(dist, PERCENT);
        if(DEBUGGING){
            ColorPixels(outliers, image , filename, ID);
        }
        

        return  outliers;
    }




    @Override
    public Tuple2<Tuple2<Integer, String>, Double[][]> map(Tuple2<Integer, String> arg0) throws Exception {
        System.out.println(arg0.f1);
        System.out.println("ID: " + arg0.f0);
        new writeToFile(arg0.f1  + ",");
        new writeToFile(Long.toString(System.nanoTime())  + ",");
        new writeToFile(Integer.toString(NEIGBORHOOD_SIZE)  + ",");
        new writeToFile(Float.toString(PERCENT)  + ",");

        String imagePath = "thermalImagesClaudia" + object + "/" + arg0.f1;

        double[][] image_data = GetImageData(imagePath);
        double[][] z_scores = calculateZScore(image_data);
        double[][] avg_z_scores = GetAvgZScore(z_scores, NEIGBORHOOD_SIZE);
        Double[][] outliers = MoranPlot(avg_z_scores, z_scores, thermalImage, "-", arg0.f0);

        return new Tuple2<>(new Tuple2<>(arg0.f0, imagePath), outliers);

        
    }
}