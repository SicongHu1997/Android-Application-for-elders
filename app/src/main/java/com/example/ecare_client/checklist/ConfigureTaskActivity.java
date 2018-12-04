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

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.ecare_client.R;
import com.example.ecare_client.checklist.Tasks.TaskContract;



// followed tutorial from https://www.youtube.com/watch?v=Mg3Gsn0wmDQ&t=732s
// credit to delaroy studios

public class ConfigureTaskActivity extends AppCompatActivity {

    //  variable to keep track of a task's priority
    private int taskPriority;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initializing to the highest priority by default ==1
        ((RadioButton) findViewById(R.id.radioButton1)).setChecked(true);
        taskPriority = 1;
    }


    /**
     * when the add button is clicked
     */
    public void onClickAddTask(View view) {

        // Check if there is no input
        String input = ((EditText) findViewById(R.id.editTextTaskDescription)).getText().toString();
        if (input.length() <= 0) {
            return;
        }

        // placing a new task data
        ContentValues contentValues = new ContentValues();
        // Put the task description and selected mPriority into the ContentValues
        contentValues.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, input);
        contentValues.put(TaskContract.TaskEntry.COLUMN_PRIORITY, taskPriority);


        // Insert the content values via a ContentResolver
        Uri uri = getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, contentValues);
        if(uri != null) {
            Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
        }

        // return back to add task page
        finish();

    }


    /**
     * change the value of task priority based on which colour button
     */
    public void onPrioritySelected(View view) {
        if (((RadioButton) findViewById(R.id.radioButton1)).isChecked()) {
            taskPriority = 1;
        } else if (((RadioButton) findViewById(R.id.radioButton2)).isChecked()) {
            taskPriority = 2;
        } else if (((RadioButton) findViewById(R.id.radioButton3)).isChecked()) {
            taskPriority = 3;
        }
    }
}
