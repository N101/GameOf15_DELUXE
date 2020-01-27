public class Player {

    private String place; // Has been made a string in order to have the number displayed with a '.' at the end of it
    private String name;
    private double time;

    Player(String place, String name, double time) {
        this.place = place;
        this.name = name;
        this.time = time;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
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
