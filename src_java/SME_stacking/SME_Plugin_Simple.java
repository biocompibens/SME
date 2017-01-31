package SME_PROJECTION_SRC;

import ij.*;
import ij.gui.GenericDialog;
import ij.plugin.ChannelSplitter;
import ij.plugin.PlugIn;
import ij.plugin.RGBStackMerge;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by rexhepaj on 22/04/16.
 */
public class SME_Plugin_Simple implements PlugIn {
    private ImagePlus imp;
    private ImagePlus rawImage;
    private ImagePlus projectedImage;
    private ImagePlus manifold;
    private ImagePlus[] images;

    private static String none = "Red channel";
    private static int maxChannels = 4;
    private static int nmbChannels = 3;
    private static String[] colors = {"Channel1 stack1 to appy the manifold",
            "Channel2 stack1 to appy the manifold",
            "Channel3 stack1 to appy the manifold",
            "Channel4 stack1 to appy the manifold"};

    private static boolean staticCreateComposite = true;
    private static boolean staticKeep;
    private static boolean staticIgnoreLuts;
    private ImagePlus manifoldModel;
    private byte[] blank;
    private boolean ignoreLuts;
    private boolean autoFillDisabled;
    private String firstChannelName;
    private ImagePlus[] projectionStacks ;
    private int stackSize = 0;
    private int width = 0;
    private int height = 0;
    private SME_Plugin_Get_Manifold smePlugin;

    public void run(String arg) {
        processChannelsManifold();
    }

    public void getManifold(int indexChannel){
        smePlugin = new SME_Plugin_Get_Manifold();
        smePlugin.initProgressBar();

        smePlugin.setup("Manifold channel",images[indexChannel]);
        smePlugin.runSimple(false);


        //IJ.showStatus("Running SML");
        runSmlStep();
        smePlugin.updateProgressbar(0.1);
        //IJ.showStatus("Running KMEANS");
        runKmeansStep();
        smePlugin.updateProgressbar(0.3);
        //IJ.showStatus("Running Energy Optimisation");
        runEnoptStep();

        //IJ.showStatus("Finished");
    }

    public void runSmlStep(){
        smePlugin.runSml(false);

    }

    public void runKmeansStep(){
        smePlugin.runKmeans(false);

    }

    public void runEnoptStep(){
        smePlugin.runEnergyOptimisation(false);
    }

    /** Combines up to seven grayscale stacks into one RGB or composite stack. */
    public void processChannelsManifold() {

        if (WindowManager.getCurrentImage()==null) {
            error("No images is selected.");
            return;
        }

        if(WindowManager.getCurrentImage().isHyperStack()){
            // hyperstack color
            try {
                processChannelsManifoldColors();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }else{
            // monochromatic image
            processChannelsManifoldSimple();
        }
    }

    public void processChannelsManifoldSimple() {
        images = new ImagePlus[1];
        images[0] = WindowManager.getCurrentImage();

        stackSize = 0;
        width = 0;
        height = 0;

        int slices = 0;
        int frames = 0;
        int i ;

        {
                i = 0;
                if(width<images[i].getWidth()){width = images[i].getWidth();}
                if(height<images[i].getHeight()){height = images[i].getHeight();}
                if(stackSize<images[i].getStackSize()){stackSize = images[i].getStackSize();}
        }


        if (width==0) {
            error("There must be at least one source image or stack.");
            return;
        }

        // run manifold extraction on the first channel
        getManifold(0);

        manifoldModel = smePlugin.getMfoldImage();
        //manifoldModel.show();
        smePlugin.getSmeImage().show();
        smePlugin.updateProgressbar(1);
    }

    public void processChannelsManifoldColors() throws NoSuchMethodException {
        ImagePlus hyperStackSME = WindowManager.getCurrentImage();
        images = ChannelSplitter.split(hyperStackSME);

        // get channel color ids
        Color[]  colors1 = new Color[images.length];
        for(int i=0;i<images.length;i++){
            hyperStackSME.setC(i+1);
            colors1[i] = (((CompositeImage) hyperStackSME).getChannelColor());
        }

        maxChannels         = images.length;
        projectionStacks    = new ImagePlus[maxChannels];
        String[] titles = new String[images.length];

        for(int i=0;i<images.length;i++){
            if(colors1[i].equals(Color.RED))
                titles[i] = "Channel-RED";
            else if(colors1[i].equals(Color.GREEN))
                titles[i] = "Channel-GREEN";
            else if(colors1[i].equals(Color.BLUE))
                titles[i] = "Channel-BLUE";
            else if(colors1[i].equals(Color.GRAY))
                titles[i] = "Channel-GRAY";
            else if(colors1[i].equals(Color.CYAN))
                titles[i] = "Channel-CYAN";
            else if(colors1[i].equals(Color.MAGENTA))
                titles[i] = "Channel-MAGENTA";
            else if(colors1[i].equals(Color.yellow))
                titles[i] = "Channel-YELLOW";
        }

        String[] names = titles;
        boolean createComposite = staticCreateComposite;
        boolean keep = staticKeep;
        ignoreLuts = staticIgnoreLuts;

        GenericDialog gd = new GenericDialog("SME Stacking");
        gd.addChoice("Extract manifold from", titles, titles[0]);

        //gd.addCheckbox("Create composite", createComposite);
        //gd.addCheckbox("Keep source images", keep);
        //gd.addCheckbox("Ignore source LUTs", ignoreLuts);
        gd.showDialog();
        if (gd.wasCanceled())
            return;

        int index = gd.getNextChoiceIndex();
        //images = new ImagePlus[maxChannels];

        stackSize = 0;
        width = 0;
        height = 0;

        int slices = 0;
        int frames = 0;

        stackSize = images[0].getStackSize();

        for(int i=0;i<images.length;i++) {
            Object pixVal = images[i].getStack().getPixels(1);
            Method setType = images[i].getStack().getClass().getDeclaredMethod("setType", Object.class);
            setType.setAccessible(true);
            try {
                setType.invoke(images[i].getStack(), pixVal);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        //for(int i=0;i<images.length;i++){
        //    Object pixVal = images[i].getStack().getPixels(1);
        //    images[i].getStack().setType(pixVal);
        //}

        // run manifold extraction on the first channel
        getManifold(index);

        manifoldModel = smePlugin.getMfoldImage();
        //manifoldModel.show();
        //smePlugin.getSmeImage().show();

        ArrayList<ImagePlus> listChannels = new ArrayList<>(1);
        for(int i=0; i<maxChannels; i++){
            if(images[i]==null) break;
            listChannels.add(images[i]);
        }

        List<ImagePlus> processedImages = listChannels.stream().
         map(channelIt ->{
         ImagePlus itIm =  applyStackManifold(((ImagePlus)channelIt).getStack(), manifoldModel);
         return itIm;})
         .collect(toList());

        ImagePlus[] vecChannels = new ImagePlus[images.length];

        for(int i=0; i<processedImages.size(); i++){
            if(images[i]==null) break;
            vecChannels[i]= processedImages.get(i);
        }

        RGBStackMerge channelMerger = new RGBStackMerge();
        ImagePlus mergedHyperstack  = channelMerger.mergeHyperstacks(vecChannels,false);
        mergedHyperstack.show();

        /*ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        CompletableFuture<List<ImagePlus>> processedImages =  CompletableFuture.supplyAsync(()->

                        listChannels.parallelStream().
                                map(channelIt ->{
                                    ImagePlus itIm =  applyStackManifold(((ImagePlus)channelIt).getStack(), manifoldModel);
                                    itIm.show();
                                    return itIm;})
                                .collect(toList()),
                forkJoinPool
        );*/

        smePlugin.updateProgressbar(1);
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
