package SME_PROJECTION_SRC;

import ij.process.FloatProcessor;

/**
 * Created by rexhepaj on 16/03/16.
 *
 * Compute min intensity projection.
 * */

class SME_ENS_Projection_Min_Intensity extends SME_ENS_Projection_Function {
    private float[] fpixels;
    private int len;

    /** Simple constructor since no preprocessing is necessary. */
    public SME_ENS_Projection_Min_Intensity(FloatProcessor fp) {
        fpixels = (float[])fp.getPixels();
        len = fpixels.length;
        for (int i=0; i<len; i++)
            fpixels[i] = Float.MAX_VALUE;
    }

    public void projectSlice(byte[] pixels) {
        for(int i=0; i<len; i++) {
            if((pixels[i]&0xff)<fpixels[i])
                fpixels[i] = (pixels[i]&0xff);
        }
    }

    public void projectSlice(short[] pixels) {
        for(int i=0; i<len; i++) {
            if((pixels[i]&0xffff)<fpixels[i])
                fpixels[i] = pixels[i]&0xffff;
        }
    }

    public void projectSlice(float[] pixels) {
        for(int i=0; i<len; i++) {
            if(pixels[i]<fpixels[i])
                fpixels[i] = pixels[i];
        }
    }

} // end MaxIntensity