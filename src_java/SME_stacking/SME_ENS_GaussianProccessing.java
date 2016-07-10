package ij.plugin.filter.SME_PROJECTION_SRC;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.GaussianBlur;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * Created by eltonr on 19/03/16.
 */
public class SME_ENS_GaussianProccessing {

    private SME_Plugin_Get_Manifold sme_pluginGetManifold = null;

    public SME_ENS_GaussianProccessing(SME_Plugin_Get_Manifold refplugin){
        sme_pluginGetManifold = refplugin;
    }

    /**
     * Create the filtered images
     *
     * @param stack2: new duplicated stack image from the Original Stack Image
     * @param sigmaX  : Sigma value for the X axis
     * @param sigmaY  : Sigma value for the Y axis
     * @return : returns the image obtained after applying the Gaussian Filter
     */

    public void Create_Gaussian_Image(ImageStack stack2, double sigmaX, double sigmaY) {

        int slice;                                                     // Define the type of slice
        int size3_ = stack2.getSize();                                 // Size of the stack image
        ImageProcessor ip_Gauss;                                       // Work on the duplicated images.
        GaussianBlur blurimg = new GaussianBlur();                     // Create ImageType
        ImageStack Gauss_Stack = stack2.duplicate();                   // We duplicate the original stack image .Create new empty stack to put the pixel values obtained after blurring blur

        for (slice = 1; slice <= size3_; slice++) {                    // Go through each slice
            ip_Gauss = Gauss_Stack.getProcessor(slice);                     // Returns an ImageProcessor for the specified slice using the Original stack image
            blurimg.blurGaussian(ip_Gauss, sigmaX, sigmaY, 0.01);      // Apply the blurring on the given slice
        }

        sme_pluginGetManifold.setStack1(Gauss_Stack);
        int size_   = sme_pluginGetManifold.getStack1().getSize();

        //Image display in new window
        sme_pluginGetManifold.setImp2( new ImagePlus("sML_" + sme_pluginGetManifold.getImp().getTitle(), sme_pluginGetManifold.getStack1()));
        sme_pluginGetManifold.getImp2().setStack(sme_pluginGetManifold.getStack1(), 1, size_, 1);
        sme_pluginGetManifold.getImp2().setCalibration(sme_pluginGetManifold.getImp2().getCalibration());

        sme_pluginGetManifold.getImp2().show();

        //return Gauss_Stack;
    }


    public void Gaussian_Filter_(float[][] Map2DImage_rearranged) {
        sme_pluginGetManifold.setMap2DImage(Map2DImage_rearranged);
        int x,y,z,slice,i,j,w,sl,k1_;
        int k_=0;
        int i_=0;
        int j_=0;
        ImageProcessor ip_ = sme_pluginGetManifold.getStack1().getProcessor(1);  // Done just to have W and H of one image
        int W = ip_.getWidth();                      // Get the image width
        int H = ip_.getHeight();
        int Size_stack = sme_pluginGetManifold.getStack1().getSize();

        double[] array = new double[Size_stack];
        double[][] Image_AGF_reshape = new double[W * H][Size_stack];
        float[][] Image_AGF_final_projection_blur = new float[W][H];
        float[][] Image_AGF_final_projection = new float[W][H];
        for (i=0;i<W;i++) {
            for (j = 0; j < H; j++) {

                if (sme_pluginGetManifold.getMap2DImage()[i][j] == 0) { //Map2DImage is the image containing the new labels once relabeled (step done previously)
                    for (slice = 0; slice < Size_stack; slice++) {
                        array[slice] = sme_pluginGetManifold.getBlur2().getVoxel(i, j, slice);
                    }
                } else if (sme_pluginGetManifold.getMap2DImage()[i][j] == 1) {
                    for (slice = 0; slice < Size_stack; slice++) {
                        array[slice] = sme_pluginGetManifold.getBlur1().getVoxel(i, j, slice);
                    }
                }
                if (sme_pluginGetManifold.getMap2DImage()[i][j] == 2) {
                    for (slice = 0; slice < Size_stack; slice++) {
                        array[slice] = sme_pluginGetManifold.getBlur0().getVoxel(i, j, slice);
                    }
                }

                // reshape each array found previously and added in a new matrix which is Image_AGF_toreshape
                for (z = 0; z < Size_stack; z++) {
                    Image_AGF_reshape[i_][j_] = array[z];
                    j_=j_+1;
                }
                i_=i_+1;
                j_=0;
            }
        }

        // Find maximum value for each pixel in the stack = projection
        double[] vect_max2= new double[W*H];
        double[]index_j= new double[W*H];  // index where the maximum pixel value was taken


        //Looking for the max value for each pixel in among all the stacks
        for (i =0 ; i < W*H ; i++) {
            double largest_2 = Image_AGF_reshape[i][0];  //Consider the first value of the stack for one given pixel as the largest
            for (j=0 ; j < Size_stack; j++) {
                if (Image_AGF_reshape[i][j] > largest_2) // Compare the value contained in largest_2 to the next value
                    largest_2 = Image_AGF_reshape[i][j]; // if the new value is greater that the one contained in largest_2
                // than it will be considered as the new largest value
            }
            vect_max2[i]=largest_2; // all the maximum values are stored in this vector "vect_max2"
        }

        // Here we are looking for the stack index from where the max pixel was taken
        int f = 0;
        for (i =0 ; i < W*H ; i++) {
            for (j=0 ; j < Size_stack; j++) {
                if (Image_AGF_reshape[i][j] == vect_max2[i])
                    f=j;
            }
            index_j[i]=f;
            f=0;
        }

        //Image where the labels are represented = Map
        float [][] index_representation = new float[W][H];
        k1_=0;
        for (i=0;i<W;i++){
            for (j=0;j<H;j++){
                index_representation[i][j]=(float)index_j[k1_];
                k1_=k1_+1;
            }
        }


        // Add the new max values found of the corresponding blurred image in a new matrix
        for (x = 0; x < W; x++) {
            for (y = 0; y < H; y++) {
                Image_AGF_final_projection_blur[x][y] = (float) vect_max2[k_];
                k_=k_+1;
            }
        }

        // Add the new max values found of the corresponding original image in a new matrix using index_j vector to
        // extract the right pixel
        w=0;
        for (x = 0; x < W; x++) {
            for (y = 0; y < H; y++) {
                sl=(int)index_j[w];
                Image_AGF_final_projection[x][y] = (float) sme_pluginGetManifold.getStack().getVoxel(x,y,sl); // get the pixel of the original image at the right stack (getVoxel)
                // using index_j
                w=w+1;
            }
        }

        //Image displayed in a new window Image_AGF_final_projection_
        FloatProcessor fp5_ind = new FloatProcessor(index_representation);
        ImageProcessor ip5_ind = fp5_ind.convertToFloat();
        ImagePlus imp5_ind = new ImagePlus("Index_image"+ sme_pluginGetManifold.getImp().getTitle(),ip5_ind);
        ip5_ind.setFloatArray(index_representation);
        imp5_ind.setProcessor(ip5_ind);
        sme_pluginGetManifold.setImp5_ind(imp5_ind);
        //imp5_ind.show();

        //Image displayed in a new window Image_AGF_final_projection_blur
        FloatProcessor fp5 = new FloatProcessor(Image_AGF_final_projection_blur);
        ImageProcessor ip5 = fp5.convertToFloat();
        ImagePlus imp5 = new ImagePlus("BLUR_Final_Projected_Image"+ sme_pluginGetManifold.getImp().getTitle(),ip5);
        ip5.setFloatArray(Image_AGF_final_projection_blur);
        imp5.setProcessor(ip5);
        sme_pluginGetManifold.setImp5(imp5);
        //imp5.show();

        //Image displayed in a new window Image_AGF_final_projection_
        FloatProcessor fp6 = new FloatProcessor(Image_AGF_final_projection);
        ImageProcessor ip6 = fp6.convertToFloat();
        ImagePlus imp6 = new ImagePlus("Final_Projected_Image"+ sme_pluginGetManifold.getImp().getTitle(),ip6);
        ip6.setFloatArray(Image_AGF_final_projection);
        imp6.setProcessor(ip6);
        sme_pluginGetManifold.setImp6(imp6);
        imp6.show();
    }
}
