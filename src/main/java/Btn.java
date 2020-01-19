import javafx.scene.control.Button;
import javafx.css.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class Btn extends Button{
    Main main = new Main();

    private boolean empty;
    private int x;
    private int y;

    Btn(int x, int y) {
        this.x = x;
        this.y = y;

    }

//    public void setLabel(String a) {
//        button.setText(a);
//    }


    //    public int getID() {
//        return ID;
//    }
//
//    public void setID(int ID) {
//        this.ID = ID;
//    }
//
    public int getXs() {
        return x;
    }

    public void setXs(int x) {
        this.x = x;
    }

    public int getYs() {
        return y;
    }

    public void setYs(int y) {
        this.y = y;
    }

    public void setGraphic(Image image) {
        this.setGraphic(new ImageView());
    }
}
