package nl.evandikken.evanchat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import nl.evandikken.evanchat.models.UserModel;

public class AccountSettingsActivity extends AppCompatActivity {
    private String uid;
    private UserModel currentUser;

    private CircleImageView image;
    private TextView username, status;

    private StorageReference storageReference;
    private DatabaseReference profilePictureReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        image = findViewById(R.id.accountSettingsImageProfile);
        username = findViewById(R.id.accountSettingsTVUsername);
        status = findViewById(R.id.accountSettingsTVStatus);

        storageReference = FirebaseStorage.getInstance().getReference();
        profilePictureReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(uid)
                .child("profile_picture");

        final DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser= dataSnapshot.getValue(UserModel.class);

                if (currentUser.getProfile_picture().equals("default")) {
                    image.setImageResource(R.drawable.user_icon);
                } else {
                    Picasso.get().load(currentUser.getProfile_picture()).placeholder(R.drawable.user_icon).into(image);
                }

                username.setText(currentUser.getUsername());
                status.setText(currentUser.getStatus());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void openMainActivity(View view){
        Intent intent = new Intent(AccountSettingsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    public void pickImage(View view) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(AccountSettingsActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final ProgressDialog progress = new ProgressDialog(AccountSettingsActivity.this);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progress.setTitle(getString(R.string.uploading_image));
                progress.setMessage(getString(R.string.please_wait_upload_profile_image));
                progress.setCancelable(false);
                progress.show();

                Uri resultUri = result.getUri();
                File thumbFilePath = new File(resultUri.getPath());
                Bitmap thumbBitmap = null;
                try {
                    thumbBitmap = new Compressor(AccountSettingsActivity.this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumbFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                StorageReference filePath = storageReference.child("profile_images").child(uid + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            final String download_url = task.getResult().getDownloadUrl().toString();
                            profilePictureReference.setValue(download_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progress.dismiss();
                                        Toast.makeText(AccountSettingsActivity.this, R.string.image_uploaded, Toast.LENGTH_SHORT).show();
                                    } else {
                                        progress.dismiss();
                                        Toast.makeText(AccountSettingsActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                                        Log.i("image upload", task.getResult().toString());
                                    }
                                }
                            });
                        } else {
                            progress.dismiss();
                            Toast.makeText(AccountSettingsActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                            Log.i("image upload", task.getResult().toString());
                        }
                    }
                });
            }

        }
    }

    public void updateStatus(View view) {
        final DatabaseReference statusReference = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(uid)
                .child("status");

        //Inflate a new dialog view
        LayoutInflater inflater = LayoutInflater.from(AccountSettingsActivity.this);
        View promptsView = inflater.inflate(R.layout.text_edit_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AccountSettingsActivity.this);

        // Set the view into the dialog builder
        alertDialogBuilder.setView(promptsView);

        //Find the userInput view;
        final TextInputLayout userInput = promptsView.findViewById(R.id.textEditMessageDialogET);
        final TextView maxCharacter = promptsView.findViewById(R.id.textEdditMessageMaxCharacter);

        userInput.setHint(getString(R.string.new_status));

        maxCharacter.setText("0/40");

        userInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                maxCharacter.setText(userInput.getEditText().getText().length() + "/40");
            }
        });

        // set dialog message
        alertDialogBuilder.setTitle(R.string.edit_status);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!TextUtils.isEmpty(userInput.getEditText().getText().toString().trim())) {
                            statusReference.setValue(userInput.getEditText().getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(AccountSettingsActivity.this, R.string.status_updated, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AccountSettingsActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            statusReference.setValue("-").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(AccountSettingsActivity.this, R.string.status_updated, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AccountSettingsActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}