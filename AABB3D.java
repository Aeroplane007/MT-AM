
public class AABB3D {
    private double min_x;
    private double max_x;
    private double min_y;
    private double max_y;
    private double min_z;
    private double max_z;

    public AABB3D(double min_x,  double max_x, double min_y, double max_y, double min_z, double max_z){
        this.min_x = min_x;
        this.max_x = max_x;
        this.min_y = min_y;
        this.max_y = max_y;
        this.min_z = min_z;
        this.max_z = max_z;
    }



    public double getmin_x(){
        return min_x;
    }

    public double getmax_x(){
        return max_x;
    }

    public double getmin_y(){
        return min_y;
    }

    public double getmax_y(){
        return max_y;
    }
    
    public double getmin_z(){
        return min_z;
    }

    public double getmax_z(){
        return max_z;
    }

    public double getVolume(){
        return (max_x - min_x) * (max_y - min_y) * (max_z - min_z);
    }
}
