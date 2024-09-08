

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Double;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.StateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.typeutils.runtime.kryo.KryoSerializer;
import org.apache.flink.core.memory.DataInputDeserializer;
import org.apache.flink.core.memory.DataOutputSerializer;
import org.apache.flink.queryablestate.client.state.ImmutableMapState;
import org.apache.flink.runtime.state.ArrayListSerializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;




public class AddClusters extends RichMapFunction<Tuple2<Integer, ArrayList<Tuple3<String, Cluster, AABB>>>, String>{

    boolean LATENCY_TEST = false;

    private static int counter = 0;

    private static LinkedList<Tuple2<Integer, ArrayList<Tuple3<String, Cluster, AABB>>>> cross_layer_clusters = new LinkedList<>();
    private static ArrayList<Tuple2<Integer, ArrayList<Tuple3<String, Cluster, AABB>>>> clusters = new ArrayList<>();
    private static ArrayList<Tuple3<String, Cluster, AABB>> clustersThatHit = new ArrayList<>();
    private static ArrayList<Tuple3<String, Cluster, AABB>> clustersThatHasAlreadyHit = new ArrayList<>();
    private static Hashtable<String, ArrayList<String>> mergeMap1 = new Hashtable<>();
    private static MapState<String, ArrayList<String>> mergeMap = new MapStateImpl<>();
    private static Hashtable<String, AABB3D> summaries = new Hashtable<>();
    private static AABB3D maxAABB3D = new AABB3D(0, 0, 0, 0, 0, 0);
    private static AABB3D meanAABB3D = new AABB3D(0, 0, 0, 0, 0, 0);
    private static AABB3D minAABB3D = new AABB3D(10, 100, 10, 100, 10, 100);

    //True positive = n. of clusters that have a hit in them
    private static int truePositives = 0;
    //False positives = n. of clusters that dont have a hit
    private static int falsePositive = 0;
    //False negative is a hit that has no cluster around it
    private static int falseNegative = 0;
    //True negative if the number of points that dont belong to a cluster and dosent have a hit form claudia
    private static int trueNegative = 0;

    private static int nClusters = 0;

    private static int nOfIncHits = 0;
    private static double accuracy = 1;
    private float EPSILON = 0;
    private int IMG_SIZE = 0;
    private String object;

    public AddClusters(float EPSILON, int IMG_SIZE, String object){
        this.EPSILON = EPSILON;
        this.IMG_SIZE = IMG_SIZE;
        this.object = object;
    }

    private MapState<String, ArrayList<String>> mapState;



    @Override
    public String map(Tuple2<Integer, ArrayList<Tuple3<String, Cluster, AABB>>> arg0) throws Exception {

        cross_layer_clusters.add(new Tuple2<>(arg0.f0, arg0.f1));
        clusters.add(new Tuple2<>(arg0.f0, arg0.f1));

        if(!LATENCY_TEST){
            hasHitErik(arg0.f0);
            System.out.println("TP: " + truePositives + " FP: " + falsePositive);
            System.out.println("TN: " + trueNegative + " FN: " + falseNegative);
            System.out.println("Inc hits: " + nOfIncHits);
            System.out.println("Total clusters that hit: " + nClusters);
        }
        

        new writeToFile(Integer.toString(truePositives) + ",");
        new writeToFile(Integer.toString(falsePositive) + ",");
        new writeToFile(Integer.toString(trueNegative) + ",");
        new writeToFile(Integer.toString(falseNegative) + ",");
        new writeToFile(Integer.toString(nOfIncHits) + ",");
        //check accuracy
        System.out.println("Max Volume " + maxAABB3D.getVolume());
        System.out.println("Min Volume " + minAABB3D.getVolume());
        System.out.println("Mean Volume " + Double.toString(meanAABB3D.getVolume() / Math.pow(summaries.size(),3)));

        if(cross_layer_clusters.size() > 1){
            Tuple3<Integer, Integer, Double> similarity = CompareLayersAABBIntercept();

            new writeToFile(Long.toString(System.nanoTime()) + "\n");
        }
        else{
            new writeToFile("NaN,");
            new writeToFile("NaN,");
            new writeToFile(Long.toString(System.nanoTime()) + "\n");
        }


        


        return "wow";
    }




    private Tuple3<Integer, Integer, Double> CompareLayers() throws Exception{
        double counter = 0;
        double size = 96 * 96;
        Tuple2<Integer, ArrayList<Tuple3<String, Cluster, AABB>>> list1 = cross_layer_clusters.pollFirst();
        Tuple2<Integer, ArrayList<Tuple3<String, Cluster, AABB>>> list2 = cross_layer_clusters.peekFirst();
        int ID1 = list1.f0;
        int ID2 = list2.f0;

        HashSet<DoublePoint> hash_set = new HashSet<>();

        for(Tuple3<String, Cluster, AABB>  c : list1.f1){
            List<DoublePoint> points_in_cluster = c.f1.getPoints();
            
            
            for(DoublePoint p : points_in_cluster){
                hash_set.add(p);
            }
            
        }
        


        for(Tuple3<String, Cluster, AABB>   c : list2.f1){
            List<DoublePoint> points_in_cluster = c.f1.getPoints();
            for(DoublePoint p : points_in_cluster){
                if(hash_set.contains(p)){
                    counter++;
                }
            }
        }


        for(Tuple3<String, Cluster, AABB>  c2 : list2.f1){
            List<DoublePoint> points_in_cluster_2 = c2.f1.getPoints();
            for(Tuple3<String, Cluster, AABB>  c1 : list1.f1){
                boolean merge = false;
                List<DoublePoint> points_in_cluster_1 = c1.f1.getPoints();
                for(DoublePoint p2 : points_in_cluster_2){
                    for(DoublePoint p1 : points_in_cluster_1){
                        if(Math.sqrt(Math.pow((p1.getPoint()[0] - p2.getPoint()[0]), 2) + Math.pow((p1.getPoint()[1] - p2.getPoint()[1]),2) + 1) < EPSILON){
                            merge = true;
                        }
                    }
                }
                if(merge){
                    if(mergeMap.contains(c1.f0)){
                        mergeMap.get(c1.f0).add(c2.f0);
                        summaries.put(c1.f0, new AABB3D(Math.min(summaries.get(c1.f0).getmin_x(), Math.min(c1.f2.getmin_x(), c1.f2.getmin_x())), 
                                                        Math.max(summaries.get(c1.f0).getmax_x(), Math.max(c1.f2.getmax_x(), c1.f2.getmax_x())), 
                                                        Math.min(summaries.get(c1.f0).getmin_y(), Math.min(c1.f2.getmin_y(), c1.f2.getmin_y())), 
                                                        Math.max(summaries.get(c1.f0).getmax_y(), Math.max(c1.f2.getmax_y(), c1.f2.getmax_y())), 
                                                        Math.min(summaries.get(c1.f0).getmin_z(), Math.min(list1.f0-290, list2.f0-290)), 
                                                        Math.max(summaries.get(c1.f0).getmax_z(), Math.max(list1.f0-290, list2.f0-290))
                                                        ));
                        checkMaxMin(c1.f0);

                    }else{
                        mergeMap.put(c1.f0, new ArrayList<String>());
                        mergeMap.get(c1.f0).add(c1.f0);
                        mergeMap.get(c1.f0).add(c2.f0);
                        summaries.put(c1.f0, new AABB3D(Math.min(c1.f2.getmin_x(), c1.f2.getmin_x()), 
                                                        Math.max(c1.f2.getmax_x(), c1.f2.getmax_x()), 
                                                        Math.min(c1.f2.getmin_y(), c1.f2.getmin_y()), 
                                                        Math.max(c1.f2.getmax_y(), c1.f2.getmax_y()), 
                                                        Math.min(list1.f0-290, list2.f0-290), 
                                                        Math.max(list1.f0-290, list2.f0-290)
                                                        ));
                        checkMaxMin(c1.f0);
                    }
                }
            }
        }



        double similarity = (counter * 100) / size;
        new writeToFile(Double.toString(similarity)  + ",");
        update();

        return new Tuple3<>(ID1, ID2, (Double) counter);

    }



    private Tuple3<Integer, Integer, Double> CompareLayersAABBIntercept() throws Exception{
        double counter = 0;
        double size = 96 * 96;
        Tuple2<Integer, ArrayList<Tuple3<String, Cluster, AABB>>> list1 = cross_layer_clusters.pollFirst();
        Tuple2<Integer, ArrayList<Tuple3<String, Cluster, AABB>>> list2 = cross_layer_clusters.peekFirst();
        int ID1 = list1.f0;
        int ID2 = list2.f0;




        for(Tuple3<String, Cluster, AABB>  c2 : list2.f1){
            for(Tuple3<String, Cluster, AABB>  c1 : list1.f1){
                boolean merge = false;
                if(AABBOverlap(c1.f2, c2.f2)){
                    List<DoublePoint> points_in_cluster_2 = c2.f1.getPoints();
                    List<DoublePoint> points_in_cluster_1 = c1.f1.getPoints();
                    for(DoublePoint p2 : points_in_cluster_2){
                        for(DoublePoint p1 : points_in_cluster_1){
                            if(Math.sqrt(Math.pow((p1.getPoint()[0] - p2.getPoint()[0]), 2) + Math.pow((p1.getPoint()[1] - p2.getPoint()[1]),2) + 1) < EPSILON){
                                merge = true;
                            }
                        }
                    }
                    if(merge){
                        if(mergeMap.contains(c1.f0)){
                            mergeMap.get(c1.f0).add(c2.f0);
                            summaries.put(c1.f0, new AABB3D(Math.min(summaries.get(c1.f0).getmin_x(), Math.min(c1.f2.getmin_x(), c2.f2.getmin_x())), 
                                                            Math.max(summaries.get(c1.f0).getmax_x(), Math.max(c1.f2.getmax_x(), c2.f2.getmax_x())), 
                                                            Math.min(summaries.get(c1.f0).getmin_y(), Math.min(c1.f2.getmin_y(), c2.f2.getmin_y())), 
                                                            Math.max(summaries.get(c1.f0).getmax_y(), Math.max(c1.f2.getmax_y(), c2.f2.getmax_y())), 
                                                            Math.min(summaries.get(c1.f0).getmin_z(), Math.min(list1.f0-290, list2.f0-290)), 
                                                            Math.max(summaries.get(c1.f0).getmax_z(), Math.max(list1.f0-290, list2.f0-290))
                                                            ));
                            checkMaxMin(c1.f0);

                        }else{
                            mergeMap.put(c1.f0, new ArrayList<String>());
                            mergeMap.get(c1.f0).add(c1.f0);
                            mergeMap.get(c1.f0).add(c2.f0);
                            summaries.put(c1.f0, new AABB3D(Math.min(c1.f2.getmin_x(), c2.f2.getmin_x()), 
                                                            Math.max(c1.f2.getmax_x(), c2.f2.getmax_x()), 
                                                            Math.min(c1.f2.getmin_y(), c2.f2.getmin_y()), 
                                                            Math.max(c1.f2.getmax_y(), c2.f2.getmax_y()), 
                                                            Math.min(list1.f0-290, list2.f0-290), 
                                                            Math.max(list1.f0-290, list2.f0-290)
                                                            ));
                            checkMaxMin(c1.f0);
                        }
                    }
                }
            }
        }


        //printSummaries();

        double similarity = (counter * 100) / size;
        new writeToFile(Double.toString(similarity)  + ",");
        update();
        calcMean();

        return new Tuple3<>(ID1, ID2, (Double) counter);

    }



    private void checkMaxMin(String f0) {
        double diffX = meanAABB3D.getmax_x() - meanAABB3D.getmin_x();
        double diffY = maxAABB3D.getmax_y() - maxAABB3D.getmin_y();
        double diffZ = maxAABB3D.getmax_z() - maxAABB3D.getmin_z();

        if(summaries.get(f0).getVolume() > maxAABB3D.getVolume()){
            maxAABB3D = summaries.get(f0);
        }
        if(summaries.get(f0).getVolume() < minAABB3D.getVolume()){
            minAABB3D = summaries.get(f0);
        }
    }


    private void calcMean(){
        Enumeration<String> e = summaries.keys();
        meanAABB3D = new AABB3D(0, 0, 0, 0, 0, 0);
        double aggMinX = 0;
        double aggMaxX = 0;
        double aggMinY = 0;
        double aggMaxY = 0;
        double aggMinZ = 0;
        double aggMaxZ = 0;
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            aggMinX += summaries.get(key).getmin_x();
            aggMaxX += summaries.get(key).getmax_x();
            aggMinY += summaries.get(key).getmin_y();
            aggMaxY += summaries.get(key).getmax_y();
            aggMinZ += summaries.get(key).getmin_z();
            aggMaxZ += summaries.get(key).getmax_z();
        }

        meanAABB3D = new AABB3D(aggMinX, aggMaxX, 
                                aggMinY, aggMaxY,
                                aggMinZ, aggMaxZ
                                );
    }


    private boolean AABBOverlap(AABB c1, AABB c2) {
        if(c1.getmax_x() >= c2.getmin_x() && c1.getmin_x() <= c2.getmax_x()){
            if(c1.getmax_y() >= c2.getmin_y() && c1.getmin_y() <= c2.getmax_y()){
                return true;
            }
        }


        if(c2.getmax_x() >= c1.getmin_x() && c2.getmin_x() <= c1.getmax_x()){
            if(c2.getmax_y() >= c2.getmin_y() && c2.getmin_y() <= c2.getmax_y()){
                return true;
            }
        }

        return false;
    }


    private void printSummaries(){
        Enumeration<String> e = summaries.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            AABB3D values = summaries.get(key);


            System.out.println( "min x: " + values.getmin_x() + "max x: " + values.getmax_x() +
                                "min y: " + values.getmin_y() + "max x: " + values.getmax_y() +
                                "min z: " + values.getmin_z() + "max z: " + values.getmax_z()
                                );

        }
    }

   /*private void update1() throws IOException {

        Enumeration<String> e = mergeMap.keys();
        int count = 0;
        double sum = 0;
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            ArrayList<String> values = mergeMap.get(key);
            AABB3D summary = summaries.get(key);
            String newKey = mergeMap.get(key).get(mergeMap.get(key).size()-1);
            mergeMap.remove(key);
            mergeMap.put(newKey, values);
            summaries.remove(key);
            summaries.put(newKey, summary);
            sum += values.size();
            count++;

           /* System.out.print("Key: " + newKey + " Clusters to add: ");
            for(String s: values){
                System.out.print(s + " : ");
            }
            System.out.println();*/

           /*  for(Tuple3<String, Cluster, AABB> s : clustersThatHit){
                if (values.contains(s.f0) && !clustersThatHasAlreadyHit.contains(s)){
                    System.out.println(key + " contains value");
                    nOfIncHits += 1;
                    clustersThatHasAlreadyHit.add(s);
                }
            }
        }


        if(count > 0){
            System.out.println("AVG height: " + sum/count);
            double AVGclusterheight = sum/(double) count;
            new writeToFile(Double.toString(AVGclusterheight)+ ",");
        }else{
            count = 1;
            System.out.println("AVG height: " + sum/count);
            double AVGclusterheight = sum/(double) count;
            new writeToFile(Double.toString(AVGclusterheight)+ ",");
        }
        
    }*/

    private void update() throws Exception {

        Iterable<String> keys = mergeMap.keys();
        int count = 0;
        double sum = 0;
        for(String key : keys){
            ArrayList<String> values = mergeMap.get(key);
            AABB3D summary = summaries.get(key);
            String newKey = mergeMap.get(key).get(mergeMap.get(key).size()-1);
            mergeMap.remove(key);
            mergeMap.put(newKey, values);
            summaries.remove(key);
            summaries.put(newKey, summary);
            sum += values.size();
            count++;

           /* System.out.print("Key: " + newKey + " Clusters to add: ");
            for(String s: values){
                System.out.print(s + " : ");
            }
            System.out.println();*/

            for(Tuple3<String, Cluster, AABB> s : clustersThatHit){
                if (values.contains(s.f0) && !clustersThatHasAlreadyHit.contains(s)){
                    System.out.println(key + " contains value");
                    nOfIncHits += 1;
                    clustersThatHasAlreadyHit.add(s);
                }
            }
        }


        if(count > 0){
            System.out.println("AVG height: " + sum/count);
            double AVGclusterheight = sum/(double) count;
            new writeToFile(Double.toString(AVGclusterheight)+ ",");
        }else{
            count = 1;
            System.out.println("AVG height: " + sum/count);
            double AVGclusterheight = sum/(double) count;
            new writeToFile(Double.toString(AVGclusterheight)+ ",");
        }
        
    }

    public void hasHit(Integer LayerID){
        String csvFile;
        if(object.equals("C4") || object.equals("A11")){
            csvFile = "csvFiles/B03_A11.csv";
        }else{
            csvFile = "csvFiles/B03_B10.csv";
        }
        String line = "";
        String csvSplitBy = ",";

        //save for points
        for(Tuple3<String, Cluster, AABB> e : clusters.get(LayerID-290).f1){
            falsePositive += 1;//e.f1.getPoints().size();
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            double layer = (double) LayerID;

            while ((line = br.readLine()) != null) {
                // Split the line by commas
                String[] data = line.split(csvSplitBy);


                double fourthColumn = Math.round(Double.parseDouble(data[2]) / 0.12) - 1;
                     

                if(layer == fourthColumn && !object.equals("C4")){
                    double x = Double.parseDouble(data[0]) / 0.125;
                    double y = Double.parseDouble(data[1]) / 0.125;
                        
                    if(!checkIfPtInCluster(x, y, LayerID)){
                        falseNegative += 1;
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        trueNegative = IMG_SIZE * (LayerID-289) - truePositives - falsePositive - falseNegative;

    }




    private boolean checkIfPtInCluster(double x, double y, Integer LayerID) {
        boolean clusterHit;
        boolean layerHit = false;
        int offsetX;
        int offsetY;

        if(object.equals("A11")){
            offsetX = 444;
            offsetY = 437;
        }
        else{
            offsetX = 901;
            offsetY = 1008;
        }

        for(Tuple3<String, Cluster, AABB> c : clusters.get(LayerID-290).f1){


            List<DoublePoint> point_list = c.f1.getPoints();

            clusterHit = false;
            for(DoublePoint p : point_list){
                
                double realPointX = p.getPoint()[0] + offsetX + 20;// pos of object + padding
                double realPointY = p.getPoint()[1] + offsetY + 20;// pos of object + padding
                if(realPointX == x && realPointY == y && !clusterHit){
                    clustersThatHit.add(c);
                    nClusters += 1;
                    truePositives += 1; //point_list.size();
                    falsePositive -= 1; //point_list.size();
                    layerHit = true;
                }
            }


            

        }

        if(layerHit){
            return true;
        }
        return false;
    }



    public void hasHitErik(Integer LayerID){
        String csvFile = "csvFiles/detections(in).csv";
        String line = "";
        String csvSplitBy = ",";
        

        //save for points
        //for(Tuple3<String, Cluster, AABB> e : clusters.get(LayerID-290).f1){
            //falsePositive += 1;//e.f1.getPoints().size();
        //}

        falsePositive += clusters.get(LayerID - 290).f1.size();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            double layer = (double) LayerID;

            while ((line = br.readLine()) != null) {
                // Split the line by commas
                String[] data = line.split(csvSplitBy);
                String fourthColumn = data[3];

                if(!fourthColumn.equals(object)){
                    continue;
                }
                
                int z = Integer.parseInt(data[0]) - 1;
                
                if(layer == z){
                    int x = Integer.parseInt(data[2]);
                    int y = Integer.parseInt(data[1]);
                    
                    counter += 1;
                    checkIfPtInClusterErik(x, y, LayerID);
                        

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        trueNegative = IMG_SIZE * (LayerID-289) - truePositives - falsePositive - falseNegative;

    }




    private boolean checkIfPtInClusterErik(int x, int y, Integer LayerID) {
        boolean layerHit = false;
        boolean clusterHit = false;
        int offsetX;
        int offsetY;

        if(object.equals("A11")){
            offsetX = 444 + 19;
            offsetY = 437 + 19;
        }
        else{
            offsetX = 901 + 19;
            offsetY = 1008 + 19;
        }

        for(Tuple3<String, Cluster, AABB> c : clusters.get(LayerID-290).f1){


            List<DoublePoint> point_list = c.f1.getPoints();

            for(DoublePoint p : point_list){
                
                int realPointX = (int) p.getPoint()[0] + offsetX;// pos of object + padding
                int realPointY = (int) p.getPoint()[1] + offsetY;// pos of object + padding

                if(realPointX == x && realPointY == y){
                    if(!clustersThatHit.contains(c)){
                        clustersThatHit.add(c);
                        falsePositive -= 1; //point_list.size();
                    }
                    
                    nClusters += 1;
                    layerHit = true;
                }
            }


            

        }

        if(layerHit){
            truePositives += 1; //point_list.size();
            return true;
        }
        System.out.println("miss in layer: " + LayerID);
        falseNegative += 1;
        return false;
    }

}

