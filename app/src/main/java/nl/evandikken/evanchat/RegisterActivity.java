package nl.evandikken.evanchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import nl.evandikken.evanchat.utils.PasswordGenerator;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout mEmail;
    private TextInputLayout mEmail2;
    private TextInputLayout mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmail = findViewById(R.id.registerETEmail1);
        mEmail2 = findViewById(R.id.registerETEmail2);
        mUsername = findViewById(R.id.registerETUsername);
    }

    public void registerUser(View view) {
        final String email = mEmail.getEditText().getText().toString().trim().toLowerCase();
        final String email2 = mEmail2.getEditText().getText().toString().trim().toLowerCase();
        final String userName = mUsername.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(email2) || TextUtils.isEmpty(userName)) {
            Toast.makeText(RegisterActivity.this, R.string.credentials_not_entered, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.equals(email2)) {
            Toast.makeText(RegisterActivity.this, R.string.passwords_no_match, Toast.LENGTH_SHORT).show();
            return;
        }

        if (userName.length() < 5){
            Toast.makeText(RegisterActivity.this, R.string.username_not_long_enough, Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(RegisterActivity.this);
        dialog.setTitle(getString(R.string.registering_account));
        dialog.setMessage(getString(R.string.please_wait_registert));
        dialog.setCancelable(false);
        dialog.show();

        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference().child("users");

        PasswordGenerator passwordGenerator = new PasswordGenerator.PasswordGeneratorBuilder()
                .useDigits(true)
                .useLower(true)
                .useUpper(true)
                .build();
        String randomPassword = passwordGenerator.generate(15);

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, randomPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email);

                    DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                            .child("users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                    HashMap<String, String> data = new HashMap<>();
                    data.put("username", userName);
                    data.put("email", email);
                    data.put("profile_picture", "default");
                    data.put("status", "Hi there, I am using Evan Chat");
                    data.put("key", FirebaseAuth.getInstance().getCurrentUser().getUid());

                    database.setValue(data);
                    FirebaseAuth.getInstance().signOut();

                    Toast.makeText(RegisterActivity.this, R.string.verify_your_email, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    dialog.dismiss();
                } else {
                    //Toast.makeText(RegisterActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        });
    }

    public void openWelcomeActivity(View view) {
        //Open the Welcome activity
        Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}