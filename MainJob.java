


import java.awt.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Types;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.sound.midi.SysexMessage;

import org.apache.commons.compress.archivers.dump.DumpArchiveEntry.TYPE;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math4.legacy.ml.clustering.DoublePoint;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.io.FileInputFormat;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.connector.source.Source;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.typeutils.runtime.kryo.KryoSerializer;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.file.sink.compactor.RecordWiseFileCompactor.Reader;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.streaming.api.datastream.AllWindowedStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.triggers.EventTimeTrigger;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.table.runtime.operators.window.groupwindow.assigners.CountSlidingWindowAssigner;
import org.apache.flink.table.shaded.com.ibm.icu.impl.locale.LocaleDistance.Data;
import org.apache.flink.util.Collector;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.core.fs.Path;
import org.apache.flink.core.memory.DataOutputSerializer;
import org.apache.flink.kubernetes.shaded.io.fabric8.kubernetes.api.model.Event;




public class MainJob {

    static float PERCENT = 0.05f;
    static int NEIGBORHOOD_SIZE = 40;
    static float EPSILON = 1.5f;
    static int minPts = 7;
    static int IMG_SIZE = 50*50;
    static String object = "B10";

    private static int index = 290;

    

    public static String[] getFileNames(){
        ArrayList<String> names = new ArrayList<String>();

        try(BufferedReader r = new BufferedReader(new FileReader("imageNamesClaudia"+ object +".txt"))){
            String line;
            while((line = r.readLine()) != null){
                names.add(line);
            }

        }catch(IOException e){
            System.out.println("Failed to read.");
        }

        String[] fileNames = new String[names.size()];
        for(int i = 0; i < names.size(); i++){
            fileNames[i] = names.get(i);
        }
        return fileNames;
    }

    public static void main(String[] args) throws Exception{


        new writeToFile("object_layer,startTime,neigborhoodSize,percent,nOfPtsInIMG,epsilon,minPts,cluPts/outPts,TP,FP,TN,FN,incHits,similarity[%],AVGclusterheight,endTime\n", object, PERCENT);


        //for(int i = 0; i < 10; i++){
            streamStarter();

        //}

        /*for(float eps = 1; eps < 3; eps+=0.5){
            for(int minp = 3; minp < 8; minp++){
                for(int nsize = 5; nsize < 35; nsize+=5){
                    for(float per = 0.05f; per < 0.25; per+=0.05){
                        index = 0;
                        EPSILON = eps;
                        minPts = minp;
                        NEIGBORHOOD_SIZE = nsize;
                        PERCENT = per;
                        streamStarter();
                    }
                }
            }
        }*/



        

        

    }

    private static void streamStarter() throws Exception{
  
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.setParallelism(1);

    
            String[] fileNames = getFileNames();
            
    
    
            DataStream<String> input = env.fromElements(fileNames);
    
            //Assign index to each file
            DataStream<Tuple2<Integer, String>> countStream = input.flatMap(new AssignIndexToFiles());
            //attach operator to stream
            DataStream<Tuple2<Tuple2<Integer, String>, Double[][]>> stream = countStream.map(new Moran(NEIGBORHOOD_SIZE, PERCENT, object));
    
            //Get the clusters and return them with the ID
            DataStream<Tuple2<Integer, ArrayList<Tuple3<String, Cluster, AABB>>>> clusterStream = stream.map(new ClusteringNode(EPSILON, minPts));
            DataStream<String> clu = clusterStream.map(new AddClusters(EPSILON, 50*50, object));
    
            env.execute("StreamPipeline");

        
    }

    public static class AssignIndexToFiles implements FlatMapFunction<String, Tuple2<Integer, String>>{

        @Override
        public void flatMap(String arg0, Collector<Tuple2<Integer, String>> arg1) throws Exception {
            arg1.collect(new Tuple2<>(index++, arg0));
        }

        

    }

}