package me.petrolingus.ift.math;

public class Interpolation {

    public static double lerp(double s, double e, double t) {
        return s + (e - s) * t;
    }

    public static double blerp(double c00, double c10, double c01, double c11, double tx, double ty) {
        return lerp(lerp(c00, c10, tx), lerp(c01, c11, tx), ty);
    }
}
