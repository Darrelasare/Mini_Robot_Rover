package rc.rover.model;

public class RoverStatus {
    private String roverName;
    private float rpm;
    private float distance;

    public RoverStatus() {}

    public RoverStatus(String roverName, float rpm, float distance) {
        this.roverName = roverName;
        this.rpm = rpm;
        this.distance = distance;
    }

    public Float getRpm() {
        return rpm;
    }

    public void setRpm(Float rpm) {
        this.rpm = rpm;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public String getRoverName() {
        return roverName;
    }

    public void setRoverName(String roverName) {
        this.roverName = roverName;
    }

    @Override
    public String toString() {
        return "RoverStatus{" +
                "roverName='" + roverName + '\'' +
                ", rpm=" + rpm +
                ", distance=" + distance +
                '}';
    }
}
