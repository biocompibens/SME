package ij.plugin.filter.SME_PROJECTION_SRC;

import ij.*;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.*;
import java.util.Arrays;

/**
 * Created by rexhepaj on 16/03/16.
 */
public class SME_ENS_Image_Prepare {
    /**
     * Image stack context
     */
    private static final long serialVersionUID = 1L;

    private Image imShow = null;;
    private ImagePlus projImage = null; /** Image to hold z-projection. */
    private ImagePlus imp = null; /** Image stack to project. */
    private Boolean projectImage = Boolean.TRUE;
    private int startSlice = 1;/** Projection starts from this slice. */
    private int stopSlice = 1;/** Projection ends at this slice. */
    private boolean allTimeFrames = true;/** Project all time points? */

    public static final int AVG_METHOD = 0;
    public static final int MAX_METHOD = 1;
    public static final int MIN_METHOD = 2;
    public static final int SUM_METHOD = 3;
    public static final int SD_METHOD = 4;
    public static final int MEDIAN_METHOD = 5;
    public static final String[] METHODS =
            {"Average Intensity", "Max Intensity", "Min Intensity", "Sum Slices", "Standard Deviation", "Median"};
    private static final String METHOD_KEY = "zproject.method";
    private int method = 0;

    private static final int BYTE_TYPE  = 0;
    private static final int SHORT_TYPE = 1;
    private static final int FLOAT_TYPE = 2;

    public static final String projectionMessage =
            "Stacks with inverter LUTs may not project correctly.\n"
                    +"To create a standard LUT, invert the stack (Edit/Invert)\n"
                    +"and invert the LUT (Image/Lookup Tables/Invert LUT).";

    private String color = "";
    private boolean isHyperstack;
    private int increment = 1;
    private int sliceCount;

    /**
     * Constructor to build the image for the display
     * @param imstk
     */
    public SME_ENS_Image_Prepare(ImagePlus imstk, Boolean imProj){
        projectImage = imProj;
        setImage(imstk);
    }

    /** Explicitly set image to be projected. This is useful if
     ZProjection_ object is to be used not as a plugin but as a
     stand alone processing object.  */
    public void setImage(ImagePlus imstack) {
        this.imp = imstack;
        startSlice = 1;
        stopSlice = imp.getStackSize();
    }

    public Image getImageFromProjection(int projectionMeth){
        Image im2show = null;
        method = projectionMeth;
        // TODO: change the projection method according to the value of the string projectionMeth
        runZProject();

        // get from projImage the actual 2D projection image
        im2show = projImage.getImage();
        return(im2show);
    }


    public void setMethod(int projMethod){
        method = projMethod;
    }

    /** Retrieve results of most recent projection operation.*/
    public ImagePlus getProjection() {
        return projImage;
    }

    //************************* ZPROJECT ***********************************//

    public void runZProject() {

        if (!imp.lock()) return;   // exit if in use
        else doProjection(true);

        //projImage.setCalibration(imp.getCalibration());
        //projImage.show("ZProjector");
    }

    /** Performs actual projection using specified method. */
    public void doProjection() {
        if (imp==null)
            return;
        sliceCount = 0;
        if (method<AVG_METHOD || method>MEDIAN_METHOD)
            method = AVG_METHOD;
        for (int slice=startSlice; slice<=stopSlice; slice+=increment)
            sliceCount++;
        if (method==MEDIAN_METHOD) {
            projImage = doMedianProjection();
            return;
        }

        // Create new float processor for projected pixels.
        FloatProcessor fp = new FloatProcessor(imp.getWidth(),imp.getHeight());
        ImageStack stack = imp.getStack();
        SME_ENS_Projection_Function rayFunc = getRayFunction(method, fp);
        if (IJ.debugMode==true) {
            IJ.log("\nProjecting stack from: "+startSlice
                    +" to: "+stopSlice);
        }

        // Determine type of input image. Explicit determination of
        // processor type is required for subsequent pixel
        // manipulation.  This approach is more efficient than the
        // more general use of ImageProcessor's getPixelValue and
        // putPixel methods.
        int ptype;
        if (stack.getProcessor(1) instanceof ByteProcessor) ptype = BYTE_TYPE;
        else if (stack.getProcessor(1) instanceof ShortProcessor) ptype = SHORT_TYPE;
        else if (stack.getProcessor(1) instanceof FloatProcessor) ptype = FLOAT_TYPE;
        else {
            IJ.error("Z Project", "Non-RGB stack required");
            return;
        }

        // Do the projection
        int sliceCount = 0;
        for (int n=startSlice; n<=stopSlice; n+=increment) {
            if (!isHyperstack) {
                IJ.showStatus("ZProjection " + color +": " + n + "/" + stopSlice);
                IJ.showProgress(n-startSlice, stopSlice-startSlice);
            }
            projectSlice(stack.getPixels(n), rayFunc, ptype);
            sliceCount++;
        }

        // Finish up projection.
        if (method==SUM_METHOD) {
            if (imp.getCalibration().isSigned16Bit())
                fp.subtract(sliceCount*32768.0);
            fp.resetMinAndMax();
            projImage = new ImagePlus(makeTitle(), fp);
        } else if (method==SD_METHOD) {
            rayFunc.postProcess();
            fp.resetMinAndMax();
            projImage = new ImagePlus(makeTitle(), fp);
        } else {
            rayFunc.postProcess();
            projImage = makeOutputImage(imp, fp, ptype);
        }

        if(projImage==null)
            IJ.error("Z Project", "Error computing projection.");
    }

    /** Performs actual projection using specified method. If handleOverlay,
     adds stack overlay elements from startSlice to stopSlice to projection*/
    public void doProjection(boolean handleOverlay) {
        doProjection();
        Overlay overlay = imp.getOverlay();
        if (handleOverlay && overlay!=null)
            projImage.setOverlay(projectStackRois(overlay));
    }

    private Overlay projectStackRois(Overlay overlay) {
        if (overlay==null) return null;
        Overlay overlay2 = overlay.create();
        Roi roi;
        int s;
        for (Roi r : overlay.toArray()) {
            s = r.getPosition();
            roi = (Roi)r.clone();
            if (s>=startSlice && s<=stopSlice || s==0) {
                roi.setPosition(s);
                overlay2.add(roi);
            }
        }
        return overlay2;
    }


    private SME_ENS_Projection_Function getRayFunction(int method, FloatProcessor fp) {
        switch (method) {
            case AVG_METHOD:
                return new SME_ENS_ENS_Projection_Average_Intensity(fp, sliceCount);
            case MAX_METHOD:
                return new SME_ENS_Projection_Max_Intensity(fp);
            case SUM_METHOD:
                return new SME_ENS_Projection_Sum_Intensity(fp);
            case MEDIAN_METHOD:
                return new SME_ENS_Projection_Median_Intensity(fp);
            case MIN_METHOD:
                return new SME_ENS_Projection_Min_Intensity(fp);
            case SD_METHOD:
                return new SME_ENS_Projection_STD_Intensity(fp, sliceCount);
            default:
                IJ.error("Z Project", "Unknown method.");
                return null;
        }
    }

    /** Generate output image whose type is same as input image. */
    private ImagePlus makeOutputImage(ImagePlus imp, FloatProcessor fp, int ptype) {
        int width = imp.getWidth();
        int height = imp.getHeight();
        float[] pixels = (float[])fp.getPixels();
        ImageProcessor oip=null;

        // Create output image consistent w/ type of input image.
        int size = pixels.length;
        switch (ptype) {
            case BYTE_TYPE:
                oip = imp.getProcessor().createProcessor(width,height);
                byte[] pixels8 = (byte[])oip.getPixels();
                for(int i=0; i<size; i++)
                    pixels8[i] = (byte)pixels[i];
                break;
            case SHORT_TYPE:
                oip = imp.getProcessor().createProcessor(width,height);
                short[] pixels16 = (short[])oip.getPixels();
                for(int i=0; i<size; i++)
                    pixels16[i] = (short)pixels[i];
                break;
            case FLOAT_TYPE:
                oip = new FloatProcessor(width, height, pixels, null);
                break;
        }

        // Adjust for display.
        // Calling this on non-ByteProcessors ensures image
        // processor is set up to correctly display image.
        oip.resetMinAndMax();

        // Create new image plus object. Don't use
        // ImagePlus.createImagePlus here because there may be
        // attributes of input image that are not appropriate for
        // projection.
        return new ImagePlus(makeTitle(), oip);
    }

    /** Handles mechanics of projection by selecting appropriate pixel
     array type. We do this rather than using more general
     ImageProcessor getPixelValue() and putPixel() methods because
     direct manipulation of pixel arrays is much more efficient.  */
    private void projectSlice(Object pixelArray, SME_ENS_Projection_Function rayFunc, int ptype) {
        switch(ptype) {
            case BYTE_TYPE:
                rayFunc.projectSlice((byte[])pixelArray);
                break;
            case SHORT_TYPE:
                rayFunc.projectSlice((short[])pixelArray);
                break;
            case FLOAT_TYPE:
                rayFunc.projectSlice((float[])pixelArray);
                break;
        }
    }

    String makeTitle() {
        String prefix = "AVG_";
        switch (method) {
            case SUM_METHOD: prefix = "SUM_"; break;
            case MAX_METHOD: prefix = "MAX_"; break;
            case MIN_METHOD: prefix = "MIN_"; break;
            case SD_METHOD:  prefix = "STD_"; break;
            case MEDIAN_METHOD:  prefix = "MED_"; break;
        }
        return WindowManager.makeUniqueName(prefix+imp.getTitle());
    }

    ImagePlus doMedianProjection() {
        IJ.showStatus("Calculating median...");
        ImageStack stack = imp.getStack();
        ImageProcessor[] slices = new ImageProcessor[sliceCount];
        int index = 0;
        for (int slice=startSlice; slice<=stopSlice; slice+=increment)
            slices[index++] = stack.getProcessor(slice);
        ImageProcessor ip2 = slices[0].duplicate();
        ip2 = ip2.convertToFloat();
        float[] values = new float[sliceCount];
        int width = ip2.getWidth();
        int height = ip2.getHeight();
        int inc = Math.max(height/30, 1);
        for (int y=0; y<height; y++) {
            if (y%inc==0) IJ.showProgress(y, height-1);
            for (int x=0; x<width; x++) {
                for (int i=0; i<sliceCount; i++)
                    values[i] = slices[i].getPixelValue(x, y);
                ip2.putPixelValue(x, y, median(values));
            }
        }
        if (imp.getBitDepth()==8)
            ip2 = ip2.convertToByte(false);
        IJ.showProgress(1, 1);
        return new ImagePlus(makeTitle(), ip2);
    }

    float median(float[] a) {
        Arrays.sort(a);
        int middle = a.length/2;
        if ((a.length&1)==0) //even
            return (a[middle-1] + a[middle])/2f;
        else
            return a[middle];
    }











}
