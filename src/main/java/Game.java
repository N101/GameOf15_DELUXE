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
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game  {
    private Btn[] tiles;
    private final int gridW;
    private int[][] numbers;
    private boolean gameOver = false;
    private String MoveDir = "";

    private Label timer = new Label();
    private Duration time = Duration.ZERO;
    private Timeline timeline;

    public Game(Stage primaryStage, int width)  {
        gridW = width;
        numbers = new int[gridW][gridW];
        tiles = new Btn[gridW*gridW];

        primaryStage.setTitle("Game of 15");
        newGame();
        primaryStage.setScene(createTheScene());
        primaryStage.show();
    }

    public Scene createTheScene() {
        BorderPane bp = new BorderPane();
        bp.getStylesheets().add("/../resources/StyleClass.css");  //../resources/StyleClass.css

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
                tiles[counter].setId("tiles");
                tiles[counter].setText(String.valueOf(numbers[i][j]));
                tiles[counter].setMinSize(100, 100);

                int finalI = i;
                int finalJ = j;

                tiles[counter].setOnAction(e -> {
                    if (gameOver == true) {
                        new Alert(Alert.AlertType.ERROR, "Game is done already! Start a new game").showAndWait();
                    } else if (isMoveLegal(finalJ, finalI) == true) {
                        move(finalJ, finalI);
                        repaintNumbers();
                        try {
                            System.out.println(isGameDone());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (GeneralSecurityException e1) {
                            e1.printStackTrace();
                        }
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
        numbers[gridW-1][gridW-1] = -1;
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
                if (numbers[i][j] == -1) {
                    blankPos = 4 - i;
                }
            }
        }

        for (int a = 0; a < ary.size(); a++) {
            for (int b = a; b < ary.size(); b++) {
                if (ary.get(a) == -1 || ary.get(b) == -1) {
                } else if (ary.get(a) > ary.get(b)) {
                    Inversions++;
                }
            }
        }

        if (gridW % 2 == 0) {
            System.out.println((Inversions % 2 == 0) == (blankPos % 2 != 0));
            return (Inversions % 2 == 0) == (blankPos % 2 != 0);
//            if ((Inversions % 2 == 0) == (blankPos % 2 != 0)) {
//                return;
//            } else {
//                shuffleStart();
//            }
        } else {
            return Inversions % 2 == 0;
//            if (Inversions % 2 == 0) {
//                return ;
//            } else {
//                shuffleStart();
//            }
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

    public boolean isMoveLegal(int x, int y) {
        boolean check = false;

        // Check above
        if (y != 0 && numbers[y - 1][x] == -1) {
            check = true;
            MoveDir = "Up";
        }
        // Below
        if (y != gridW-1 && numbers[y + 1][x] == -1) {
            check = true;
            MoveDir = "Down";
        }
        // Right
        if (x != gridW-1 && numbers[y][x + 1] == -1) {
            check = true;
            MoveDir = "Right";
        }
        // Left
        if (x != 0 && numbers[y][x - 1] == -1) {
            check = true;
            MoveDir = "Left";
        }
        return check;
    }

    private void move(int x, int y) {
        int clickedNr = numbers[y][x];
        switch (MoveDir) {
            case "Up":
                numbers[y][x] = numbers[y - 1][x];
                numbers[y - 1][x] = clickedNr;
                break;
            case "Down":
                numbers[y][x] = numbers[y + 1][x];
                numbers[y + 1][x] = clickedNr;
                break;
            case "Right":
                numbers[y][x] = numbers[y][x + 1];
                numbers[y][x + 1] = clickedNr;
                break;
            case "Left":
                numbers[y][x] = numbers[y][x - 1];
                numbers[y][x - 1] = clickedNr;
                break;
        }
    }

    public boolean isGameDone() throws IOException, GeneralSecurityException {
        List<Integer> tmpary = new ArrayList<>();
        boolean tmp = true;                         //starts as true since it is looking if anything is out of place

        for (int i = 0; i < numbers.length; i++) {
            for (int j = 0; j < numbers[i].length; j++) {
                tmpary.add(numbers[i][j]);
            }
        }
        for (int x = 0; x < tmpary.size() - 2; x++) {   //not looking at the last field (should be the empty tile)
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

            if (gridW == 4) {
                Stage enterName = new Stage();
                enterName.setTitle("Enter your username");
                BorderPane bPane = new BorderPane();
                bPane.getStylesheets().add("StyleClass.css");
                bPane.setId("name");

                TextField txtName = new TextField();
                txtName.setMaxWidth(200);
                Button sendName = new Button("Save");
                sendName.setOnAction(e -> {
                    enterName.hide();
                });
                VBox vBox = new VBox();
                vBox.setAlignment(Pos.CENTER);
                vBox.getChildren().addAll(new Label("Enter username to record your time"), txtName, sendName);
                vBox.setSpacing(12);
                vBox.setStyle("-fx-margin-top: 30;");

                bPane.setCenter(vBox);

                enterName.setScene(new Scene(bPane, 300, 200));
                enterName.showAndWait();

                System.out.println(time.toSeconds());
                System.out.println(txtName.getText());
                GoogleLeaderboard.newPlayer(txtName.getText(), time.toSeconds());
            }
        }
        return gameOver;
    }

    public void setStyle(String style, Btn btn) {
        if (style.equals("empty")) {
            btn.setStyle(null);
            btn.getStyleClass().clear();
            btn.getStyleClass().add("emptyBtn");
            btn.setStyle("emptyPos");
        } else if (style.equals("normal")) {
            btn.getStyleClass().clear();
            btn.getStyleClass().add("button");
        }
    }

    private void repaintNumbers() {
        int counter = 0;
        for (int i = 0; i < gridW; i++) {
            for (int j = 0; j < gridW; j++) {
                tiles[counter].setText(String.valueOf(numbers[i][j]));
                if (numbers[i][j] == -1) {
                    tiles[counter].setId("emptyBtn");
                } else {
                    tiles[counter].setId("tiles");
                }
                counter++;
            }
        }
    }


}
