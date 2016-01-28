package so2.unica.qaddu.models;

import android.database.SQLException;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import so2.unica.qaddu.helpers.DatabaseHelper;

/**
 * Created by Sergio.
 */

@DatabaseTable(tableName = "workouts")
public class WorkoutItem {
    @DatabaseField(generatedId = true)
    int id;

    @DatabaseField
    String name;

    @DatabaseField
    Date start;

    @DatabaseField
    Long totalTime;

    @DatabaseField
    Double distance;

    @ForeignCollectionField(eager = true)
    Collection<WorkoutPoint> points;



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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setPoints(List<WorkoutPoint> points) throws java.sql.SQLException {
        if (this.points == null) {
            Dao<WorkoutItem, Integer> dao = DatabaseHelper.getIstance().getDao();
            this.points = dao.getEmptyForeignCollection("points");
        }
        this.points.addAll(points);
    }

    public List<WorkoutPoint> getPoints() {
        List<WorkoutPoint> notesArray = new ArrayList<WorkoutPoint>();
        for (WorkoutPoint note : points) {
            notesArray.add(note);
        }
        return notesArray;
    }
}

