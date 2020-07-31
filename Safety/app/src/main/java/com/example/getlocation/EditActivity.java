package com.example.getlocation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;


public class EditActivity extends AppCompatActivity {
    private EditText nameEditText, professionEditText, workEditText, ageEditText;
    String name;
    private EditText phoneEditText, emailEditText;
    private Button registerButton;
    String UniqueID;

    String downloadPicUrl;
    private ImageView profilePic;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference,storageReferenceFetchImage;


    private FirebaseDatabase rootNode;
    private DatabaseReference reference;//for sub-elements of root nodes

    private static final String USERS = "users";
    private String TAG = "EditActivity";
    private String username, fname, email, profession, phone, workplace,age,profilePicUri;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mAuth= FirebaseAuth.getInstance();
        UniqueID = mAuth.getCurrentUser().getUid();//get the unique id of user


        nameEditText = findViewById(R.id.fullname_edittext);
        professionEditText = findViewById(R.id.profession_edittext);
        workEditText = findViewById(R.id.workplace_edittext);
        phoneEditText = findViewById(R.id.phone_edittext);
        emailEditText = findViewById(R.id.email_edittext);
        profilePic = findViewById(R.id.pic_imageview);
        registerButton = findViewById(R.id.register_button);
        ageEditText=findViewById(R.id.age_editText);

        show();


        storage= FirebaseStorage.getInstance();
        storageReference=storage.getReference();
        storageReferenceFetchImage=storage.getReferenceFromUrl("gs://shieldappsih.appspot.com/Profile/"+UniqueID+"/").child("profile_pic.jpg");

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });





        rootNode = FirebaseDatabase.getInstance();//call to the root node
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        reference = rootNode.getReference("Users");

        registerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //get All Values
                fname = nameEditText.getText().toString();
                email = emailEditText.getText().toString();
                phone = phoneEditText.getText().toString();
                profession = professionEditText.getText().toString();
                workplace = workEditText.getText().toString();
                age=ageEditText.getText().toString();
                profilePicUri="";

                UserHelperClass helperClass=new UserHelperClass(fname,profilePicUri,profession, workplace,age,phone,email);
                reference.child(UniqueID).setValue(helperClass);//creates a child with Unique id

                Toast.makeText(EditActivity.this, "Saved!",Toast.LENGTH_SHORT).show();
                Intent ii=new Intent(EditActivity.this,ProfileActivity.class);
                startActivity(ii);
            }
        });



    }

    private void choosePicture() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }



    public void show(){
        mAuth= FirebaseAuth.getInstance();
        final String currentUserUid = mAuth.getCurrentUser().getUid();//get the unique id of user
        reference= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email=snapshot.child("email").getValue().toString();
                String name=snapshot.child("name").getValue().toString();
                String phone=snapshot.child("phone").getValue().toString();
                String profession=snapshot.child("profession").getValue().toString();
                String age=snapshot.child("age").getValue().toString();
                String workplace=snapshot.child("workplace").getValue().toString();

                //String photoLink=snapshot.child("profilePhoto").getValue().toString();
                //Picasso.get().load(photoLink).into(profilePic);
                //String profilePicture=snapshot.child("profilePic").getValue().toString(); //-----------------Fetched profile pic id from real time database
                fetchImage();


                emailEditText.setText(email);
                nameEditText.setText(name);
                phoneEditText.setText(phone);
                professionEditText.setText(profession);
                ageEditText.setText(age);
                workEditText.setText(workplace);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageUri=data.getData();
            profilePic.setImageURI(imageUri);
            uploadPicture();
        }
    }

    private void uploadPicture() {

        final ProgressDialog pd=new ProgressDialog(this);
        pd.setTitle("Uploading Image...");
        pd.show();

        StorageReference riversRef = storageReference.child("Profile/"+UniqueID+"/profile_pic.jpg");

        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                        Snackbar.make(findViewById(android.R.id.content),"Pic uploaded!",Snackbar.LENGTH_LONG).show();
                        //downloadPicUrl=taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();   //get download link of pic


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(),"Wasn't Uploaded",Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progressPercent=(100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        pd.setMessage("Percentage : " + (int) progressPercent + "%");
                    }
                });
    }

public void fetchImage() {
    final File file;
    try {
        file = File.createTempFile("image","jpg");
        storageReferenceFetchImage.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bitmap= BitmapFactory.decodeFile(file.getAbsolutePath());
                profilePic.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    } catch (IOException e) {
        e.printStackTrace();
    }

}
}