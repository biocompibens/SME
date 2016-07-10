package ij.plugin.filter.SME_PROJECTION_SRC;

import ij.*;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.ChannelSplitter;
import ij.plugin.RGBStackMerge;
import ij.process.ImageProcessor;

import java.awt.*;

/**
 * Created by rexhepaj on 16/03/16.
 */
public class SME_ENS_Projection_COLOURS {
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
    private int method = (int) Prefs.get(METHOD_KEY, AVG_METHOD);

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

    public void doRGBProjection() {
        doRGBProjection(imp.getStack());
    }

    public void doRGBProjection(boolean handleOverlay) {
        doRGBProjection(imp.getStack());
        Overlay overlay = imp.getOverlay();
        if (handleOverlay && overlay!=null)
            projImage.setOverlay(projectRGBHyperStackRois(overlay));
    }

    private Overlay projectRGBHyperStackRois(Overlay overlay) {
        if (overlay==null) return null;
        int frames = projImage.getNFrames();
        int t1 = imp.getFrame();
        Overlay overlay2 = overlay.create();
        Roi roi;
        int c, z, t;
        for (Roi r : overlay.toArray()) {
            c = r.getCPosition();
            z = r.getZPosition();
            t = r.getTPosition();
            roi = (Roi)r.clone();
            if (z>=startSlice && z<=stopSlice || z==0 || c==0 || t==0) {
                if (frames==1 && t!=t1 && t!=0)//current time frame
                    continue;
                roi.setPosition(t);
                overlay2.add(roi);
            }
        }
        return overlay2;
    }

    public void doHyperStackProjection(boolean allTimeFrames) {
        int start = startSlice;
        int stop = stopSlice;
        int firstFrame = 1;
        int lastFrame = imp.getNFrames();
        if (!allTimeFrames)
            firstFrame = lastFrame = imp.getFrame();
        ImageStack stack = new ImageStack(imp.getWidth(), imp.getHeight());
        int channels = imp.getNChannels();
        int slices = imp.getNSlices();
        if (slices==1) {
            slices = imp.getNFrames();
            firstFrame = lastFrame = 1;
        }
        int frames = lastFrame-firstFrame+1;
        increment = channels;
        boolean rgb = imp.getBitDepth()==24;
        for (int frame=firstFrame; frame<=lastFrame; frame++) {
            IJ.showStatus(""+ (frame-firstFrame) + "/" + (lastFrame-firstFrame));
            IJ.showProgress(frame-firstFrame, lastFrame-firstFrame);
            for (int channel=1; channel<=channels; channel++) {
                startSlice = (frame-1)*channels*slices + (start-1)*channels + channel;
                stopSlice = (frame-1)*channels*slices + (stop-1)*channels + channel;
                if (rgb)
                    doHSRGBProjection(imp);
                else
                    doProjection();
                stack.addSlice(null, projImage.getProcessor());
            }
        }
        projImage = new ImagePlus("Test", stack);
        projImage.setDimensions(channels, 1, frames);
        if (channels>1) {
            projImage = new CompositeImage(projImage, 0);
            ((CompositeImage)projImage).copyLuts(imp);
            if (method==SUM_METHOD || method==SD_METHOD)
                ((CompositeImage)projImage).resetDisplayRanges();
        }
        if (frames>1)
            projImage.setOpenAsHyperStack(true);
        Overlay overlay = imp.getOverlay();
        if (overlay!=null) {
            startSlice = start;
            stopSlice = stop;
            if (imp.getType()==ImagePlus.COLOR_RGB)
                projImage.setOverlay(projectRGBHyperStackRois(overlay));
            else
                projImage.setOverlay(projectHyperStackRois(overlay));
        }
        IJ.showProgress(1, 1);
    }

    private void doProjection() {
    }

    private void doHSRGBProjection(ImagePlus rgbImp) {
        ImageStack stack = rgbImp.getStack();
        ImageStack stack2 = new ImageStack(stack.getWidth(), stack.getHeight());
        for (int i=startSlice; i<=stopSlice; i++)
            stack2.addSlice(null, stack.getProcessor(i));
        startSlice = 1;
        stopSlice = stack2.getSize();
        doRGBProjection(stack2);
    }

    private void doRGBProjection(ImageStack stack) {
        ImageStack[] channels = ChannelSplitter.splitRGB(stack, true);
        ImagePlus red = new ImagePlus("Red", channels[0]);
        ImagePlus green = new ImagePlus("Green", channels[1]);
        ImagePlus blue = new ImagePlus("Blue", channels[2]);
        imp.unlock();
        ImagePlus saveImp = imp;
        imp = red;
        color = "(red)"; doProjection();
        ImagePlus red2 = projImage;
        imp = green;
        color = "(green)"; doProjection();
        ImagePlus green2 = projImage;
        imp = blue;
        color = "(blue)"; doProjection();
        ImagePlus blue2 = projImage;
        int w = red2.getWidth(), h = red2.getHeight(), d = red2.getStackSize();
        if (method==SD_METHOD) {
            ImageProcessor r = red2.getProcessor();
            ImageProcessor g = green2.getProcessor();
            ImageProcessor b = blue2.getProcessor();
            double max = 0;
            double rmax = r.getStatistics().max; if (rmax>max) max=rmax;
            double gmax = g.getStatistics().max; if (gmax>max) max=gmax;
            double bmax = b.getStatistics().max; if (bmax>max) max=bmax;
            double scale = 255/max;
            r.multiply(scale); g.multiply(scale); b.multiply(scale);
            red2.setProcessor(r.convertToByte(false));
            green2.setProcessor(g.convertToByte(false));
            blue2.setProcessor(b.convertToByte(false));
        }
        RGBStackMerge merge = new RGBStackMerge();
        ImageStack stack2 = merge.mergeStacks(w, h, d, red2.getStack(), green2.getStack(), blue2.getStack(), true);
        imp = saveImp;
        projImage = new ImagePlus("Test", stack2);
    }

    private Overlay projectHyperStackRois(Overlay overlay) {
        if (overlay==null) return null;
        int t1 = imp.getFrame();
        int channels = projImage.getNChannels();
        int slices = 1;
        int frames = projImage.getNFrames();
        Overlay overlay2 = overlay.create();
        Roi roi;
        int c, z, t;
        int size = channels * slices * frames;
        for (Roi r : overlay.toArray()) {
            c = r.getCPosition();
            z = r.getZPosition();
            t = r.getTPosition();
            roi = (Roi)r.clone();
            if (size==channels) {//current time frame
                if (z>=startSlice && z<=stopSlice && t==t1 || c==0) {
                    roi.setPosition(c);
                    overlay2.add(roi);
                }
            }
            else if (size==frames*channels) {//all time frames
                if (z>=startSlice && z<=stopSlice)
                    roi.setPosition(c, 1, t);
                else if (z==0)
                    roi.setPosition(c, 0, t);
                else continue;
                overlay2.add(roi);
            }
        }
        return overlay2;
    }


}
