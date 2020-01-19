import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;

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

    public void numbers(ActionEvent actionEvent) {  // ask Robertson do we rlly need these action events??
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
        Game game = new Game(new Stage(), 3);
        Stage stage = (Stage) newGame.getScene().getWindow();
        stage.hide();
    }

    public void fourGame() {
        Game game = new Game(new Stage(), 4);
        Stage windw = (Stage) newGame.getScene().getWindow();
        windw.hide();
    }

    public void fiveGame() {
        Game game = new Game(new Stage(), 5);
        Stage windw = (Stage) newGame.getScene().getWindow();
        windw.hide();
    }

    public void selectImage() {
        desktop = Desktop.getDesktop();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pick your image");
        File defaultDirectory = new File("/Users/noahkiefer/IdeaProjects/GameOfFINISH/src/sample/images");
        fileChooser.setInitialDirectory(defaultDirectory);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );

        File file = fileChooser.showOpenDialog(new Stage());

        GameImage.puzzleImg = file.getName().replace(".jpg", "");

        GameImage game = new GameImage(new Stage());
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

        BufferedImage image = ImageIO.read(file);

        System.out.println("image has been received. Height is: " + image.getHeight());

        GameUpload game = new GameUpload(new Stage(), image);
        Stage stage = (Stage) uploadImage.getScene().getWindow();
        stage.hide();
    }

    public void leaderboard(ActionEvent actionEvent) {
        try {
            GoogleLeaderboard board = new GoogleLeaderboard(new Stage());
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }


}
