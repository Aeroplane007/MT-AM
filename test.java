import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;

public class test implements MapFunction<Tuple2<Integer, List<Cluster>>, String>{


    @Override
    public String map(Tuple2<Integer, List<Cluster>> arg0) throws Exception {
        // TODO Auto-generated method stub

        List<Cluster> clusters= arg0.f1;

        System.out.println("Cluster: " + ((DoublePoint) clusters.get(0).getPoints().get(0)).getPoint() [0]);
        return null;
    }
    
}
