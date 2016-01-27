package so2.unica.qaddu.Models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by Sergio.
 */

@DatabaseTable(tableName = "workouts")
public class WorkoutItem {
    @DatabaseField(generatedId = true)
    int id;

    @DatabaseField
    Date start;

    @DatabaseField
    Long totalTime;

    @DatabaseField
    Double distance;

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
