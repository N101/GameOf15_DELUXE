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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameImage  {
    private Btn[] tiles;
    private final int gridW = 4;
    private int[][] numbers;
    private boolean gameOver = false;
    private String MoveDir = "";
    private Image[][] images;
    public static String puzzleImg;

    private Label timer = new Label();
    private Duration time = Duration.ZERO;
    private Timeline timeline;

    public GameImage(Stage primaryStage)  {
        numbers = new int[4][4];
        tiles = new Btn[16];
        images = new Image[4][4];

        primaryStage.setTitle("Game of 15");
        newGame();
        primaryStage.setScene(createTheScene());
        primaryStage.show();
    }

    public Scene createTheScene() {
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

                tiles[counter].setMinSize(100, 100);
                tiles[counter].setMaxSize(100, 100);

                tiles[counter].setGraphic(new ImageView(images[i][j]));

                int finalI = i;
                int finalJ = j;

                tiles[counter].setOnAction(e -> {
                    if (gameOver == true) {
                        new Alert(Alert.AlertType.ERROR, "Game is done already! Start a new game").showAndWait();
                    } else if (isMoveLegal(finalJ, finalI) == true) {
                        move(finalJ, finalI);
                        repaintNumbers();
                        System.out.println(isGameDone());
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

    public void newGame() {
        setBoard();
        printGrid();
        shuffleStart();
        printGrid();
        importImages();
        MoveDir = "";
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
        String word = "";
        for (int i = 0; i < numbers.length; i++) {
            for (int j = 0; j < numbers[i].length; j++) {
                word += numbers[i][j] + ", ";
            }
        }
        System.out.println(word);
    }

    private void shuffleStart() {
        Random ran = new Random();

        for (int i = numbers.length - 1; i > 0; i--) {
            for (int k = numbers[i].length - 1; k > 0; k--) {
                int m = ran.nextInt(i + 1);
                int n = ran.nextInt(k + 1);

                int tmp = numbers[i][k];
                numbers[i][k] = numbers[m][n];
                numbers[m][n] = tmp;
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
                if (ary.get(a) == 16 || ary.get(b) == 16) {
                } else if (ary.get(a) > ary.get(b)) {
                    Inversions++;
                }
            }
        }

        if (gridW % 2 == 0) {
            System.out.println((Inversions % 2 == 0) == (blankPos % 2 != 0));
            return (Inversions % 2 == 0) == (blankPos % 2 != 0);
        } else {
            return Inversions % 2 == 0;
        }
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

    private void importImages() {
        List<Integer> ary = new ArrayList<>();
        for (int a = 0; a<numbers.length; a++) {
            for (int b = 0; b<numbers[a].length; b++) {
                ary.add(numbers[a][b]);
            }
        }

        int counter = 0;
        for (int i = 0; i<images.length; i++) {
            for (int k = 0; k<images[i].length; k++) {
                try {
                    if (ary.get(counter) != 16) {
                        FileInputStream fs = new FileInputStream("/Users/noahkiefer/IdeaProjects/GameOfFINISH/src/sample/images/" + puzzleImg + "/" + ary.get(counter) + ".png");
                        images[i][k] = new Image(fs);
                    } else {
                        images[i][k] = null;
                    }
                    counter++;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isMoveLegal(int x, int y) {
        boolean check = false;

        // Check above
        if (y != 0 && numbers[y - 1][x] == 16) {
            check = true;
            MoveDir = "Up";
        }
        // Below
        if (y != gridW-1 && numbers[y + 1][x] == 16) {
            check = true;
            MoveDir = "Down";
        }
        // Right
        if (x != gridW-1 && numbers[y][x + 1] == 16) {
            check = true;
            MoveDir = "Right";
        }
        // Left
        if (x != 0 && numbers[y][x - 1] == 16) {
            check = true;
            MoveDir = "Left";
        }
        return check;
    }

    private void move(int x, int y) {
        int clickedNr = numbers[y][x];
        Image clickedImg = images[y][x];
        switch (MoveDir) {
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

    public boolean isGameDone() {
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
