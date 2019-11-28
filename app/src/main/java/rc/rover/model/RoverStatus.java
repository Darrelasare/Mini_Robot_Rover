package rc.rover.model;

public class RoverStatus {

    public enum Direction { left, right };
    public enum Throttle { forward, reverse }

    private String roverName;
    private Direction direction;
    private Throttle motion;
    private float rpm;
    private float distance;

    public RoverStatus() {

    }
    public RoverStatus(String roverName, Direction direction, Throttle motion, float rpm, float distance) {
        this.roverName = roverName;
        this.direction = direction;
        this.motion = motion;
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

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Throttle getMotion() {
        return motion;
    }

    public void setMotion(Throttle motion) {
        this.motion = motion;
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
                ", direction=" + direction +
                ", motion=" + motion +
                ", rpm=" + rpm +
                ", distance=" + distance +
                '}';
    }
}
