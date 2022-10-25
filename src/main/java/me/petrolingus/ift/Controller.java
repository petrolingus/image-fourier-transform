package me.petrolingus.ift;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;

public class Controller {

    public StackPane originalImagePane;

    public ImageView imageView = new ImageView();

    public void initialize() {

        originalImagePane.getChildren().add(imageView);

    }

    public void onOpenImage() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("C:\\Users\\petro\\IdeaProjects\\image-fourier-transform\\src\\main\\resources\\images"));
        File file = fileChooser.showOpenDialog(originalImagePane.getScene().getWindow());
        if (file != null) {
            Image image = new Image(file.getAbsolutePath());
            imageView.setImage(image);
            imageView.setFitWidth(originalImagePane.getWidth() - 2);
            imageView.setFitHeight(originalImagePane.getHeight() - 2);
        }

    }
}
