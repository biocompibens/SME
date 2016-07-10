package ij.plugin.filter.SME_PROJECTION_SRC;

public class SME_ENS_Filter_FFT_ {


    // compute the FFT of x[], assuming its length is a power of 2
    public static SME_ENS_Complex[] fft(SME_ENS_Complex[] x) {
        int N = x.length;

        // base case
        if (N == 1) return new SME_ENS_Complex[] { x[0] };

        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) {
            throw new RuntimeException("N is not a power of 2");
        }

        // fft of even terms
        SME_ENS_Complex[] even = new SME_ENS_Complex[N/2];
        for (int k = 0; k < N/2; k++) {
            even[k] = x[2*k];
        }
        SME_ENS_Complex[] q = fft(even);

        // fft of odd terms
        SME_ENS_Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < N/2; k++) {
            odd[k] = x[2*k + 1];
        }
        SME_ENS_Complex[] r = fft(odd);

        // combine
        SME_ENS_Complex[] y = new SME_ENS_Complex[N];
        for (int k = 0; k < N/2; k++) {
            double kth = -2 * k * Math.PI / N;
            SME_ENS_Complex wk = new SME_ENS_Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = q[k].plus(wk.times(r[k]));
            y[k + N/2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }
}
