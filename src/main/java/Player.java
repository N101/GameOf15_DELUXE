public class Player {

    private int place;
    private String name;
    private double time;

    Player(int place, String name, double time) {
        this.place = place;
        this.name = name;
        this.time = time;
    }

    Player(int place, String name) {
        this.place = place;
        this.name = name;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}
