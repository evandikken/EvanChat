package nl.evandikken.evanchat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;

import nl.evandikken.evanchat.adapters.UsersAdapter;
import nl.evandikken.evanchat.models.UserModel;

public class AddFriendsActivity extends AppCompatActivity {
    private ListView users;
    private SearchView searchView;

    private ArrayList<UserModel> usersList;

    private UsersAdapter adapter;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mRequestsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        users = findViewById(R.id.addFriendsListFriends);
        searchView = findViewById(R.id.searchView);


        //Set the mUsersDatabase reference
        mUsersDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users");

        mRequestsReference = FirebaseDatabase.getInstance().getReference().child("requests");

        //Creates the adapter and messages arraylist
        usersList = new ArrayList<>();

        setListeners();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().length() < 5){
                    adapter.resetList();
                    return false;
                }

                adapter.filterList(newText.trim());

                return true;
            }
        });

        users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                CharSequence options[] = new CharSequence[]{"Send friend request", "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(AddFriendsActivity.this);
                builder.setTitle("Select options");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //Click event for each item
                        if (i == 0){
                            UserModel clickedUser = (UserModel) adapter.getItem(position);

                            HashMap<String, String> data = new HashMap<>();
                            data.put("sent_by", FirebaseAuth.getInstance().getCurrentUser().getUid());

                            mRequestsReference
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(clickedUser.getKey())
                                    .setValue(data);

                            mRequestsReference
                                    .child(clickedUser.getKey())
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(data);

                            startActivity(new Intent(AddFriendsActivity.this, MainActivity.class));
                            Toast.makeText(AddFriendsActivity.this, R.string.request_sent, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        adapter = new UsersAdapter(AddFriendsActivity.this, usersList);

        //Sets the arraylist adapter
        users.setAdapter(adapter);


        if (searchView.getQuery().length() > 4){
            adapter.filterList(searchView.getQuery().toString().trim());
        }

        adapter.notifyDataSetChanged();
    }

    private void setListeners() {
        mUsersDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                UserModel user = dataSnapshot.getValue(UserModel.class);
                usersList.add(user);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                UserModel user = dataSnapshot.getValue(UserModel.class);
                for (int i = 0; i < usersList.size(); i++) {
                    if (usersList.get(i).getKey().equals(user.getKey())) {
                        usersList.set(i, user);
                        adapter.notifyDataSetChanged();
                        return;
                    }
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void openMainActivity(View view){
        startActivity(new Intent(AddFriendsActivity.this, MainActivity.class));
        finish();
    }

}