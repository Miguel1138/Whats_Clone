package com.miguel_santos.com.example.whats_clone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private EditText reg_username;
    private EditText reg_email;
    private EditText reg_password;
    private Button btnRegister;
    private Button btnSelected_userphoto;
    private ImageView reg_imgPhoto;


    private Uri selectedUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        reg_username = findViewById(R.id.reg_username);
        reg_email = findViewById(R.id.reg_email);
        reg_password = findViewById(R.id.reg_password);
        btnRegister = findViewById(R.id.login_btn_enter);
        btnSelected_userphoto = findViewById(R.id.btnUser_photo);
        reg_imgPhoto = findViewById(R.id.imgPhoto);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userCreate();
            }
        });

        btnSelected_userphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoSelected();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            selectedUri = data.getData();
            Bitmap bitmap;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedUri);
                reg_imgPhoto.setImageDrawable(new BitmapDrawable(bitmap));
                btnSelected_userphoto.setAlpha(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } // CREATE METHOD IN CASE THE REQUEST CODE FAILS.
    }

    private void photoSelected() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/+");
        startActivityForResult(intent, 0);
    }

    private void userCreate() {
        String username = reg_username.getText().toString();
        String email = reg_email.getText().toString();
        String password = reg_password.getText().toString();

        if (username == null || username.isEmpty() || email == null || email.isEmpty() ||
                password == null || password.isEmpty()) {
            Toast.makeText(this, "Erro: Preencha os campos de nome,  email e senha", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveUserInFirebase();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("teste", e.getMessage());
                    }
                });


    }

    private void saveUserInFirebase() {

        String filename = UUID.randomUUID().toString();
        final StorageReference ref = FirebaseStorage.getInstance().getReference("/images/" + filename);
        ref.putFile(selectedUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                String userID = FirebaseAuth.getInstance().getUid();
                                String username = reg_username.getText().toString();
                                String profileUrl = uri.toString();

                                User user = new User(userID, username, profileUrl);

                                FirebaseFirestore.getInstance().collection("users")
                                        .document(userID)
                                        .set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent intent = new Intent(RegisterActivity.this, MessageActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("teste", e.getMessage(), e);
                                            }
                                        });

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("teste", e.getMessage());
                    }
                });
    }
}