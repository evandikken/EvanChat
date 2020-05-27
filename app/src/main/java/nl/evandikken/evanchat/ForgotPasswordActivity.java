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
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputLayout mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mEmail = findViewById(R.id.forgotPasswordETEmail);
    }

    public void openLoginActivity(View view){
        //Open the Welcome activity
        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void sendPasswordReset(View view){
        String email = mEmail.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(ForgotPasswordActivity.this, R.string.credentials_not_entered, Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(ForgotPasswordActivity.this);
        dialog.setTitle(getString(R.string.sending_email));
        dialog.setMessage(getString(R.string.please_wait_while_we_sent_email));
        dialog.setCancelable(false);
        dialog.show();

        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ForgotPasswordActivity.this, R.string.email_sent, Toast.LENGTH_SHORT).show();
                    openLoginActivity(null);
                    dialog.dismiss();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }
}