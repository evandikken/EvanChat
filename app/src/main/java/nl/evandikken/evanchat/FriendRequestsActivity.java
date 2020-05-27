package nl.evandikken.evanchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import nl.evandikken.evanchat.models.InviteModel;

public class FriendRequestsActivity extends AppCompatActivity {
    private RecyclerView mFriendsList;

    private DatabaseReference mRequestsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        mFriendsList = findViewById(R.id.friendRequestList);
        mAuth = FirebaseAuth.getInstance();

        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mRequestsDatabase = FirebaseDatabase.getInstance().getReference()
                .child("requests")
                .child(mCurrentUserId);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users");

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<InviteModel, FriendRequestsActivity.RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<InviteModel, RequestsViewHolder>(
                InviteModel.class,
                R.layout.requests_list_request_item,
                FriendRequestsActivity.RequestsViewHolder.class,
                mRequestsDatabase
        ) {

            @Override
            protected void populateViewHolder(final RequestsViewHolder viewHolder, InviteModel model, int position) {
                final String listUserId = getRef(position).getKey();
                InviteModel currentInvite = getItem(position);
                viewHolder.setInviteModel(currentInvite);
                viewHolder.setUserIds(mCurrentUserId, listUserId);
                viewHolder.setButtons();

                mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("username").getValue().toString();
                        final String userImage = dataSnapshot.child("profile_picture").getValue().toString();

                        viewHolder.setName(username);
                        viewHolder.setImage(userImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        };

        mFriendsList.setAdapter(adapter);
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        InviteModel inviteModel;
        String listUserId;
        String currentUserId;

        public RequestsViewHolder(View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        public void setUserIds(String currentUser, String listUser) {
            this.listUserId = listUser;
            this.currentUserId = currentUser;
        }

        public void setInviteModel(InviteModel model) {
            this.inviteModel = model;
        }

        public void setButtons() {
            Button acceptButton = itemView.findViewById(R.id.requestListItemButtonAccept);
            Button denyButton = itemView.findViewById(R.id.requestListItemButtonDeny);

            final DatabaseReference requestsDatabase = FirebaseDatabase.getInstance().getReference()
                    .child("requests");
            final DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference()
                    .child("users");

            //Invite is still pending
            acceptButton.setVisibility(View.VISIBLE);
            denyButton.setVisibility(View.VISIBLE);

            if (inviteModel.getSent_by().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                //USERS OWN REQUEST
                acceptButton.setVisibility(View.INVISIBLE);
                denyButton.setText(R.string.cancel);

                //User wants to cancel his own request
                denyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestsDatabase
                                .child(currentUserId)
                                .child(listUserId)
                                .removeValue();
                        requestsDatabase
                                .child(listUserId)
                                .child(currentUserId)
                                .removeValue();
                    }
                });
            } else {
                //REQUEST BY OTHER USER
                denyButton.setText(R.string.deny);

                //User wants to accept the request
                acceptButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestsDatabase
                                .child(currentUserId)
                                .child(listUserId)
                                .removeValue();
                        requestsDatabase
                                .child(listUserId)
                                .child(currentUserId)
                                .removeValue();

                        HashMap<String, String> data = new HashMap<>();
                        data.put("since", LocalDate.now().toString());

                        usersDatabase
                                .child(currentUserId)
                                .child("friends")
                                .child(listUserId)
                                .setValue(data);

                        usersDatabase
                                .child(listUserId)
                                .child("friends")
                                .child(currentUserId)
                                .setValue(data);
                    }
                });

                //User wants to deny the request
                denyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestsDatabase
                                .child(currentUserId)
                                .child(listUserId)
                                .removeValue();
                        requestsDatabase
                                .child(listUserId)
                                .child(currentUserId)
                                .removeValue();
                    }
                });
            }
        }

        public void setName(String name) {
            TextView nameView = mView.findViewById(R.id.requestsListItemName);
            nameView.setText(name);
        }

        public void setImage(String url) {
            CircleImageView imageView = mView.findViewById(R.id.requestListItemImage);
            imageView.setVisibility(View.VISIBLE);
            if (url.equals("default")) {
                imageView.setImageResource(R.drawable.user_icon);
                return;
            }

            Picasso.get().load(url).placeholder(R.drawable.user_icon).into(imageView);
        }
    }

    public void openMainActivity(View view){
        startActivity(new Intent(FriendRequestsActivity.this, MainActivity.class));
        finish();
    }
}