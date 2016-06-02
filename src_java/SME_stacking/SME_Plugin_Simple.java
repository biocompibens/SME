package SME_PROJECTION_SRC;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.ChannelSplitter;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

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
        smePlugin.updateProgressbar(1);
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
        int[] wList         = WindowManager.getIDList();
        if (wList==null) {
            error("No images are open.");
            return;
        }else if(wList.length>1){
            error("There must be at most one image open: either 1) Mono-chromatic stack or 2) Stack of composite images (Hyperstack)");
            return;
        }

        if(WindowManager.getImage(wList[0]).isHyperStack()){
            // hyperstack color
            processChannelsManifoldColors();
        }else{
            // monochromatic image
            processChannelsManifoldSimple();
        }
    }

    public void processChannelsManifoldSimple() {
        int[] wList         = WindowManager.getIDList();
        images = new ImagePlus[1];

        stackSize = 0;
        width = 0;
        height = 0;

        int slices = 0;
        int frames = 0;
        int i ;

        {
                i = 0;
                images[i] = WindowManager.getImage(wList[i]);
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
    }

    public void processChannelsManifoldColors() {
        int[] wList         = WindowManager.getIDList();
        projectionStacks    = new ImagePlus[maxChannels];
        maxChannels         = 3;

        ImagePlus hyperStackSME = WindowManager.getImage(wList[0]);

        String[] titles = new String[3];

        titles[0] = "Red channel";
        titles[1] = "Green channel";
        titles[2] = "Blue channel";

        String[] names = titles;
        boolean createComposite = staticCreateComposite;
        boolean keep = staticKeep;
        ignoreLuts = staticIgnoreLuts;

        GenericDialog gd = new GenericDialog("SME Stacking");
        gd.addChoice("Extract manifold from", titles, "*none*");

        //gd.addCheckbox("Create composite", createComposite);
        //gd.addCheckbox("Keep source images", keep);
        //gd.addCheckbox("Ignore source LUTs", ignoreLuts);
        gd.showDialog();
        if (gd.wasCanceled())
            return;

        int index = gd.getNextChoiceIndex();
        images = new ImagePlus[maxChannels];

        stackSize = 0;
        width = 0;
        height = 0;

        int slices = 0;
        int frames = 0;

        images = ChannelSplitter.split(hyperStackSME);
        stackSize = images[0].getStackSize();

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

        /**List<ImagePlus> processedImages = listChannels.stream().
         map(channelIt ->{
         ImagePlus itIm =  applyStackManifold(((ImagePlus)channelIt).getStack(), manifoldModel);
         //itIm.show();
         return itIm;})
         .collect(toList());**/

        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        CompletableFuture<List<ImagePlus>> processedImages =  CompletableFuture.supplyAsync(()->

                        listChannels.parallelStream().
                                map(channelIt ->{
                                    ImagePlus itIm =  applyStackManifold(((ImagePlus)channelIt).getStack(), manifoldModel);
                                    itIm.show();
                                    return itIm;})
                                .collect(toList()),
                forkJoinPool
        );
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
