/* Code developed by Team Morgaint
 * for Subject IT Project COMP30022
 * Team member:
 * Chengyao Xu
 * Jin Wei Loh
 * Philip Cervenjak
 * Qianqian Zheng
 * Sicong Hu
 */
package com.example.ecare_client.checklist;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import com.example.ecare_client.R;
import com.example.ecare_client.checklist.Tasks.TaskContract;



// followed tutorial from https://www.youtube.com/watch?v=Mg3Gsn0wmDQ&t=732s
// credit to delaroy studios

public class CheckListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {


    // Constants for logging and referring to a unique loader
    private static final String TAG = CheckListActivity.class.getSimpleName();
    private static final int TASK_LOADER_ID = 10;

    // Member variables for the adapter and RecyclerView
    private CursorAdapter cursorAdapter;
    RecyclerView mRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        // Seting the RecyclerView to the corresponding view
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewTasks);

        // Seting the layout for the RecyclerView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        cursorAdapter = new CursorAdapter(this);
        mRecyclerView.setAdapter(cursorAdapter);

        /*
            Enabling swipe behaviour, wipe left or right to delete
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a task
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                int id = (int) viewHolder.itemView.getTag();

                // Build  uri
                String stringId = Integer.toString(id);
                Uri uri = TaskContract.TaskEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();

                // Delete the selected swiped row
                getContentResolver().delete(uri, null, null);

                //  re-query for all tasks after a deletion after restart
                getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, CheckListActivity.this);

            }
        }).attachToRecyclerView(mRecyclerView);

        /*
             launch the AddTaskActivity if the floating action button is clicked
         */
        FloatingActionButton fabButton = (FloatingActionButton) findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addTaskIntent = new Intent(CheckListActivity.this, ConfigureTaskActivity.class);
                startActivity(addTaskIntent);
            }
        });

        /*
         Ensure a loader is initialized and active. If the loader doesn't already exist, one is
         created, otherwise the last created loader is re-used.
         */
        getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);
    }




    /**

     *  take care of loading data at all stages
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor
            Cursor myCursorTaskData = null;

            // when the loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (myCursorTaskData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(myCursorTaskData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {

                try {
                    return getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            TaskContract.TaskEntry.COLUMN_PRIORITY);

                } catch (Exception e) {
                    Log.e(TAG, "Failure to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                myCursorTaskData = data;
                super.deliverResult(data);
            }
        };

    }


    /**

     *  removes any references this activity had to the loader's data.

     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
            cursorAdapter.swapCursor(null);
        }


    @Override
    protected void onResume() {
        super.onResume();

        // re-queries all tasks
        getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the data
        cursorAdapter.swapCursor(data);
    }

}

