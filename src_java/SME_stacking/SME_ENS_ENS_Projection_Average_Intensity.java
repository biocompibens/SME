package ij.plugin.filter.SME_PROJECTION_SRC;

/**
 * Created by rexhepaj on 16/03/16.
 */

import ij.process.FloatProcessor;

/** Compute average intensity projection. */
class SME_ENS_ENS_Projection_Average_Intensity extends SME_ENS_Projection_Function {
    private float[] fpixels;
    private int num, len;

    /** Constructor requires number of slices to be
     projected. This is used to determine average at each
     pixel. */
    public SME_ENS_ENS_Projection_Average_Intensity(FloatProcessor fp, int num) {
        fpixels = (float[])fp.getPixels();
        len = fpixels.length;
        this.num = num;
    }

    public void projectSlice(byte[] pixels) {
        for(int i=0; i<len; i++)
            fpixels[i] += (pixels[i]&0xff);
    }

    public void projectSlice(short[] pixels) {
        for(int i=0; i<len; i++)
            fpixels[i] += pixels[i]&0xffff;
    }

    public void projectSlice(float[] pixels) {
        for(int i=0; i<len; i++)
            fpixels[i] += pixels[i];
    }

    public void postProcess() {
        float fnum = num;
        for(int i=0; i<len; i++)
            fpixels[i] /= fnum;
    }

} // end SME_ENS_ENS_Projection_Average_Intensity
