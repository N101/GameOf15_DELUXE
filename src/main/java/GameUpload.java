import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameUpload  {
    private Btn[] tiles;
    private final int gridW = 4;
    private int[][] numbers;
    private boolean gameOver = false;
    private String moveDir = "";
    private Image[][] images;
    private final BufferedImage gameImage;

    private Label timer = new Label();
    private Duration time = Duration.ZERO;
    private Timeline timeline;

    public GameUpload(Stage primaryStage, BufferedImage gameImage)  {
        this.gameImage = gameImage;

        numbers = new int[4][4];
        tiles = new Btn[16];
        images = new Image[4][4];

        primaryStage.setTitle("Game of 15");
        newGame();
        primaryStage.setScene(createTheScene());
        primaryStage.show();
    }

    private Scene createTheScene() {
        BorderPane bp = new BorderPane();
        bp.getStylesheets().add("/../resources/StyleClass.css");

        Button btn = new Button("new game");
        btn.setPrefSize(100, 50);
        btn.setId("outside");
        Button back = new Button("Home");
        back.setId("outside");
        back.setPrefSize(100,50);

        VBox vbox = new VBox(10, btn, back);

        bp.setLeft(vbox);
        btn.setOnAction(e -> {
            newGame();
            repaintNumbers();
        });
        back.setOnAction(e -> {
            Stage oldStage = (Stage) back.getScene().getWindow();
            oldStage.close();
            try {
                Parent root = FXMLLoader.load(getClass().getResource("TitleScreen.fxml"));
                Stage newStage = new Stage();
                newStage.setTitle("Game of 15");
                newStage.setScene(new Scene(root, 800, 600));
                newStage.show();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        timer.setId("timer");
        timer.setPrefWidth(40);
        timer.setAlignment(Pos.CENTER);
        bp.setRight(timer);

        bp.setCenter(gamePane());
        return new Scene(bp, 800, 630);
    }

    private Node gamePane() {
        GridPane grid = new GridPane();
        grid.setId("borderPane");
        grid.setPrefSize(600, 600);
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);

        int counter = 0;

        for (int i = 0; i <= gridW-1; i++) {
            for (int j = 0; j <= gridW-1; j++) {
                tiles[counter] = new Btn(j, i);
                tiles[counter].setId("ImageTiles");

                // So the buttons don't take up any unnecessary space and leave large spaces between the tiles
                tiles[counter].setMaxSize(gameImage.getWidth()/4, gameImage.getHeight()/4);
                tiles[counter].setMinSize(gameImage.getWidth()/4, gameImage.getHeight()/4);

                tiles[counter].setGraphic(new ImageView(images[i][j]));

                int finalI = i;
                int finalJ = j;

                tiles[counter].setOnAction(e -> {
                    if (gameOver) {
                        new Alert(Alert.AlertType.ERROR, "Game is done already! Start a new game").showAndWait();
                    } else if (isMoveLegal(finalJ, finalI)) {
                        move(finalJ, finalI);
                        repaintNumbers();
                        isGameDone();
                        printGrid();
                    }
                });

                grid.add(tiles[counter], j, i);

                counter++;
            }
        }
        repaintNumbers();

        return grid;
    }

    private void newGame() {
        cutImage();
        importImages();
        setBoard();
        printGrid();
        shuffleStart();
        printGrid();
        moveDir = "";
        gameOver = false;
        startTimer();
        time = Duration.ZERO;
    }

    private void setBoard() {
        int counter = 1;
        for (int i = 0; i <= gridW - 1; i++) {
            for (int j = 0; j <= gridW - 1; j++) {
                numbers[i][j] = counter;
                counter++;
            }
        }
        numbers[gridW-1][gridW-1] = 16;

    }

    private void printGrid() {
        StringBuilder word = new StringBuilder();
        for (int[] number : numbers) {
            for (int aNumber : number) {
                word.append(aNumber).append(", ");
            }
        }
    }

    private void shuffleStart() {
        Random ran = new Random();

        for (int i = numbers.length - 1; i > 0; i--) {
            for (int k = numbers[i].length - 1; k > 0; k--) {
                int m = ran.nextInt(i + 1);
                int n = ran.nextInt(k + 1);

                // shuffle numbers array
                int tmp = numbers[i][k];
                numbers[i][k] = numbers[m][n];
                numbers[m][n] = tmp;

                // shuffle the image array the same
                Image tmporary = images[i][k];
                images[i][k] = images[m][n];
                images[m][n] = tmporary;
            }
        }
        if(!isSolvable()) {
            shuffleStart();
        }
    }

    private boolean isSolvable() {
        int Inversions = 0;
        int blankPos = 0;
        List<Integer> ary = new ArrayList<>();

        for (int i = 0; i < numbers.length; i++) {
            for (int j = 0; j < numbers[i].length; j++) {
                ary.add(numbers[i][j]);
                if (numbers[i][j] == 16) {
                    blankPos = 4 - i;
                }
            }
        }

        for (int a = 0; a < ary.size(); a++) {
            for (int b = a; b < ary.size(); b++) {
                if (ary.get(a) == 16 || ary.get(b) == 16) { }
                else if (ary.get(a) > ary.get(b)) {
                    Inversions++;
                }
            }
        }

        return (Inversions % 2 == 0) == (blankPos % 2 != 0);
    }

    private void startTimer() {
        DoubleProperty timerSecs = new SimpleDoubleProperty();

        timer.textProperty().bind(timerSecs.asString());

        timeline = new Timeline(
                new KeyFrame(Duration.millis(100),
                        event -> {
                            Duration dur = ((KeyFrame) event.getSource()).getTime();
                            time = time.add(dur);
                            timerSecs.set(time.toSeconds());
                        })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void cutImage() {

        //Provide number of rows and column
        int row = 4;
        int col = 4;

        //total width and total height of an image
        int tWidth =  gameImage.getWidth();
        int tHeight =  gameImage.getHeight();

        System.out.println("Image Dimension: " + tWidth + "x" + tHeight);

        //width and height of each piece
        int eWidth = tWidth / col;
        int eHeight = tHeight / row;

        int x = 0; // width
        int y = 0; // height
        int counter = 1;

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                try {
                    BufferedImage SubImage = gameImage.getSubimage(x, y, eWidth, eHeight);

                    File outputfile = new File("/Users/noahkiefer/IdeaProjects/GameOf15_DELUXE/src/main/resources/images/ImageCuts/"+counter+".png");
                    ImageIO.write(SubImage, "png", outputfile);

                    // Add to x in order to move 1/4 to the right and cut the next piece
                    x += eWidth;
                    counter++;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // reset to the left border
            x = 0;
            // move down for the next row of images
            y += eHeight;
        }

    }

    private void importImages() {
        int counter = 1;
        for (int i = 0; i<images.length; i++) {
            for (int k = 0; k<images[i].length; k++) {
                try {
                    if (counter != 16) {
                        FileInputStream fs = new FileInputStream("/Users/noahkiefer/IdeaProjects/GameOf15_DELUXE/src/main/resources/images/ImageCuts/"+counter+".png");
                        images[i][k] = new Image(fs);
                    } else {
                        System.out.println("setting last one to null");
                        images[i][k] = null;
                    }
                    counter++;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isMoveLegal(int x, int y) {
        boolean check = false;

        // Check above
        if (y != 0 && numbers[y - 1][x] == 16) {
            check = true;
            moveDir = "Up";
        }
        // Below
        if (y != gridW-1 && numbers[y + 1][x] == 16) {
            check = true;
            moveDir = "Down";
        }
        // Right
        if (x != gridW-1 && numbers[y][x + 1] == 16) {
            check = true;
            moveDir = "Right";
        }
        // Left
        if (x != 0 && numbers[y][x - 1] == 16) {
            check = true;
            moveDir = "Left";
        }
        return check;
    }

    private void move(int x, int y) {
        int clickedNr = numbers[y][x];
        Image clickedImg = images[y][x];
        switch (moveDir) {
            case "Up":
                numbers[y][x] = numbers[y - 1][x];
                numbers[y - 1][x] = clickedNr;
                images[y][x] = images[y - 1][x];
                images[y - 1][x] = clickedImg;
                break;
            case "Down":
                numbers[y][x] = numbers[y + 1][x];
                numbers[y + 1][x] = clickedNr;
                images[y][x] = images[y + 1][x];
                images[y + 1][x] = clickedImg;
                break;
            case "Right":
                numbers[y][x] = numbers[y][x + 1];
                numbers[y][x + 1] = clickedNr;
                images[y][x] = images[y][x + 1];
                images[y][x + 1] = clickedImg;
                break;
            case "Left":
                numbers[y][x] = numbers[y][x - 1];
                numbers[y][x - 1] = clickedNr;
                images[y][x] = images[y][x - 1];
                images[y][x - 1] = clickedImg;
                break;
        }
    }

    private boolean isGameDone() {
        List<Integer> tmpary = new ArrayList<>();
        boolean tmp = true;                         //starts as true since it is looking if anything is out of place

        for (int i = 0; i < numbers.length; i++) {
            for (int j = 0; j < numbers[i].length; j++) {
                tmpary.add(numbers[i][j]);
            }
        }
        for (int x = 0; x < tmpary.size()-1; x++) {
            if (tmpary.get(x) > tmpary.get(x + 1)) {
                tmp = false;
            }
        }
        if (tmp) {
            gameOver = true;
            timeline.stop();
            Alert a = new Alert(Alert.AlertType.INFORMATION);   //Victory message
            a.setContentText("YOU HAVE WON!!");
            a.setHeaderText(null);
            a.showAndWait();
        }
        return gameOver;
    }

    private void repaintNumbers() {
        int counter = 0;
        for (int i = 0; i < gridW; i++) {
            for (int j = 0; j < gridW; j++) {
                tiles[counter].setGraphic(new ImageView(images[i][j]));
                counter++;
            }
        }
    }

}
