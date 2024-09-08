
public class AABB {
    private double min_x;
    private double max_x;
    private double min_y;
    private double max_y;

    public AABB(double min_x,  double max_x, double min_y, double max_y){
        this.min_x = min_x;
        this.max_x = max_x;
        this.min_y = min_y;
        this.max_y = max_y;
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
}
