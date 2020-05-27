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

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = findViewById(R.id.loginETEmail);
        mPassword = findViewById(R.id.loginETPassword);
    }

    public void loginUser(View view) {
        //Get the string values from the input fields
        String email = mEmail.getEditText().getText().toString();
        String password = mPassword.getEditText().getText().toString();

        //Check if any of the input fields are empty
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, R.string.credentials_not_entered, Toast.LENGTH_SHORT).show();
            return;
        }

        //Create a progress dialog to show the user we are logging in
        final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
        dialog.setTitle(getString(R.string.logging_in));
        dialog.setMessage(getString(R.string.please_wait_login));
        dialog.setCancelable(false);
        dialog.show();

        //Login the user with the given input
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //User is logged in, let them know
                    Toast.makeText(LoginActivity.this, R.string.successfully_logged_in, Toast.LENGTH_SHORT).show();

                    //Open the main activity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    dialog.dismiss();
                } else {
                    //User is not logged in, let them know
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }

    public void openWelcomeActivity(View view) {
        //Open the Welcome activity
        Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void openForgotPasswordActivity(View view){
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
    }
}