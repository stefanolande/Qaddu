package so2.unica.qaddu.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

import so2.unica.qaddu.models.WorkoutItem;
import so2.unica.qaddu.models.WorkoutPoint;


/**
 * Helper, implemented as a singleton, that provides read, write, delete and update method for the database via object-relational mapping.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

   private static final String DATABASE_NAME = "quaddu.db";
   private static final int DATABASE_VERSION = 19;
   private static DatabaseHelper mDatabaseHelper;

   private DatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
      // Trick to call onCreate - onUpgrade
      getWritableDatabase();
   }

   public static void initialize(Context ctx) {
      if (mDatabaseHelper == null) {
         mDatabaseHelper = new DatabaseHelper(ctx.getApplicationContext());
      }
   }

   public static DatabaseHelper getInstance() {
      return mDatabaseHelper;
   }

   @Override
   public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
      Log.i(DatabaseHelper.class.getName(), "onCreate");
      try {
         TableUtils.createTable(connectionSource, WorkoutItem.class);
         TableUtils.createTable(connectionSource, WorkoutPoint.class);
      } catch (SQLException e) {
         e.printStackTrace();
      }

   }

   @Override
   public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {
      Log.i(DatabaseHelper.class.getName(), "onUpgrade");
      try {
         TableUtils.dropTable(connectionSource, WorkoutItem.class, true);
         TableUtils.dropTable(connectionSource, WorkoutPoint.class, true);
      } catch (SQLException e) {
         e.printStackTrace();
      }
      onCreate(sqLiteDatabase, connectionSource);
   }

   /**
    * Adds an object to the database
    *
    * @param object the object to save
    * @param name   the class name of the object
    * @param <K>    the class of the object
    */
   public <K> void addData(K object, Class<K> name) {
      try {
         Dao dao = getDao(name);
         dao.create(object);
      } catch (SQLException e) {
         e.printStackTrace();
      }

   }

   /**
    * Removes an object to the database
    * @param object the object to save
    * @param name the class name of the object
    * @param <K> the class of the object
    */
   public <K> void removeData(K object, Class<K> name) {
      try {
         Dao dao = getDao(name);
         dao.delete(object);
      } catch (SQLException e) {
         e.printStackTrace();
      }

   }

   /**
    * Removes all objects of a class from the db
    * @param name objects class' name to delete
    * @param <K> class of the objects
    */
   public <K> void deleteAll(Class<K> name) {
      try {
         TableUtils.clearTable(getConnectionSource(), name);
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }

   /**
    * Returns a list of the objects in the db of a given class
    * @param name objects class' name
    * @param <K> class of the objects
    * @return List
    */
   public <K> List<K> GetData(Class<K> name) {
      List<K> list = null;
      try {
         Dao dao = getDao(name);
         list = dao.queryForAll();
      } catch (SQLException e) {
         e.printStackTrace();
      }
      return list;
   }

   /**
    * Return a object of the given class with the id passed as parameter
    * @param id id of the object you want to retrieve
    * @param name object class' name
    * @param <K> class of the object
    * @return the object retrieved from the db
    */
   public <K> Object getItemById(int id, Class<K> name) {
      Object item = null;
      try {
         Dao dao = getDao(name);
         item = dao.queryForId(id);
      } catch (SQLException e) {
         e.printStackTrace();
      }
      return item;
   }

   /**
    * Returns the data access object
    * @return the dao
    */
   public Dao<WorkoutItem, Integer> getDao() {
      try {
         return getDao(WorkoutItem.class);
      } catch (SQLException e) {
         e.printStackTrace();
      }
      return null;
   }
}
