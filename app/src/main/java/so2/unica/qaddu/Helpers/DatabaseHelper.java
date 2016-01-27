package so2.unica.qaddu.Helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

import so2.unica.qaddu.Models.WorkoutItem;


/**
 * Created by Sergio
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static DatabaseHelper mDatabaseHelper;
    private static final String DATABASE_NAME = "quaddu.db";
    private static final int DATABASE_VERSION = 1;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // Trick to call onCreate - onUpgrade
        getWritableDatabase();
    }

    public static DatabaseHelper getIstance(Context ctx){
        if(mDatabaseHelper == null){
            mDatabaseHelper = new DatabaseHelper(ctx.getApplicationContext());
        }
        return mDatabaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        Log.i(DatabaseHelper.class.getName(), "onCreate");
        try {
            TableUtils.createTable(connectionSource, WorkoutItem.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {
        Log.i(DatabaseHelper.class.getName(), "onUpgrade");
        try {
            TableUtils.dropTable(connectionSource, WorkoutItem.class, true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        onCreate(sqLiteDatabase, connectionSource);
    }

    public <K> void addData(K object, Class<K> name) {
        try {
            Dao dao = getDao(name);
            dao.create(object);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public <K> void DeleteAll(Class<K> name){
        try {
            TableUtils.clearTable(getConnectionSource(),name);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public <K> List<K> GetData(Class<K> name)
    {
        List<K> list = null;
        try {
            Dao dao = getDao(name);
            list = dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
