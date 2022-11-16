package me.petrolingus.ift;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.stage.FileChooser;
import me.petrolingus.ift.core.Algorithm;
import me.petrolingus.ift.core.FilterType;
import me.petrolingus.ift.core.ImageFourierTransform;
import me.petrolingus.ift.core.SpectrumType;
import me.petrolingus.ift.generators.GaussianDomeGenerator;
import me.petrolingus.ift.generators.GaussianDomeGenerator.Dome;
import me.petrolingus.ift.math.Interpolation;
import org.apache.commons.math3.complex.Complex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    public CheckBox autoResize;

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

    private static double[][] originalPixels;

    public void initialize() {

        ObservableList<FilterType> filterTypes = FXCollections.observableArrayList(
                FilterType.IDEAL_HIGH_PASS,
                FilterType.IDEAL_LOW_PASS
        );

        filterType.setItems(filterTypes);
        filterType.setValue(filterType.getItems().get(1));

        ObservableList<SpectrumType> spectrumTypes = FXCollections.observableArrayList(
                SpectrumType.LINEAR,
                SpectrumType.LOG_1P
        );

        spectrumType.setItems(spectrumTypes);
        spectrumType.setValue(spectrumType.getItems().get(1));

    }

    public void onGenerateImageButton() {

        int width = Integer.parseInt(generatedImageWidth.getText());
        int height = Integer.parseInt(generatedImageHeight.getText());

        Dome dome1 = createDome(a1, x1, y1, sx1, sy1);
        Dome dome2 = createDome(a2, x2, y2, sx2, sy2);
        Dome dome3 = createDome(a3, x3, y3, sx3, sy3);
        GaussianDomeGenerator generator = new GaussianDomeGenerator(List.of(dome1, dome2, dome3));

        originalPixels = generator.getPixels(width, height);
        originalImageView.setImage(getImageFromPixels(originalPixels));
        onProcessButton();
    }

    public void onLoadImageButton() {

        // Load image resource
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "\\IdeaProjects\\image-fourier-transform\\src\\main\\resources\\me\\petrolingus\\ift\\images"));
        File file = fileChooser.showOpenDialog(originalImageView.getScene().getWindow());

        if (file == null) {
            System.err.println("Image not found!");
            return;
        }

        // Read raw pixels
        double[][] rawPixels;
        try (FileInputStream inputStream = new FileInputStream(file.getPath())) {

            Image image = new Image(inputStream);
            originalImageView.setImage(image);
            originalImageView.setPreserveRatio(true);

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

        int width = Integer.parseInt(targetImageWidth.getText());
        int height = Integer.parseInt(targetImageHeight.getText());
        originalPixels = imageUpdateForFourier(rawPixels, width, height, autoResize.isSelected());
        onProcessButton();
    }

    public void onProcessButton() {
        process(originalPixels);
    }

    public double[][] imageUpdateForFourier(double[][] image, int targetWidth, int targetHeight, boolean autoSize) {

        int width = image[0].length;
        int height = image.length;
        int newWidth = 1;
        int newHeight = 1;

        if (autoSize) {
            while (newWidth < width) {
                newWidth *= 2;
            }
            while (newHeight < height) {
                newHeight *= 2;
            }
        } else {
            newWidth = targetWidth;
            newHeight = targetHeight;
        }

        System.out.println(newWidth + ":" + newHeight);

        double[][] updatedImage = new double[newHeight][newWidth];

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                double gx = (double) x / newWidth * (width - 1);
                double gy = (double) y / newHeight * (height - 1);
                int gxi = (int) gx;
                int gyi = (int) gy;
                try {
                    double v = Interpolation.blerp(image[gyi][gxi], image[gyi + 1][gxi], image[gyi][gxi + 1], image[gyi + 1][gxi + 1], gx - gxi, gy - gyi);
                    updatedImage[y][x] = v;
                } catch (Exception e) {
                    System.out.println(gxi + ":" + gyi);
                }

            }
        }

        return updatedImage;
    }

    private void process(double[][] originalPixels) {

        brightnessImageView.setImage(getImageFromPixels(originalPixels));

        // Generate noise
        double[][] noisedPixels = Algorithm.setNoise(originalPixels, Double.parseDouble(noiseLevel.getText()));
        noisedImageView.setImage(getImageFromPixels(noisedPixels));
        noisedImageView.setPreserveRatio(true);

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
