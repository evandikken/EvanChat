package nl.evandikken.evanchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import nl.evandikken.evanchat.models.MessageModel;
import nl.evandikken.evanchat.models.UserModel;

public class ChatActivity extends AppCompatActivity {
    private DatabaseReference mMessagesDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mAllMessagesDatabase;

    private FirebaseAuth mAuth;

    private EditText userInput;
    private RecyclerView mMessagesList;

    private String mCurrentUserId;
    private String listUserId;
    private UserModel listUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userInput = findViewById(R.id.chatActivityInputField);

        final TextView mUsername = findViewById(R.id.chatActivityUsername);
        final CircleImageView mImage = findViewById(R.id.chatActivityImage);

        Intent intent = getIntent();
        this.listUserId = intent.getStringExtra(MainActivity.USER_KEY);

        mMessagesList = findViewById(R.id.chatActivityMessageList);
        mAuth = FirebaseAuth.getInstance();

        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mMessagesDatabase = FirebaseDatabase.getInstance().getReference()
                .child("messages")
                .child(mCurrentUserId)
                .child(listUserId);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users");

        mAllMessagesDatabase = FirebaseDatabase.getInstance().getReference()
                .child("messages");

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(new LinearLayoutManager(this));

        mUsersDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listUser = dataSnapshot.getValue(UserModel.class);

                mUsername.setText(listUser.getUsername());
                if (listUser.getProfile_picture().equals("default")){
                    mImage.setImageResource(R.drawable.user_icon);
                } else {
                    Picasso.get().load(listUser.getProfile_picture()).placeholder(R.drawable.user_icon).into(mImage);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mUsersDatabase
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("last_seen")
                .setValue("online");
        Log.i("LASTSEEN", "Set online value");

        FirebaseRecyclerAdapter<MessageModel, ChatActivity.MessagesViewHolder> adapter = new FirebaseRecyclerAdapter<MessageModel, MessagesViewHolder>(
                MessageModel.class,
                R.layout.chat_message,
                ChatActivity.MessagesViewHolder.class,
                mMessagesDatabase // change to mFriendsDatabase upon final implementation
        ) {

            @Override
            protected void populateViewHolder(final MessagesViewHolder viewHolder, final MessageModel model, int position) {
                mUsersDatabase.child(model.getSent_by()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserModel currentUser = dataSnapshot.getValue(UserModel.class);
                        viewHolder.setImage(currentUser.getProfile_picture());
                        viewHolder.setMessage(model.getMessage());
                        viewHolder.setTime(model.getDate());
                        viewHolder.setName(currentUser.getUsername());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mMessagesList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public static class MessagesViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public MessagesViewHolder(View itemView){
            super(itemView);
            this.mView = itemView;
        }

        public void setName(String name){
            TextView nameView = mView.findViewById(R.id.name_text_layout);
            nameView.setText(name);
        }

        public void setMessage(String message){
            TextView messageView = mView.findViewById(R.id.message_text_layout);
            messageView.setText(message);
        }

        public void setTime(String time){
            TextView timeView = mView.findViewById(R.id.time_text_layout);
            timeView.setText(time);

        }

        public void setImage(String url){
            CircleImageView imageView = mView.findViewById(R.id.message_profile_layout);
            if (url.equals("default")){
                imageView.setImageResource(R.drawable.user_icon);
                return;
            }

            Picasso.get().load(url).placeholder(R.drawable.user_icon).into(imageView);

        }
    }

    public void sendMessage(View view){
        String message = userInput.getText().toString().trim();
        if (TextUtils.isEmpty(message)){
            return;
        }

        userInput.setText("");

        String key = mAllMessagesDatabase.child(mCurrentUserId).child(listUserId).push().getKey();

        HashMap<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("sent_by", mAuth.getCurrentUser().getUid());
        data.put("key", key);
        data.put("date", LocalDate.now().toString());

        mAllMessagesDatabase.child(mCurrentUserId).child(listUserId).child(key).setValue(data);
        mAllMessagesDatabase.child(listUserId).child(mCurrentUserId).child(key).setValue(data);
        mMessagesList.smoothScrollToPosition(mMessagesList.getBottom() - 1);
    }

    public void sendDocument(View view){
        //TODO: Implement document / image sending through chat activity
    }

    public void openMainActivity(View view){
        startActivity(new Intent(ChatActivity.this, MainActivity.class));
        finish();
    }
}