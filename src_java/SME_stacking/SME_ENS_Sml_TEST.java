package SME_PROJECTION_SRC;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.Convolver;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * Created by rexhepaj on 17/03/16.
 */
public class SME_ENS_Sml_TEST {

    private SME_Plugin_TEST sme_plugin = null;

    public SME_ENS_Sml_TEST(SME_Plugin_TEST refplugin){
        sme_plugin = refplugin;
    }

    /**
     * Applying the sML filter on the Original ImageStack
     *
     */

    public void applySML() {
        // TODO replace number of ImagePlus, ImageStack new objects
        // TODO update filtering according to the matlab code
        // TODO change holding matrix data from short processor to floatprocessor
        float[] M   = { -1, 2, -1};
        double[] hGDouble  = {0.00296901674395050, 0.0133062098910137,0.0219382312797146,0.0133062098910137,0.00296901674395050,
                        0.0133062098910137,0.0596342954361801,0.0983203313488458,0.0596342954361801,0.0133062098910137,
                        0.0219382312797146,0.0983203313488458,0.162102821637127,0.0983203313488458,0.0219382312797146,
                        0.0133062098910137,0.0596342954361801,0.0983203313488458,0.0596342954361801,0.0133062098910137,
                        0.00296901674395050,0.0133062098910137,0.0219382312797146,0.0133062098910137,0.00296901674395050};

        float[] hG          = SME_ENS_Utils.convertDoubleVecFloat(hGDouble);

        Convolver  convENS = new Convolver();

        ImagePlus imp_sml = sme_plugin.getImp().duplicate();

        ImageProcessor ip = imp_sml.getProcessor();
        sme_plugin.setStack1(imp_sml.getStack());                    // ImagePlus into ImageStack

        int W = ip.getWidth();                      // Get the image width
        int H = ip.getHeight();                     // Get the image height
        int i, j, slice;                            // Define the type of i, j and slice (equivalent to z axis)
        int size_ = sme_plugin.getStack1().getSize();               // Size of the stack image

        ImageStack smlResult = new ImageStack(W, H);

        for (slice = 1; slice <= size_; slice++) {  //Go through each slice

            // Work on the duplicated images.
            // TODO check if you can remove the dublicate call below
            FloatProcessor ip_copy1_X =new  FloatProcessor(ip.duplicate().getIntArray());
            FloatProcessor ip_copy1_Y =new  FloatProcessor(ip.duplicate().getIntArray());

            ip = sme_plugin.getStack1().getProcessor(slice);                     // Returns an ImageProcessor for the specified slice
            FloatProcessor ip_sum = new FloatProcessor(W, H);    //Create an empty ImageProcessor

            // Apply the convolution on the duplicated ImageProcessor

            convENS.convolveFloat(ip_copy1_X, hG, 5, 5);
            convENS.convolveFloat(ip_copy1_X, M, 3, 1);
            convENS.convolveFloat(ip_copy1_Y, hG, 5, 5);
            convENS.convolveFloat(ip_copy1_Y, M, 1, 3);

            //ip_copy1_X.convolve(hG, 5, 5);                // Make the convolution on the X axis
            //ip_copy1_Y.convolve(M, 3, 1);                // Make the convolution on the X axis

            //ip_copy1_X.convolve(hG, 5, 5);                // Make the convolution on the Y axis
            //ip_copy1_Y.convolve(M, 1, 3);                // Make the convolution on the Y axis

            for (i = 0; i < W; ++i) {
                for (j = 0; j < H; ++j) {
                    float a = ip_copy1_X.getf(i, j);                // get the pixel value in the resultant image after convolution (X axis)
                    float b = ip_copy1_Y.getf(i, j);                // get the pixel value in the resultant image after convolution (Y axis)
                    float sumPixl = Math.abs(a) +Math.abs(b);                             // add the 2 pixel values
                    ip_sum.putPixelValue(i, j, (double) sumPixl);                  // put the result of the addition in the newly created ImageProcessor
                }
            }

            smlResult.addSlice(ip_sum);         // Assigns a pixel array to the specified slice
        }

        sme_plugin.setStack1(smlResult);
        IJ.saveAsTiff(new ImagePlus("SMEresult",smlResult),"SMEtempresults.tiff");
        //Image display in new window
        sme_plugin.setImp2(new ImagePlus("sML_" + sme_plugin.getImp().getTitle(), sme_plugin.getStack1()));
        sme_plugin.getImp2().setStack(sme_plugin.getStack1(), 1, size_, 1);
        sme_plugin.getImp2().setCalibration(sme_plugin.getImp2().getCalibration());

        //sme_plugin.getImp2().show();
        sme_plugin.setSmlImage(sme_plugin.getImp2());
    }
}
