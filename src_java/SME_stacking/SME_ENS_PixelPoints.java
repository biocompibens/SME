package ij.plugin.filter.SME_PROJECTION_SRC;

import org.apache.commons.math3.geometry.partitioning.Region;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.neuralnet.twod.util.LocationFinder;

import java.util.ArrayList;

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
