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
 * Models a single workout done by the user. Contains basic informations and a list of WorkoutPoints.
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

   /**
    * Returns the current list of WorkoutPoint
    *
    * @return List of workout point
    */
   public List<WorkoutPoint> getPoints() {
      //convert the Collection<WorkoutPoint> (needed by ORMLite for the 1-N relation) in a List<WorkoutPoint>
      List<WorkoutPoint> notesArray = new ArrayList<>();
      for (WorkoutPoint note : points) {
         notesArray.add(note);
      }
      return notesArray;
   }

   /**
    * Set the list passed as the current list of workoutPoints
    * @param points List of workout points
    * @throws java.sql.SQLException
    */
   public void setPoints(List<WorkoutPoint> points) throws java.sql.SQLException {
      if (this.points == null) {
         //Initialize the collection using the dao
         Dao<WorkoutItem, Integer> dao = DatabaseHelper.getInstance().getDao();
         this.points = dao.getEmptyForeignCollection("points");
      }
      this.points.addAll(points);
   }


   public double getAverageSpeed() {
      double speed = 0;
      if (totalTime != 0) {
         speed = distance / (totalTime / 1000) * 3.6;
      }
      return speed;
   }

   public double getAveragePaceInSeconds() {
      double speed = getAverageSpeed();
      if (speed != 0) {
         return (1 / speed) * 3600;
      } else {
         return 0;
      }
   }
}

