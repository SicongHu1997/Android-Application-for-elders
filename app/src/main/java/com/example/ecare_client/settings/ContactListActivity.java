/* Code developed by Team Morgaint
 * for Subject IT Project COMP30022
 * Team member:
 * Chengyao Xu
 * Jin Wei Loh
 * Philip Cervenjak
 * Qianqian Zheng
 * Sicong Hu
 */
package com.example.ecare_client.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.example.ecare_client.BaseActivity;
import com.example.ecare_client.ChatActivity;
import com.example.ecare_client.R;


import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.example.ecare_client.SinchService;
import com.example.ecare_client.TitleLayout;
import com.example.ecare_client.settings.widgets.ContactAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.SinchError;


import java.io.Serializable;
import java.util.ArrayList;

public class ContactListActivity extends BaseActivity implements Serializable {

    private EditText inputContact;
    private Button btnAddContact;
    private ProgressBar progressBar;
    private Button btnDeleteContacts;
    private ProgressDialog mSpinner;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private DatabaseReference userRef;

    final private ArrayList<Contact> contacts = new ArrayList<>();
    final private ContactAdapter adapter = new ContactAdapter(contacts);
    private RecyclerView contactListView;






    // Set layout manager to position the items




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact_list);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.hide();
        }
        TitleLayout titleLayout = (TitleLayout) findViewById(R.id.contact_list_title);
        titleLayout.setTitleText("Contact List");

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        btnAddContact = (Button) findViewById(R.id.add_contact_button);
        inputContact = (EditText) findViewById(R.id.contact_email);
        btnDeleteContacts = (Button) findViewById(R.id.delete_contact_button);


        contactListView = (RecyclerView) findViewById(R.id.contact_list_view);
        contactListView.setAdapter(adapter);
        contactListView.setLayoutManager(new LinearLayoutManager(this));

        // contactIDs is just used to pass the contacts onto another method.
        final ArrayList<String> contactIDs = new ArrayList<>();
        final ArrayList<String> contactNicknames = new ArrayList<>();

        //-----------------------------------------------------------------------------
        final FirebaseUser currentUser = auth.getCurrentUser();

        userRef = database.getReference().child("Users").child(currentUser.getUid());

        Query queryContacts = userRef.child("Contacts").orderByKey();


        queryContacts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    // dataSnapshot is the "Users" node with all children with contactEmail.
                    for (DataSnapshot matchingContact : dataSnapshot.getChildren()) {

                        // Very bad way of doing this perhaps.
                        if ( !(matchingContact.getKey().equals("Null")) ) {

                            contactIDs.add(matchingContact.getKey());
                            contactNicknames.add(matchingContact.getValue(String.class));


                        }

                    }

                    setContactListeners(contactIDs,
                            contactNicknames,
                            contacts,
                            adapter,
                            contactListView);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing.
            }
        });







        //-----------------------------------------------------------------------------




        // Initialize contacts

        // Create adapter passing in the sample user data

        // Attach the adapter to the recyclerview to populate items


        // Refresh list after doing the query, even before pressing AddContact.



        btnAddContact.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String contactEmail = inputContact.getText().toString().trim();

                    if (contactEmail.equals(auth.getCurrentUser().getEmail())) {
                        Toast.makeText(getApplicationContext(),
                                "You cannot add yourself as a contact.",
                                Toast.LENGTH_SHORT).show();
                        return;

                    }


                    DatabaseReference queryRef =
                            database.getReference().child("Users");

                    // Check if contactEmail is in the database!!!
                    Query queryNew = queryRef.orderByChild("Email").equalTo(contactEmail);


                    queryNew.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // dataSnapshot is the "Users" node with all children with contactEmail.
                                for (DataSnapshot matchingContact : dataSnapshot.getChildren()) {
                                    // do something with the individual "contact"
                                    // since we're in this loop, we know the contact email exists.


                                    // NEED TO CHECK IF THE CONTACT IS ALREADY RECORDED.
                                    String contactUid = matchingContact.getKey();

                                    Contact searchObject = new Contact("Null", contactUid, false);
                                    boolean isAlreadyContact = contacts.contains(searchObject);

                                    if (isAlreadyContact) {
                                        Toast.makeText(getApplicationContext(),
                                                contactEmail + " is already a contact.",
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    String contactNickname = contactEmail;
                                    userRef.child("Contacts").child(contactUid).setValue(contactNickname);

                                    // Need to update the other user's contact list.
                                    DatabaseReference contactRef =
                                            database.getReference().
                                                    child("Users").child(contactUid);

                                    // May need to worry about refreshing the other user's page!!


                                    contactRef.child("Contacts").
                                            child(currentUser.getUid()).setValue(currentUser.getEmail());


                                    setContactListener(contactUid,
                                            contactNickname,
                                            contacts,
                                            adapter,
                                            contactListView);


                                }
                                // NEED TO REFRESH THE LIST VIEW!!!!

                            }

                            else {
                                Toast.makeText(getApplicationContext(),
                                        "User " + contactEmail+ " does not exist.",
                                        Toast.LENGTH_SHORT).show();
                                return;

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Do nothing.
                        }
                    });


                }
            }

        );


        // Perhaps make this button disabled if no checkboxes are pressed.
        btnDeleteContacts.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteContacts(contacts, adapter, contactListView, false);


                    }
                }

        );




    }




    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        //deleteContacts(contacts, adapter, contactListView, true);

    }



    public void beginProfile(Contact contact) {
        Intent contactProfileActivity = new Intent(this, ContactProfileActivity.class);

        contactProfileActivity.putExtra("ContactName", contact.getName());
        contactProfileActivity.putExtra("ContactKey", contact.getKey());
        contactProfileActivity.putExtra("ContactNickname", contact.getNickname());

        startActivity(contactProfileActivity);

    }




    protected void setContactListeners(ArrayList<String> contactIDs,
                                       ArrayList<String> contactNicknames,
                                       final ArrayList<Contact> contacts,
                                       final ContactAdapter adapter,
                                       final RecyclerView listView) {


        int nicknameIndex = 0;
        String contactNickname;

        for (String contactID : contactIDs) {
            contactNickname = contactNicknames.get(nicknameIndex);

            setContactListener(contactID, contactNickname,
                    contacts, adapter, listView);

            nicknameIndex += 1;

        }


    }

    // Call this method only once for each contact when
    // you enter the activity.
    // (Of course, you can call it multiple times if entering
    // the activity multiple times).
    protected void setContactListener(final String contactID,
                                      final String contactNickname,
                                      final ArrayList<Contact> contacts,
                                      final ContactAdapter adapter,
                                      final RecyclerView listView) {

        // If the contact is already present at this point in the method,
        // then DON'T reset the ValueEventListener!!!!

        String contactEmail = "Null";
        Boolean contactOnline = false;


        Contact searchObject = new Contact(contactEmail, contactID, contactOnline);

        int currentIndex = contacts.indexOf(searchObject);

        if (currentIndex == -1) {
            // Then a new contact has to be created.
            contacts.add(0, searchObject);
            adapter.notifyItemInserted(0);

            searchObject.setContext(this);

            // All the other values for the contact are determined
            // in the EventListeners.

        }

        else {
            Log.d("ERROR", "Contact already stored somehow!!!!");
        }



        database = FirebaseDatabase.getInstance();


        DatabaseReference contactIDRef = database.getReference().child("Users").child(contactID);

        DatabaseReference userContactRef = userRef.child("Contacts").child(contactID);

        // Also need to initialise the contact list.

        ValueEventListener contactEventListener = new ValueEventListener() {
            @Override
            // THE DATA SNAPSHOT IS AT THE CHILD!! NOT THE ROOT NODE!!!!
            public void onDataChange(DataSnapshot dataSnapshot) {

                String contactEmail = "Null";
                Boolean contactOnline = false;

                String testKeyForNull = dataSnapshot.getKey();

                if (testKeyForNull == null) {
                    Log.d("Contact", "User deleted!");
                    return;

                }

                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.getKey().equals("Email")) {
                        contactEmail = child.getValue(String.class);
                    }

                    if (child.getKey().equals("Online")) {
                        contactOnline = Boolean.parseBoolean(
                                child.getValue(String.class));
                    }
                }


                // Use this only to search for the required contact in "contacts" list,
                // unless the contact does NOT already exist.
                Contact searchObject = new Contact(contactEmail, contactID, contactOnline);

                int currentIndex = contacts.indexOf(searchObject);



                if (currentIndex != -1) {
                    // Edit the EXISTING contact.
                    Contact contactObject = contacts.get(currentIndex);

                    boolean wasChecked = contactObject.isChecked();


                    contactObject.setOnline(contactOnline);
                    contactObject.setChecked(wasChecked);

                    contactObject.setName(contactEmail);


                    if (contactOnline) {
                        // Then move the contact to the top.
                        adapter.notifyItemChanged(currentIndex);
                        contacts.remove(currentIndex);
                        contacts.add(0, contactObject);
                        adapter.notifyItemMoved(currentIndex, 0);

                    }

                    else {
                        adapter.notifyItemChanged(currentIndex);
                        contacts.remove(currentIndex);
                        contacts.add(contactObject);

                        int lastIndex = contacts.size() - 1;
                        adapter.notifyItemMoved(currentIndex, lastIndex);


                    }

                }

                else {
                    Log.d("ERROR", "Contact not stored locally!");

                }

                // In case the contact wasn't previously in the list.




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing.
            }

        };


        ValueEventListener contactNicknameListener = new ValueEventListener() {

            @Override
            // THE DATA SNAPSHOT IS AT THE CHILD!! NOT THE ROOT NODE!!!!
            public void onDataChange(DataSnapshot dataSnapshot) {

                String newNickname = (String) dataSnapshot.getValue();

                // THIS VALUE MIGHT GET DELETED!!!!
                if (newNickname == null) {
                    Log.d("Contact", "Contact deleted from list!");
                    return;
                }


                String contactEmail = "Null";
                Boolean contactOnline = false;

                Contact searchObject = new Contact(contactEmail, contactID, contactOnline);

                int currentIndex = contacts.indexOf(searchObject);
                // I don't think currentIndex can ever be -1, since the contact must exist.
                Contact contactObject = contacts.get(currentIndex);

                contactObject.setNickname(newNickname);

                adapter.notifyItemChanged(currentIndex);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing.
            }


        };

        searchObject.setContactEventListener(contactEventListener);
        searchObject.setContactNicknameListener(contactNicknameListener);

        contactIDRef.addValueEventListener(contactEventListener);
        userContactRef.addValueEventListener(contactNicknameListener);

    }

    protected void deleteContacts(final ArrayList<Contact> contacts,
                                  final ContactAdapter adapter,
                                  final RecyclerView listView,
                                  boolean deleteAllContacts) {

        int i = 0;

        while (i < contacts.size()) {
            Contact contactObject = contacts.get(i);

            if (contactObject.isChecked() || deleteAllContacts) {

                deleteContact(contactObject,
                        contacts,
                        adapter,
                        listView);

            }

            else {
                i += 1;
            }

        }
    }

    protected void deleteContact(final Contact contactObject,
                                 final ArrayList<Contact> contacts,
                                 final ContactAdapter adapter,
                                 final RecyclerView listView) {


        final String contactID = contactObject.getKey();

        final FirebaseUser currentUser = auth.getCurrentUser();

        final DatabaseReference userRef =
                database.getReference().child("Users").child(currentUser.getUid());

        userRef.child("Contacts").child(contactID).removeValue();

        // Need to remove the previous event listener so that the contact does not reappear
        // in the list.
        DatabaseReference contactRef =
                database.getReference().
                        child("Users").child(contactID);

        contactRef.removeEventListener(contactObject.getContactEventListener());
        contactRef.removeEventListener(contactObject.getContactNicknameListener());

        // Need to remove the previous event listener!!!!!

        // May need to worry about refreshing the other user's page!!
        Contact searchObject = new Contact("Null", contactID, false);
        int currentIndex = contacts.indexOf(searchObject);

        contacts.remove(currentIndex);
        adapter.notifyItemRemoved(currentIndex);


        contactRef.child("Contacts").
                child(currentUser.getUid()).removeValue();




        // Need to update the other user's contact list.



    }
}
