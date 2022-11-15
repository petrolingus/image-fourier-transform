package me.petrolingus.ift.core;

import org.apache.commons.math3.complex.Complex;

import java.util.concurrent.ThreadLocalRandom;

public class Algorithm {

    public static double[][] setNoise(double[][] pixels, double noiseLevel) {

        int width = pixels[0].length;
        int height = pixels.length;

        // Generate noise data
        double[][] noise = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                noise[i][j] = ThreadLocalRandom.current().nextGaussian();
            }
        }

        // Calculate noise energy
        double noiseEnergy = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                noiseEnergy += noise[i][j] * noise[i][j];
            }
        }

        // Calculate image energy
        double imageEnergy = 0;
        for (double[] row : pixels) {
            for (int j = 0; j < width; j++) {
                imageEnergy += row[j] * row[j];
            }
        }

        // Calculate noise multiplier
        double alpha = Math.sqrt(imageEnergy / noiseEnergy / Math.pow(10, 0.1 * noiseLevel));

        // Apply noise
        double[][] result = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result[i][j] = pixels[i][j] + alpha * noise[i][j];
            }
        }

        return normalize(result);
    }

    public static double[][] normalize(double[][] pixels) {

        int width = pixels[0].length;
        int height = pixels.length;

        // Search min&max values for normalization
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (double[] row : pixels) {
            for (int j = 0; j < width; j++) {
                max = Math.max(max, row[j]);
                min = Math.min(min, row[j]);
            }
        }

        // Normalization of image values
        double[][] result = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result[i][j] = (pixels[i][j] - min) / (max - min);
            }
        }

        return result;
    }

    public static double[][] transformComplexPixels(Complex[][] pixels, SpectrumType spectrumType) {

        int width = pixels[0].length;
        int height = pixels.length;

        double[][] result = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (spectrumType == SpectrumType.LINEAR) {
                    result[i][j] = pixels[i][j].abs();
                } else {
                    result[i][j] = Math.log1p(pixels[i][j].abs());
                }
            }
        }

        return Algorithm.normalize(result);
    }

    public static Complex[][] shuffleQuarters(Complex[][] pixels) {

        int width = pixels[0].length;
        int height = pixels.length;
        int hw = width / 2;
        int hh = height / 2;

        Complex[][] result = new Complex[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int x = (j < hw) ? hw + j : j - hw;
                int y = (i < hh) ? hh + i : i - hh;
                result[i][j] = pixels[y][x];
            }
        }

        return result;
    }

    // The ideal lowpass filter is used to cut off all the high-frequency components of Fourier transformation.
    public static Complex[][] lowPassFiltering(Complex[][] pixels, double threshold) {
        return idealPassFilter(pixels, threshold, true);
    }

    // The ideal highpass filter is used to cut off all the low-frequency components of Fourier transformation.
    public static Complex[][] highPassFiltering(Complex[][] pixels, double threshold) {
        return idealPassFilter(pixels, threshold, false);
    }

    private static Complex[][] idealPassFilter(Complex[][] pixels, double threshold, boolean isLowpass) {

        int width = pixels[0].length;
        int height = pixels.length;
        int maxSize = Math.max(width, height);

        Complex[][] result = new Complex[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double d = Math.sqrt(Math.pow(i - height / 2.0, 2) + Math.pow(j - width / 2.0, 2));
                if (isLowpass) {
                    result[i][j] = d > (0.5 * maxSize * threshold) ? Complex.ZERO : pixels[i][j];
                } else {
                    result[i][j] = d < (0.5 * maxSize * threshold) ? Complex.ZERO : pixels[i][j];
                }
            }
        }

        return result;
    }

}
