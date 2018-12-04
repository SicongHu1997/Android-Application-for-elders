/* Code developed by Team Morgaint
 * for Subject IT Project COMP30022
 * Team member:
 * Chengyao Xu
 * Jin Wei Loh
 * Philip Cervenjak
 * Qianqian Zheng
 * Sicong Hu
 */
package com.example.ecare_client.checklist.Tasks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ecare_client.checklist.Tasks.TaskContract.TaskEntry;


// followed tutorial from https://www.youtube.com/watch?v=Mg3Gsn0wmDQ&t=732s
// credit to delaroy studios


public class TaskHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tasksDb.db";

    private static final int VERSION = 1;


    TaskHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }


    /**
     * when database is initialised
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        final String CREATE_TABLE = "CREATE TABLE "  + TaskEntry.TABLE_NAME + " (" +
                        TaskEntry._ID                + " INTEGER PRIMARY KEY, " +
                        TaskEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                        TaskEntry.COLUMN_PRIORITY    + " INTEGER NOT NULL);";

        db.execSQL(CREATE_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME);
        onCreate(db);
    }
}
