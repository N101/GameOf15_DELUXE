import javafx.scene.control.Button;

public class Btn extends Button{

    private int x;
    private int y;

    Btn(int x, int y) {
        this.x = x;
        this.y = y;
    }

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

}
