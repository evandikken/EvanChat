package nl.evandikken.evanchat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import nl.evandikken.evanchat.models.FriendModel;
import nl.evandikken.evanchat.models.UserModel;

public class MainActivity extends AppCompatActivity {
    public static final String USER_KEY = "nl.evandikken.evanchat.USER_KEY";

    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mChatsDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFriendsList = findViewById(R.id.mainListView);

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("friends");
        mUsersDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users");

        mChatsDatabase = FirebaseDatabase.getInstance().getReference()
                .child("messages");

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<FriendModel, FriendsViewHolder> adapter = new FirebaseRecyclerAdapter<FriendModel, FriendsViewHolder>(
                FriendModel.class,
                R.layout.main_list_user_item,
                FriendsViewHolder.class,
                mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, FriendModel model, int position) {
                final String listUserId = getRef(position).getKey();

                mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("username").getValue().toString();
                        final String status = dataSnapshot.child("status").getValue().toString();
                        final String userImage = dataSnapshot.child("profile_picture").getValue().toString();

                        viewHolder.setName(username);
                        viewHolder.setStatus(status);
                        viewHolder.setImage(userImage);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                                intent.putExtra(MainActivity.USER_KEY, listUserId);
                                startActivity(intent);
                            }
                        });

                        viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                removeFriend(listUserId);
                                return false;
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        };

        mFriendsList.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        public void setName(String name) {
            TextView nameView = mView.findViewById(R.id.requestsListItemName);
            nameView.setText(name);
        }

        public void setStatus(String status) {
            TextView statusView = mView.findViewById(R.id.mainListItemStatus);
            statusView.setText(status);

        }

        public void setImage(String url) {
            CircleImageView imageView = mView.findViewById(R.id.requestListItemImage);
            if (url.equals("default")) {
                imageView.setImageResource(R.drawable.user_icon);
                return;
            }

            Picasso.get().load(url).placeholder(R.drawable.user_icon).into(imageView);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mainMenuAddFriend:
                openAddFriendsActivity();
                return true;
            case R.id.mainMenuFriendRequests:
                openFriendRequestsActivity();
                return true;
            case R.id.mainMenuAccountSettings:
                openAccountSettingsActivity();
                return true;
            case R.id.mainMenuLogOut:
                signOut(null);
                return true;
            default:
                return false;
        }
    }

    private void removeFriend(final String id) {
        CharSequence options[] = new CharSequence[]{getString(R.string.remove_friend), getString(R.string.cancel)};
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.select_options);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                //User wants to remove this friend
                if (i == 0) {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    mFriendsDatabase
                            .child(id)
                            .removeValue();

                    mUsersDatabase
                            .child(id)
                            .child("friends")
                            .child(uid)
                            .removeValue();

                    mChatsDatabase
                            .child(id)
                            .child(uid)
                            .removeValue();

                    mChatsDatabase
                            .child(uid)
                            .child(id)
                            .removeValue();

                    Toast.makeText(MainActivity.this, R.string.friend_removed, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                //User wants to cancel
                else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void signOut(View view) {
        FirebaseAuth.getInstance().signOut();

        Toast.makeText(MainActivity.this, R.string.you_have_been_signed_out, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openAccountSettingsActivity() {
        startActivity(new Intent(MainActivity.this, AccountSettingsActivity.class));
    }

    private void openAddFriendsActivity() {
        startActivity(new Intent(MainActivity.this, AddFriendsActivity.class));
    }

    private void openFriendRequestsActivity() {
        startActivity(new Intent(MainActivity.this, FriendRequestsActivity.class));
    }
}


