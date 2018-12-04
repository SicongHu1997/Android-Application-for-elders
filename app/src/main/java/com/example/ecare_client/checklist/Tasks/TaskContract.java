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

import android.net.Uri;
import android.provider.BaseColumns;


// followed tutorial from https://www.youtube.com/watch?v=Mg3Gsn0wmDQ&t=732s
// credit to delaroy studios

public class TaskContract {


    public static final String PATH = "com.example.android.checklist";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + PATH);

    // Define the possible paths for accessing data in this contract
    // This is the path for the "tasks" directory
    public static final String PATH_TASKS = "tasks";

    /* TaskEntry is an inner class that defines the contents of the task table */
    public static final class TaskEntry implements BaseColumns {

        // TaskEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASKS).build();


        // Task table and column names
        public static final String TABLE_NAME = "tasks";

        // Since TaskEntry implements the interface "BaseColumns", it has an automatically produced
        // "_ID" column in addition to the two below
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_PRIORITY = "priority";

        

    }
}
