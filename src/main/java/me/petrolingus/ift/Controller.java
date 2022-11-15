package me.petrolingus.ift;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import me.petrolingus.ift.core.Algorithm;
import me.petrolingus.ift.core.FilterType;
import me.petrolingus.ift.core.ImageFourierTransform;
import me.petrolingus.ift.core.SpectrumType;
import me.petrolingus.ift.generators.GaussianDomeGenerator;
import me.petrolingus.ift.generators.GaussianDomeGenerator.Dome;
import org.apache.commons.math3.complex.Complex;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class Controller {

    public TextField a1;
    public TextField x1;
    public TextField y1;
    public TextField sx1;
    public TextField sy1;

    public TextField a2;
    public TextField x2;
    public TextField y2;
    public TextField sx2;
    public TextField sy2;

    public TextField a3;
    public TextField x3;
    public TextField y3;
    public TextField sx3;
    public TextField sy3;

    public TextField generatedImageWidth;
    public TextField generatedImageHeight;

    public TextField targetImageWidth;
    public TextField targetImageHeight;

    public TextField noiseLevel;
    public TextField threshold;
    public ChoiceBox<FilterType> filterType;
    public ChoiceBox<SpectrumType> spectrumType;
    public TextField epsilon1;
    public TextField epsilon2;

    public ImageView originalImageView;
    public ImageView brightnessImageView;
    public ImageView noisedImageView;
    public ImageView spectrumImageView;
    public ImageView filteredImageView;
    public ImageView restoredImageView;

    public void initialize() {

        ObservableList<FilterType> filterTypes = FXCollections.observableArrayList(
                FilterType.IDEAL_HIGH_PASS,
                FilterType.IDEAL_LOW_PASS
        );

        filterType.setItems(filterTypes);
        filterType.setValue(filterType.getItems().get(0));

        ObservableList<SpectrumType> spectrumTypes = FXCollections.observableArrayList(
                SpectrumType.LINEAR,
                SpectrumType.LOG_1P
        );

        spectrumType.setItems(spectrumTypes);
        spectrumType.setValue(spectrumType.getItems().get(0));

    }

    public void onGenerateImageButton() {

        int width = Integer.parseInt(generatedImageWidth.getText());
        int height = Integer.parseInt(generatedImageHeight.getText());

        Dome dome1 = createDome(a1, x1, y1, sx1, sy1);
        Dome dome2 = createDome(a2, x2, y2, sx2, sy2);
        Dome dome3 = createDome(a3, x3, y3, sx3, sy3);
        GaussianDomeGenerator generator = new GaussianDomeGenerator(List.of(dome1, dome2, dome3));

        double[][] originalPixels = generator.getPixels(width, height);
        originalImageView.setImage(getImageFromPixels(originalPixels));
        process(originalPixels);
    }

    public void onLoadImageButton() {

        // Load image resource
        URL resource = Main.class.getResource("images/aqua256.jpg");
        if (resource == null) {
            System.err.println("Image not found!");
            return;
        }

        // Read raw pixels
        double[][] rawPixels;
        try (FileInputStream inputStream = new FileInputStream(resource.getPath())) {

            Image image = new Image(inputStream);
            originalImageView.setImage(image);
            originalImageView.setPreserveRatio(false);

            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            rawPixels = new double[height][width];

            PixelReader pixelReader = image.getPixelReader();
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    rawPixels[i][j] = pixelReader.getColor(j, i).getBrightness();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO: rawPixels bilinear interpolation

        int width = Integer.parseInt(targetImageWidth.getText());
        int height = Integer.parseInt(targetImageHeight.getText());
        double[][] originalPixels = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                originalPixels[i][j] = rawPixels[i][j];
            }
        }

        process(originalPixels);
    }

    private void process(double[][] originalPixels) {

        brightnessImageView.setImage(getImageFromPixels(originalPixels));

        // Generate noise
        double[][] noisedPixels = Algorithm.setNoise(originalPixels, Double.parseDouble(noiseLevel.getText()));
        noisedImageView.setImage(getImageFromPixels(noisedPixels));

        // Calculate spectrum
        Complex[][] spectrum = Algorithm.shuffleQuarters(ImageFourierTransform.fft(noisedPixels));
        double[][] spectrumPixels = Algorithm.transformComplexPixels(spectrum, spectrumType.getValue());
        spectrumImageView.setImage(getImageFromPixels(spectrumPixels));

        // Filtering spectrum
        Complex[][] filteredSpectrum;
        if (filterType.getValue() == FilterType.IDEAL_HIGH_PASS) {
            filteredSpectrum = Algorithm.highPassFiltering(spectrum, Double.parseDouble(threshold.getText()));
        } else {
            filteredSpectrum = Algorithm.lowPassFiltering(spectrum, Double.parseDouble(threshold.getText()));
        }
        double[][] filteredSpectrumPixels = Algorithm.transformComplexPixels(filteredSpectrum, spectrumType.getValue());
        filteredImageView.setImage(getImageFromPixels(filteredSpectrumPixels));

        // Restore image
        Complex[][] restored = ImageFourierTransform.ifft(filteredSpectrum);
        double[][] restoredPixels = Algorithm.transformComplexPixels(restored, SpectrumType.LINEAR);
        restoredImageView.setImage(getImageFromPixels(restoredPixels));

        // Calculation epsilon1
        epsilon1.setText(Double.toString(calculateEpsilon(originalPixels, noisedPixels)));
        epsilon2.setText(Double.toString(calculateEpsilon(originalPixels, restoredPixels)));
    }

    private double calculateEpsilon(double[][] originalPixels, double[][] processedPixels) {

        int w = originalPixels[0].length;
        int h = originalPixels.length;

        double numerator = 0;
        double denominator = 0;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                double a = originalPixels[i][j];
                double b = processedPixels[i][j];
                numerator += (a - b) * (a - b);
                denominator += a * a;
            }
        }

        return numerator / denominator;
    }

    private Dome createDome(TextField a, TextField cx, TextField cy, TextField sx, TextField sy) {

        Dome dome = new Dome();
        dome.a = Double.parseDouble(a.getText());
        dome.cx = Double.parseDouble(cx.getText());
        dome.cy = Double.parseDouble(cy.getText());
        dome.sx = Double.parseDouble(sx.getText());
        dome.sy = Double.parseDouble(sy.getText());

        return dome;
    }

    private Image getImageFromPixels(double[][] pixels) {
        int width = pixels[0].length;
        int height = pixels.length;
        int[] buffer = new int[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int y = (int) Math.round(255 * pixels[i][j]);
                buffer[width * i + j] = 0xFF << 24 | y << 16 | y << 8 | y;
            }
        }
        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();
        pixelWriter.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), buffer, 0, width);
        return image;
    }
}
