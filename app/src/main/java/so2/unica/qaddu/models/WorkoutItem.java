package so2.unica.qaddu.models;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import so2.unica.qaddu.helpers.DatabaseHelper;

/**
 * Created by Sergio
 */

@DatabaseTable(tableName = "workouts")
public class WorkoutItem {
   @DatabaseField(generatedId = true)
   int id;

   @DatabaseField
   String name;

   @DatabaseField
   Date startDate;

   @DatabaseField
   Long totalTime;

   @DatabaseField
   Double distance;

   @ForeignCollectionField(eager = true)
   Collection<WorkoutPoint> points;


   public Date getStartDate() {
      return startDate;
   }

   public void setStartDate(Date startDate) {
      this.startDate = startDate;
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

   public List<WorkoutPoint> getPoints() {
      List<WorkoutPoint> notesArray = new ArrayList<WorkoutPoint>();
      for (WorkoutPoint note : points) {
         notesArray.add(note);
      }
      return notesArray;
   }

   public void setPoints(List<WorkoutPoint> points) throws java.sql.SQLException {
      if (this.points == null) {
         Dao<WorkoutItem, Integer> dao = DatabaseHelper.getIstance().getDao();
         this.points = dao.getEmptyForeignCollection("points");
      }
      this.points.addAll(points);
   }
}

