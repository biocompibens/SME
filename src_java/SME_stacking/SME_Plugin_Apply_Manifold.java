/** Nikita MENEZES B.**/
/** Elton REXHEPAJ **/
/** Asm SHIAVUDDIN **/
/** Sreetama BASU **/

package ij.plugin.filter.SME_PROJECTION_SRC;

import ij.*;
import ij.gui.GenericDialog;
import ij.plugin.CompositeConverter;
import ij.plugin.PlugIn;
import ij.plugin.filter.EDM;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import javax.swing.*;
import java.awt.*;
import java.awt.image.IndexColorModel;
import java.util.Arrays;

//import java.util.Vector;
//import ij.util.ArrayUtil;

/**
 Plugin Description :
 **/


public class SME_Plugin_Apply_Manifold implements PlugIn {

    private ImagePlus imp;
    private ImagePlus rawImage;
    private ImagePlus projectedImage;
    private ImagePlus manifold ;


    private static String none = "*None*";
    private static int maxChannels = 4;
    private static int nmbChannels = 3;
    private static String[] colors = {"Channel1 stack1 to appy the manifold",
            "Channel2 stack1 to appy the manifold",
            "Channel3 stack1 to appy the manifold",
            "Channel4 stack1 to appy the manifold"};

    private static boolean staticCreateComposite = true;
    private static boolean staticKeep;
    private static boolean staticIgnoreLuts;

    private byte[] blank;
    private boolean ignoreLuts;
    private boolean autoFillDisabled;
    private String firstChannelName;
    private ImagePlus[] projectionStacks ;
    private int stackSize = 0;
    private int width = 0;
    private int height = 0;
    public void run(String arg) {

        processChannelsManifold();
    }

    /** Combines up to seven grayscale stacks into one RGB or composite stack. */
    public void processChannelsManifold() {
        int[] wList         = WindowManager.getIDList();
        projectionStacks    = new ImagePlus[maxChannels];

        if (wList==null) {
            error("No images are open.");
            return;
        }else if(wList.length<1){
            error("There must be at least two images open: 1) Stack to project and 2) Manifold");
            return;
        }

        String[] titles = new String[wList.length+1];
        for (int i=0; i<wList.length; i++) {
            ImagePlus imp = WindowManager.getImage(wList[i]);
            titles[i] = imp!=null?imp.getTitle():"";
        }

        titles[wList.length] = none;
        String[] names = getInitialNamesAllstacks(titles);
        boolean createComposite = staticCreateComposite;
        boolean keep = staticKeep;
        ignoreLuts = staticIgnoreLuts;

        String options = Macro.getOptions();
        boolean macro = IJ.macroRunning() && options!=null;
        if (macro) {
            createComposite = keep = ignoreLuts = false;
            options = options.replaceAll("red=", "c1=");
            options = options.replaceAll("green=", "c2=");
            options = options.replaceAll("blue=", "c3=");
            options = options.replaceAll("gray=", "c4=");
            Macro.setOptions(options);
        }

        GenericDialog gd = new GenericDialog("SME APPLY MANIFOLD TO OPEN STACKS");
        gd.addChoice("Manifold  : (uint8 0-255)", titles, macro?none:names[0]);
        gd.addChoice("Channel 1 : (Mapped to RED in composite)", titles, macro?none:names[1]);
        gd.addChoice("Channel 2 : (Mapped to GREEN in composite)", titles, macro?none:names[2]);
        gd.addChoice("Channel 3 : (Mapped to Blue in composite)", titles, macro?none:names[3]);

        //gd.addCheckbox("Create composite", createComposite);
        //gd.addCheckbox("Keep source images", keep);
        //gd.addCheckbox("Ignore source LUTs", ignoreLuts);
        gd.showDialog();
        if (gd.wasCanceled())
            return;
        int[] index = new int[maxChannels];

        for (int i=0; i<maxChannels; i++) {
            index[i] = gd.getNextChoiceIndex();
        }

        ImagePlus[] images = new ImagePlus[maxChannels];

        stackSize = 0;
        width = 0;
        height = 0;

        int slices = 0;
        int frames = 0;
        for (int i=0; i<maxChannels; i++) {

            //IJ.log(i+"  "+index[i]+"	"+titles[index[i]]+"  "+wList.length);
            if (index[i]<wList.length) {
                images[i] = WindowManager.getImage(wList[index[i]]);
                if(width<images[i].getWidth()){width = images[i].getWidth();}
                if(height<images[i].getHeight()){height = images[i].getHeight();}
                if(stackSize<images[i].getStackSize()){stackSize = images[i].getStackSize();}
            }
        }

        if (width==0) {
            error("There must be at least one source image or stack.");
            return;
        }


        boolean mergeHyperstacks = false;
        for (int i=1; i<maxChannels; i++) {

            // break out of the loop if no more images to project

            //if(i>=nmbChannels){
            //    break;
            //}

            ImagePlus img = images[i];

            if (img==null) continue;
            if (img.getStackSize()!=stackSize) {
                error("SME PROJECT = The source stacks must have the same number of images.");
                return;
            }
            if (img.isHyperStack()) {
                if (img.getNChannels()>1) {
                    error("SME PROJECT = Source hyperstacks cannot have more than 1 channel.");
                    return;
                }
                if (img.getNSlices()!=slices || img.getNFrames()!=frames) {
                    error("Source hyperstacks must have the same dimensions.");
                    return;
                }
                mergeHyperstacks = true;
            } // isHyperStack
            if (img.getWidth()!=width || images[i].getHeight()!=height) {
                error("The source images or stacks must have the same width and height.");
                return;
            }

            // do projection and upload to matrix
            if(i>=wList.length){
                break;
            }else {
                projectionStacks[i] = applyStackManifold(img.getStack(), images[0]);
                projectionStacks[i].show();
            }
        }
    }

    private String[] getInitialNamesAllstacks(String[] titles) {
        String[] names = new String[maxChannels];
        for (int i=0; i<maxChannels; i++)
            names[i] = getNameStack(i+1, titles);
        return names;
    }

    private String getNameStack(int channel, String[] titles) {
        if (autoFillDisabled)
            return none;
        String str = "C"+channel;
        String name = null;
        for (int i=titles.length-1; i>=0; i--) {
            if (titles!=null && titles[i].startsWith(str) && (firstChannelName==null||titles[i].contains(firstChannelName))) {
                name = titles[i];
                if (channel==1)
                    firstChannelName = name.substring(3);
                break;
            }
        }
        if (name==null) {
            for (int i=titles.length-1; i>=0; i--) {
                int index = titles[i].indexOf(colors[channel-1]);
                if (titles!=null && index!=-1 && (firstChannelName==null||titles[i].contains(firstChannelName))) {
                    name = titles[i];
                    if (channel==1 && index>0)
                        firstChannelName = name.substring(0, index-1);
                    break;
                }
            }
        }
        if (channel==1 && name==null)
            autoFillDisabled = true;
        if (name!=null)
            return name;
        else
            return none;
    }

    public ImagePlus applyStackManifold(ImageStack imStack, ImagePlus manifold){
        int dimW            =   imStack.getWidth();
        int dimH            =   imStack.getHeight();

        RealMatrix projMnold    = MatrixUtils.createRealMatrix(SME_ENS_Utils.convertFloatMatrixToDoubles(manifold.getProcessor().getFloatArray(),dimW,dimH)).transpose();

        for(int j=0;j<dimH;j++){
            for(int i=0;i<dimW;i++){
                int zIndex = ((int) Math.round(stackSize*(projMnold.getEntry(j,i)/255)));
                projMnold.setEntry (j,i,imStack.getVoxel(i,j,zIndex-1));
            }
        }

        float[][] mfoldFlaot = SME_ENS_Utils.convertDoubleMatrixToFloat(projMnold.transpose().getData(),dimW,dimH);
        ImagePlus smeManifold = new ImagePlus("",((ImageProcessor) new FloatProcessor(mfoldFlaot)));

        return(smeManifold);
    }

    void error(String msg) {
        IJ.error("Merge Channels", msg);
    }
}