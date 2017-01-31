package SME_PROJECTION_SRC;

import ij.ImagePlus;

import javax.swing.*;
import java.awt.*;

/**
 * Created by rexhepaj on 16/03/16.
 */
public class SME_ENS_Image_Component extends JComponent {
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
    private int drawWidth = 200;
    private int drawHeight = 200;
    private int methodProject = 0;
    /**
     * Constructor to build the image for the display
     * @param imstk
     */
    public SME_ENS_Image_Component(ImagePlus imstk, Boolean imProj, int dWidth, int dHeight, int projMeth){
        imp = imstk;
        projectImage = imProj;
        drawHeight = dHeight;
        drawWidth = dWidth;
        methodProject = projMeth;
        stk2im(imp, projMeth);
    }

    /**
     * Method projecting a multilayer stack image into a single layer by MAX projection
     * @return
     */
    private void stk2im(ImagePlus imstack, int projMeth){
        SME_ENS_Image_Prepare sipStk = new SME_ENS_Image_Prepare(imp,true);
        imShow = sipStk.getImageFromProjection(projMeth);
    }

    public void paintComponent (Graphics g){
        if(imShow == null) return;
        int imageWidth = imShow.getWidth(this);
        int imageHeight = imShow.getHeight(this);

        g.drawImage(imShow, drawWidth, drawHeight, this);

        for (int i = 0; i*imageWidth <= getWidth(); i++)
            for(int j = 0; j*imageHeight <= getHeight();j++)
                if(i+j>0) g.copyArea(0, 0, imageWidth, imageHeight, i*imageWidth, j*imageHeight);
    }

    public Image getIm2Show(){
        return imShow;
    }
}