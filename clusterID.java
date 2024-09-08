public class clusterID {
    private int clusterIndex;
    private int layerID;
    private String clusterID;

    public clusterID(int clusterIndex, int laterID){
        this.clusterIndex = clusterIndex;
        this.layerID = layerID;
        clusterID = layerID + "_" + clusterIndex;
    }

    public int getClusterIndex(){
        return clusterIndex;
    }

    public int getLayerID(){
        return layerID;
    }

    public String getClusterID(){
        return clusterID;
    }

}
