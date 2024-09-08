import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.sound.midi.SysexMessage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.api.java.typeutils.runtime.kryo.KryoSerializer;
import org.apache.flink.core.memory.DataInputDeserializer;
import org.apache.flink.core.memory.DataOutputSerializer;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.runtime.state.ArrayListSerializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.apache.logging.log4j.core.config.plugins.convert.TypeConverters.IntegerConverter;
import org.apache.logging.log4j.core.util.Integers;
import org.w3c.dom.TypeInfo;




/*
 * Emit using Collector each cluster and its points one by one
 * Aggergate the cluster by id
 * put them into the window
 */

public class ClusteringNode extends RichMapFunction<Tuple2<Tuple2<Integer, String>, Double[][]>, Tuple2<Integer, ArrayList<Tuple3<String, Cluster, AABB>>>> {


    final boolean DEBBUGGING = false;
    float EPSILON;
    int minPts;

    public ClusteringNode(float EPSILON, int minPts){
        this.EPSILON = EPSILON;
        this.minPts = minPts;
    }

    @Override
    public Tuple2<Integer, ArrayList<Tuple3<String, Cluster, AABB>>> map(Tuple2<Tuple2<Integer, String>, Double[][]> arg0) throws Exception {

        int ID = arg0.f0.f0;
        DBSCANClusterer clusterer = new DBSCANClusterer<>(EPSILON, minPts);
        new writeToFile(Float.toString(EPSILON)  + ",");
        new writeToFile(Integer.toString(minPts)  + ",");

        ArrayList<DoublePoint> dataDoublePoints = getDoublePoints(arg0.f1);

        ArrayList<Cluster> clusters = (ArrayList) clusterer.cluster(dataDoublePoints);

        System.out.println("number of outliers: " + arg0.f1.length);

        ArrayList<Tuple3<String, Cluster, AABB>> cluster_n_id = new ArrayList<>();
        Integer i = 0;
        int nClusterPts = 0;
        for(Cluster e : clusters){
            nClusterPts += e.getPoints().size();
            AABB aabb = getAABB(e);
            cluster_n_id.add(new Tuple3<>(getID(ID,i), e, aabb));
            i = i+1;
        }

        System.out.println("Number of cluster points: " + nClusterPts);
        System.out.println("n. of points in clusters / n. of outliers: " + (double) nClusterPts * 100 / (arg0.f1.length) + "%");
        new writeToFile(Double.toString((double) nClusterPts * 100 / arg0.f1.length)  + ",");

        if(DEBBUGGING){
           ColorClusters(clusters, arg0.f0.f1, arg0.f0.f0);
        }
        


        return new Tuple2<>(ID, cluster_n_id);
    }
    

    private AABB getAABB(Cluster e) {
        List<DoublePoint> points_in_cluster = e.getPoints();
        double min_x = 96;
        double max_x = 0;
        double min_y = 96;
        double max_y = 0;
        for(DoublePoint p : points_in_cluster){
            double x = p.getPoint() [0];
            double y = p.getPoint() [1];
            if(x < min_x){
                min_x = x;
            }
            if(x > max_x){
                max_x = x;
            }
            if(y < min_y){
                min_y = y;
            }
            if(y > max_y){
                max_y = y;
            }


        }


        min_x -= Math.min(min_x - 0, EPSILON);
        max_x += Math.min(50 - max_x, EPSILON);
        min_y -= Math.min(min_y - 0, EPSILON);
        max_y += Math.min(50 - max_y, EPSILON);
        

        return new AABB(min_x, max_x, min_y, max_y);
    }

    private String getID(int ID, int clusterID){
        return Integer.toString(ID) + "_" + Integer.toString(clusterID);

    }



    public void ColorClusters(ArrayList<Cluster> clusters, String imagePath, Integer ID){
        BufferedImage image = null;

        try{
            image = ImageIO.read(new File(imagePath));

        }catch(IOException e){
            System.out.println("failed to get image");
        }


        //convert image to RGB
        BufferedImage img = new BufferedImage(
            image.getWidth(), image.getHeight(),
            BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = img.createGraphics();


        g2d.drawImage(image, 0, 0, null);


        Color[] colors = new Color[]{   Color.BLUE, Color.BLACK, Color.CYAN, Color.GRAY, 
                                        Color.GREEN, Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE,
                                        Color.PINK, Color.RED, Color.WHITE, Color.YELLOW
                                    };
        for(int i = 0; i < clusters.size(); i++){
            Cluster cluster = clusters.get(i);


            for(int j = 0; j < cluster.getPoints().size(); j++){
                int x = (int) ((DoublePoint) cluster.getPoints().get(j)).getPoint() [0];
                int y = (int) ((DoublePoint) cluster.getPoints().get(j)).getPoint() [1];
               img.setRGB( x, y, colors[i % colors.length].getRGB());
            }
        }



        // Create a JFrame
        JFrame frame = new JFrame("Display Clusters" + ID);

        // Load the image
        ImageIcon icon = new ImageIcon(img);

        // Create a JLabel to display the image
        JLabel label = new JLabel(icon);

        // Add the JLabel to the JFrame
        frame.add(label);

        // Set JFrame properties
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(ID*100, 0);
        frame.pack(); // Adjusts the frame size to fit the image
        frame.setVisible(true);
    }

    public ArrayList<DoublePoint> getDoublePoints(Double[][] points){
        ArrayList<DoublePoint> doublePoints = new ArrayList<>();
        for(int i = 0; i < points.length; i++){
            doublePoints.add(new DoublePoint(new double[]{ points[i][0], points[i][1]}));
        }

        return doublePoints;
    }






}