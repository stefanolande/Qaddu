package so2.unica.qaddu.models;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by stefano on 27/01/16.
 * This class models an istant of the workout
 */

@DatabaseTable(tableName = "workout_points")
public class WorkoutPoint {

    @DatabaseField(foreign = true)
    public transient WorkoutItem workout;
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private double latitude;
    @DatabaseField
    private double longitude;
    @DatabaseField
    private double speed;
    @DatabaseField
    private double step;
    @DatabaseField
    private double altitude;
    @DatabaseField
    private long time;
    @DatabaseField
    private double distance;

    public WorkoutPoint() {
    }

    public WorkoutPoint(WorkoutItem workout, double latitude, double longitude, double speed, double altitude, long time, double distance) {
        this.workout = workout;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.altitude = altitude;
        this.time = time;
        this.distance = distance;

        //calculate the step from the speed
        double km = distance / 1000;
        double stepInSeconds = time / km;
        this.step = stepInSeconds / 60;
    }

    public WorkoutItem getWorkout() {
        return workout;
    }

    public void setWorkout(WorkoutItem workout) {
        this.workout = workout;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getStep() {
        return step;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
