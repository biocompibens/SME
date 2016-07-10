package ij.plugin.filter.SME_PROJECTION_SRC;

import ij.process.FloatProcessor;

/**
 * Created by rexhepaj on 16/03/16.
 * Compute standard deviation projection.
 * */
class SME_ENS_Projection_STD_Intensity extends SME_ENS_Projection_Function {
    private float[] result;
    private double[] sum, sum2;
    private int num,len;

    public SME_ENS_Projection_STD_Intensity(FloatProcessor fp, int num) {
        result = (float[])fp.getPixels();
        len = result.length;
        this.num = num;
        sum = new double[len];
        sum2 = new double[len];
    }

    public void projectSlice(byte[] pixels) {
        int v;
        for(int i=0; i<len; i++) {
            v = pixels[i]&0xff;
            sum[i] += v;
            sum2[i] += v*v;
        }
    }

    public void projectSlice(short[] pixels) {
        double v;
        for(int i=0; i<len; i++) {
            v = pixels[i]&0xffff;
            sum[i] += v;
            sum2[i] += v*v;
        }
    }

    public void projectSlice(float[] pixels) {
        double v;
        for(int i=0; i<len; i++) {
            v = pixels[i];
            sum[i] += v;
            sum2[i] += v*v;
        }
    }

    public void postProcess() {
        double stdDev;
        double n = num;
        for(int i=0; i<len; i++) {
            if (num>1) {
                stdDev = (n*sum2[i]-sum[i]*sum[i])/n;
                if (stdDev>0.0)
                    result[i] = (float)Math.sqrt(stdDev/(n-1.0));
                else
                    result[i] = 0f;
            } else
                result[i] = 0f;
        }
    }

} // end StandardDeviation