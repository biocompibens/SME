package SME_PROJECTION_SRC;

import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.ml.clustering.Clusterable;

// wrapper class
public class SME_ENS_PixelPoints implements Clusterable {
    private double[] points;
    private RealVector location;
    private int indexPixel;

    public SME_ENS_PixelPoints(RealVector location,int indxPix) {
        this.location = location;
        this.points = location.toArray() ;
        indexPixel  = indxPix;
    }

    public RealVector getLocation() {
        return location;
    }

    public int getIndexPixel(){
        return indexPixel;
    }

    public double[] getPoint() {
        return points;
    }
}
