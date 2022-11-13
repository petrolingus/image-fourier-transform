package me.petrolingus.ift.core;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class ImageFourierTransform {

    private static final FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

    public static Complex[][] fft(double[][] pixels) {

        int width = pixels[0].length;
        int height = pixels.length;

        Complex[][] temp1 = new Complex[height][width];
        for (int i = 0; i < height; i++) {
            temp1[i] = fft.transform(pixels[i], TransformType.FORWARD);
        }

        Complex[][] temp2 = new Complex[height][width];
        for (int i = 0; i < width; i++) {
            Complex[] col = new Complex[height];
            for (int j = 0; j < height; j++) {
                col[j] = temp1[j][i];
            }
            Complex[] transform = fft.transform(col, TransformType.FORWARD);
            for (int j = 0; j < height; j++) {
                temp2[j][i] = transform[j];
            }
        }

        return temp2;
    }

    public static Complex[][] ifft(Complex[][] pixels) {

        int width = pixels[0].length;
        int height = pixels.length;

        Complex[][] temp1 = new Complex[height][width];
        for (int i = 0; i < height; i++) {
            temp1[i] = fft.transform(pixels[i], TransformType.INVERSE);
        }

        Complex[][] temp2 = new Complex[height][width];
        for (int i = 0; i < width; i++) {
            Complex[] col = new Complex[height];
            for (int j = 0; j < height; j++) {
                col[j] = temp1[j][i];
            }
            Complex[] transform = fft.transform(col, TransformType.INVERSE);
            for (int j = 0; j < height; j++) {
                temp2[j][i] = transform[j];
            }
        }

        return temp2;
    }

}
