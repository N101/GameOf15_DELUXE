import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller {
    public Button newGame;
    public Button leaderboard;
    public Button numbers;
    public Button three;
    public Button four;
    public Button five;

    public Button images;
    public Button selectImage;
    public Button uploadImage;

    private Desktop desktop = null;

    public void newGame() {
        newGame.setVisible(false);
        leaderboard.setVisible(false);
        numbers.setVisible(true);
        images.setVisible(true);

    }

    public void numbers() {  // ask Robertson do we rlly need these action events??
        numbers.setVisible(false);
        images.setVisible(false);
        three.setVisible(true);
        four.setVisible(true);
        five.setVisible(true);
    }

    public void images() {
        numbers.setVisible(false);
        images.setVisible(false);
        selectImage.setVisible(true);
        uploadImage.setVisible(true);
    }

    public void threeGame() {
        new Game(new Stage(), 3);
        Stage stage = (Stage) newGame.getScene().getWindow();
        stage.hide();
    }

    public void fourGame() {
        new Game(new Stage(), 4);
        Stage windw = (Stage) newGame.getScene().getWindow();
        windw.hide();
    }

    public void fiveGame() {
        new Game(new Stage(), 5);
        Stage windw = (Stage) newGame.getScene().getWindow();
        windw.hide();
    }

    public void selectImage() {
        desktop = Desktop.getDesktop();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pick your image");
        File defaultDirectory = new File("/Users/noahkiefer/IdeaProjects/GameOf15_DELUXE/src/main/resources/images");
        fileChooser.setInitialDirectory(defaultDirectory);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );

        File file = fileChooser.showOpenDialog(new Stage());

        // Open the image in preview so the player can see what the final puzzle should look like
        if (file != null) {
            try {
                desktop.open(file);
            } catch (IOException ex) {
                Logger.getLogger(
                        Controller.class.getName()).log(
                        Level.SEVERE, null, ex
                );
            }
        }

        GameImage.puzzleImg = file.getName().replace(".jpg", "");

        new GameImage(new Stage());
        Stage stage = (Stage) selectImage.getScene().getWindow();
        stage.hide();
    }

    public void uploadImage() throws IOException {
        desktop = Desktop.getDesktop();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pick your image");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("All Images", "*.*")
        );

        File file = fileChooser.showOpenDialog(new Stage());
        BufferedImage image = ImageIO.read(file);

        if (image.getHeight() > 500 || image.getWidth() > 500) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Image must be smaller than 500x500");
            a.showAndWait();
            uploadImage();
        } else {
            // Open the image in preview so the player can see what the final puzzle should look like
            if (file != null) {
                try {
                    desktop.open(file);
                } catch (IOException ex) {
                    Logger.getLogger(
                            Controller.class.getName()).log(
                            Level.SEVERE, null, ex
                    );
                }
            }

            System.out.println(file.getAbsolutePath());

            System.out.println("image has been received. Height is: " + image.getHeight());

            new GameUpload(new Stage(), image);
            Stage stage = (Stage) uploadImage.getScene().getWindow();
            stage.hide();
        }
    }

    public void leaderboard() {
        try {
            new GoogleLeaderboard(new Stage());
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }


}
