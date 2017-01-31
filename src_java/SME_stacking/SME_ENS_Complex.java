package SME_PROJECTION_SRC;

/******************************************************************************
 *  Compilation:  javac SME_ENS_Complex.java
 *  Execution:    java SME_ENS_Complex
 *
 *  Data type for complex numbers.
 *
 *  The data type is "immutable" so once you create and initialize
 *  a SME_ENS_Complex object, you cannot change it. The "final" keyword
 *  when declaring re and im enforces this rule, making it a
 *  compile-time error to change the .re or .im fields after
 *  they've been initialized.
 ******************************************************************************/

public class SME_ENS_Complex {
    private final double re;   // the real part
    private final double im;   // the imaginary part

    // create a new object with the given real and imaginary parts
    public SME_ENS_Complex(double real, double imag) {
        re = real;
        im = imag;
    }

    // return a string representation of the invoking SME_ENS_Complex object
    public String toString() {
        if (im == 0) return re + "";
        if (re == 0) return im + "i";
        if (im <  0) return re + " - " + (-im) + "i";
        return re + " + " + im + "i";
    }

    // return abs/modulus/magnitude and angle/phase/argument
    public double abs()   { return Math.hypot(re, im); }  // Math.sqrt(re*re + im*im)
    public double phase() { return Math.atan2(im, re); }  // between -pi and pi

    // return a new SME_ENS_Complex object whose value is (this + b)
    public SME_ENS_Complex plus(SME_ENS_Complex b) {
        SME_ENS_Complex a = this;             // invoking object
        double real = a.re + b.re;
        double imag = a.im + b.im;
        return new SME_ENS_Complex(real, imag);
    }

    // return a new SME_ENS_Complex object whose value is (this - b)
    public SME_ENS_Complex minus(SME_ENS_Complex b) {
        SME_ENS_Complex a = this;
        double real = a.re - b.re;
        double imag = a.im - b.im;
        return new SME_ENS_Complex(real, imag);
    }

    // return a new SME_ENS_Complex object whose value is (this * b)
    public SME_ENS_Complex times(SME_ENS_Complex b) {
        SME_ENS_Complex a = this;
        double real = a.re * b.re - a.im * b.im;
        double imag = a.re * b.im + a.im * b.re;
        return new SME_ENS_Complex(real, imag);
    }

    // scalar multiplication
    // return a new object whose value is (this * alpha)
    public SME_ENS_Complex times(double alpha) {
        return new SME_ENS_Complex(alpha * re, alpha * im);
    }

    // return a new SME_ENS_Complex object whose value is the conjugate of this
    public SME_ENS_Complex conjugate() {  return new SME_ENS_Complex(re, -im); }

    // return a new SME_ENS_Complex object whose value is the reciprocal of this
    public SME_ENS_Complex reciprocal() {
        double scale = re*re + im*im;
        return new SME_ENS_Complex(re / scale, -im / scale);
    }

    // return the real or imaginary part
    public double re() { return re; }
    public double im() { return im; }

    // return a / b
    public SME_ENS_Complex divides(SME_ENS_Complex b) {
        SME_ENS_Complex a = this;
        return a.times(b.reciprocal());
    }

    // return a new SME_ENS_Complex object whose value is the complex exponential of this
    public SME_ENS_Complex exp() {
        return new SME_ENS_Complex(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
    }

    // return a new SME_ENS_Complex object whose value is the complex sine of this
    public SME_ENS_Complex sin() {
        return new SME_ENS_Complex(Math.sin(re) * Math.cosh(im), Math.cos(re) * Math.sinh(im));
    }

    // return a new SME_ENS_Complex object whose value is the complex cosine of this
    public SME_ENS_Complex cos() {
        return new SME_ENS_Complex(Math.cos(re) * Math.cosh(im), -Math.sin(re) * Math.sinh(im));
    }

    // return a new SME_ENS_Complex object whose value is the complex tangent of this
    public SME_ENS_Complex tan() {
        return sin().divides(cos());
    }

    // a static version of plus
    public static SME_ENS_Complex plus(SME_ENS_Complex a, SME_ENS_Complex b) {
        double real = a.re + b.re;
        double imag = a.im + b.im;
        SME_ENS_Complex sum = new SME_ENS_Complex(real, imag);
        return sum;
    }
}
