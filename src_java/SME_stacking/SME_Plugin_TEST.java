/** Elton REXHEPAJ **/


package ij.plugin.filter.SME_PROJECTION_SRC;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.filter.EDM;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

//import java.util.Vector;
//import ij.util.ArrayUtil;

/**
 Plugin Description :
 **/


public class SME_Plugin_TEST implements PlugInFilter {

    private ImagePlus imp;
    private ImagePlus imp2;
    private ImagePlus imp3;
    private ImagePlus imp5;
    private ImagePlus imp5_ind;
    private ImagePlus imp6;
    private SME_KMeans_Paralel mKMeans;
    private ImageStack stack;
    private double[] kmeansLabels;
    private double[][] kmeanCentroids;

    protected float mean0;
    protected float mean1;
    protected float mean2;

    private ImageStack Blur0;
    private ImageStack Blur1;
    private ImageStack Blur2;
    private ImageStack Blur3;
    private ImageStack Blur4;
    private ImageStack Blur5;

    private ImageStack stack1;
    private float[][] Map2DImage;
    private SME_Cluster[] clustersKmean;
    private SME_ENS_GUI_MAIN gui_main = null;
    private ImagePlus map2d ;

    // Output step by step images
    private ImagePlus rawImage = null;
    private ImagePlus projImage = null;
    private ImagePlus smlImage = null;
    private ImagePlus kmensImage = null;
    private ImagePlus mfoldImage = null;
    private ImagePlus smeImage = null;

    private final String MANIFOLD = "Manifold2D";
    private final String RAWIMAGE = "RawStack";


    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL + STACK_REQUIRED; // Works for stack images
    }

    public void run(ImageProcessor ip) {
        this.rawImage = imp.duplicate();
        stack = imp.getStack();                  // ImagePlus into ImageStack
        this.stack1 = stack.duplicate();   // Duplicates the original stack image
        ImageStack stack2 = stack.duplicate();   // Duplicates the original stack image
        ImageStack stack3 = stack.duplicate();   // Duplicates the original stack image
        ImageStack stack4 = stack.duplicate();   // Duplicates the original stack image


        runSmlStep();
        //runKmeansStep();
        //runEnergyOptimisation();

    }

    public void runSmlStep(){
        this.runSml();
    }

    public void runSml(){
        SME_ENS_Sml_TEST smlPlugin = new SME_ENS_Sml_TEST(this);
        smlPlugin.applySML();
    }


    /************************************************ Getter and Setters *******************************/

    public ImagePlus getImp() {
        return imp;
    }

    public void setImp(ImagePlus imp) {
        this.imp = imp;
    }

    public ImagePlus getImp2() {
        return imp2;
    }

    public void setImp2(ImagePlus imp2) {
        this.imp2 = imp2;
    }

    public ImagePlus getImp3() {
        return imp3;
    }

    public void setImp3(ImagePlus imp3) {
        this.imp3 = imp3;
    }

    public ImagePlus getImp5() {
        return imp5;
    }

    public void setImp5(ImagePlus imp5) {
        this.imp5 = imp5;
    }

    public ImagePlus getImp5_ind() {
        return imp5_ind;
    }

    public void setImp5_ind(ImagePlus imp5_ind) {
        this.imp5_ind = imp5_ind;
    }

    public ImagePlus getImp6() {
        return imp6;
    }

    public void setImp6(ImagePlus imp6) {
        this.imp6 = imp6;
    }

    public SME_KMeans_Paralel getmKMeans() {
        return mKMeans;
    }

    public void setmKMeans(SME_KMeans_Paralel mKMeans) {
        this.mKMeans = mKMeans;
    }

    public ImageStack getStack() {
        return stack;
    }

    public void setStack(ImageStack stack) {
        this.stack = stack;
    }

    public double[] getKmeansLabels() {
        return kmeansLabels;
    }

    public void setKmeansLabels(double[] kmeansLabels) {
        this.kmeansLabels = kmeansLabels;
    }

    public double[][] getKmeanCentroids() {
        return kmeanCentroids;
    }

    public void setKmeanCentroids(double[][] kmeanCentroids) {
        this.kmeanCentroids = kmeanCentroids;
    }

    public ImageStack getStack1() {
        return stack1;
    }

    public void setStack1(ImageStack stack1) {
        this.stack1 = stack1;
    }

    public float[][] getMap2DImage() {
        return Map2DImage;
    }

    public void setMap2DImage(float[][] map2DImage) {
        Map2DImage = map2DImage;
    }

    public SME_Cluster[] getClustersKmean() {
        return clustersKmean;
    }

    public void setClustersKmean(SME_Cluster[] clustersKmean) {
        this.clustersKmean = clustersKmean;
    }

    public SME_ENS_GUI_MAIN getGui_main() {
        return gui_main;
    }

    public void setGui_main(SME_ENS_GUI_MAIN gui_main) {
        this.gui_main = gui_main;
    }

    public ImagePlus getMap2d() {
        return map2d;
    }

    public void setMap2d(ImagePlus map2d) {
        this.map2d = map2d;
    }

    public float getMean0() {
        return mean0;
    }

    public void setMean0(float mean0) {
        this.mean0 = mean0;
    }

    public float getMean1() {
        return mean1;
    }

    public void setMean1(float mean1) {
        this.mean1 = mean1;
    }

    public float getMean2() {
        return mean2;
    }

    public void setMean2(float mean2) {
        this.mean2 = mean2;
    }

    public ImageStack getBlur0() {
        return Blur0;
    }

    public void setBlur0(ImageStack blur0) {
        Blur0 = blur0;
    }

    public ImageStack getBlur1() {
        return Blur1;
    }

    public void setBlur1(ImageStack blur1) {
        Blur1 = blur1;
    }

    public ImageStack getBlur2() {
        return Blur2;
    }

    public void setBlur2(ImageStack blur2) {
        Blur2 = blur2;
    }

    public ImageStack getBlur3() {
        return Blur3;
    }

    public void setBlur3(ImageStack blur3) {
        Blur3 = blur3;
    }

    public ImageStack getBlur4() {
        return Blur4;
    }

    public void setBlur4(ImageStack blur4) {
        Blur4 = blur4;
    }

    public ImageStack getBlur5() {
        return Blur5;
    }

    public void setBlur5(ImageStack blur5) {
        Blur5 = blur5;
    }

    public ImagePlus getRawImage() {
        return rawImage;
    }

    public void setRawImage(ImagePlus rawImage) {
        this.rawImage = rawImage;
    }

    public ImagePlus getSmlImage() {
        return smlImage;
    }

    public void setSmlImage(ImagePlus smlImage) {
        this.smlImage = smlImage;
    }

    public ImagePlus getKmensImage() {
        return kmensImage;
    }

    public void setKmensImage(ImagePlus kmensImage) {
        this.kmensImage = kmensImage;
    }

    public ImagePlus getMfoldImage() {
        return mfoldImage;
    }

    public void setMfoldImage(ImagePlus mfoldImage) {
        this.mfoldImage = mfoldImage;
    }

    public ImagePlus getSmeImage() {
        return smeImage;
    }

    public void setSmeImage(ImagePlus smeImage) {
        this.smeImage = smeImage;
    }

    public ImagePlus getProjImage() {
        return projImage;
    }

    public void setProjImage(ImagePlus projImage) {
        this.projImage = projImage;
    }
}