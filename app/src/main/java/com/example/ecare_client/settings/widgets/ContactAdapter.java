/* Code developed by Team Morgaint
 * for Subject IT Project COMP30022
 * Team member:
 * Chengyao Xu
 * Jin Wei Loh
 * Philip Cervenjak
 * Qianqian Zheng
 * Sicong Hu
 */
package com.example.ecare_client.settings.widgets;


import com.example.ecare_client.R;
import com.example.ecare_client.settings.Contact;



import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;


import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;



public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<Contact> mContacts;



    // Pass in the contact array into the constructor
    public ContactAdapter(List<Contact> contacts) {
        mContacts = contacts;
    }

    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_contact, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ContactAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        final Contact contact = mContacts.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.nameTextView;
        textView.setText(contact.getNickname());
        textView.setTextColor(contact.isOnline() ? Color.BLACK : Color.GRAY);


        TextView status = viewHolder.statusTextView;
        status.setText(contact.isOnline() ? "Online" : "Offline");
        status.setTextColor(contact.isOnline() ? Color.BLACK : Color.GRAY);




        viewHolder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                int action = motionEvent.getAction();

                if (action == MotionEvent.ACTION_UP) {

                    // Start activity using the context of ContactListActivity.
                    (contact.getContext()).beginProfile(contact);
                }

                return true;
            }
        });


        CheckBox checkbox = viewHolder.deleteCheckbox;
        checkbox.setChecked(contact.isChecked());

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                contact.setChecked(b);

            }
        });




    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mContacts.size();
    }




    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public TextView statusTextView;
        public CheckBox deleteCheckbox;



        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.contact_name);
            statusTextView = (TextView) itemView.findViewById(R.id.online_status);
            deleteCheckbox = (CheckBox) itemView.findViewById(R.id.delete_checkbox);
        }
    }

}
