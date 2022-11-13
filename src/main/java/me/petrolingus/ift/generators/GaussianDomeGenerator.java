package me.petrolingus.ift.generators;

import java.util.List;

public class GaussianDomeGenerator {

    private final List<Dome> domes;

    public GaussianDomeGenerator(List<Dome> domes) {
        this.domes = domes;
    }

    public double[][] getPixels(int width, int height) {

        double hx = 2.0 / (width - 1);
        double hy = 2.0 / (height - 1);

        // Generate pixels data
        double[][] pixels = new double[height][width];
        for (int i = 0; i < height; i++) {
            double y = -1.0 + i * hy;
            for (int j = 0; j < width; j++) {
                double x = -1.0 + j * hx;
                for (Dome dome : domes) {
                    pixels[i][j] += dome.getValue(x, y);
                }
            }
        }

        // Search min and max values
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                min = Math.min(min, pixels[i][j]);
                max = Math.max(max, pixels[i][j]);
            }
        }

        // Normalize data from 0 to 1
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                pixels[i][j] = (pixels[i][j] - min) / (max - min);
            }
        }

        return pixels;
    }

    public static class Dome {
        public double a;
        public double cx;
        public double cy;
        public double sx;
        public double sy;

        public double getValue(double x, double y) {
            double sxsx2 = 2.0 * sx * sx;
            double sysy2 = 2.0 * sy * sy;
            return a * Math.exp(-(Math.pow(x - cx, 2) / sxsx2 + Math.pow(y - cy, 2) / sysy2));
        }
    }
}
