package com.example.getlocation;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static android.widget.Toast.LENGTH_LONG;

public class var_get extends AppCompatActivity{
    String girlUniqueIdplusDate,girlUniqueId, boyUniqueId;
    String peopleHelpingNow;
    Double v1,v2;
    String v3 ,v4;
    int num=0;
    int temp=0,counter=0;
    String category;
    private TextView number_save,uniqueid_save,age_save,name_save,refresh_save,safeNo_save,distanceAndDuration;
    private Button back_btn , link_save;
    private ImageView image_save;

    private Handler mHandler=new Handler();
    //Latitude and longitude.................
    double girlLat1=0, girlLon1=0, boyLat2=0, boyLon2=0;
    double lat1,long1;
    int flag=0;
    String sType;




    //Firebase wale....
    FirebaseDatabase database;
    DatabaseReference rootReference,dangerReference, userReference, girlReference;
    private FirebaseStorage storage;
    private StorageReference storageReferenceFetchImage;

    //------------------------------------Latitude, Longitude Vars--------------------------------------
    public String latitude1, longitude1;
    public String locationLink;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_var_get);
//------------------------------------------------------Location----------------------------------------




















//....................................................value dalo..........................................
        distanceAndDuration=findViewById(R.id.distanceAndDuration);
        number_save = findViewById(R.id.number_save);
        uniqueid_save = findViewById(R.id.uniqueid_save);
        name_save=findViewById(R.id.name_save);
        age_save=findViewById(R.id.age_save);
        image_save=findViewById(R.id.image_save);
        refresh_save=findViewById(R.id.refresh_save);
        safeNo_save=findViewById(R.id.SafeNumber_save);
        back_btn =findViewById(R.id.var_btn);
        boyUniqueId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        Button iwillHelp=findViewById(R.id.iWillHelp);
        link_save= findViewById(R.id.go_to_link);


        final TextView linkTextView = findViewById(R.id.puranjanTextView);


        if (getIntent().hasExtra("category")) {
            String smslink = getIntent().getStringExtra("link");
            category = getIntent().getStringExtra("category");
            girlUniqueIdplusDate= getIntent().getStringExtra("UniqueId");
            girlUniqueId = girlUniqueIdplusDate.substring(0, girlUniqueIdplusDate.length() - 20);
            locationLink = smslink;
            //Link.setText(smslink);
        }





        //............
        database=FirebaseDatabase.getInstance();
        rootReference=database.getReference();
        dangerReference=rootReference.child("inDanger").child(girlUniqueIdplusDate).child(boyUniqueId);
        userReference=rootReference.child("Users");
        girlReference=rootReference.child("inDanger").child(girlUniqueIdplusDate);

    //.........................................Add Functions from below................................................................


     //............................location nikalne ke function se variable v1 aur v2 ki value is comment lines ke bivh daal..............................



        //show dialog
        /**final ProgressDialog pd=new ProgressDialog(this);
        pd.setTitle("Getting Your Location...");
        pd.show();
        class LocationBroadcastReciver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent){
                if(intent.getAction().equals("act_location")){
                    double lat = intent.getDoubleExtra("Latitude", 0f);
                    double longitude = intent.getDoubleExtra("Longitude", 0f);
                    //Toast.makeText(MainActivity.this, "Lat :" + lat + "Long :" + longitude, Toast.LENGTH_LONG).show();

                    String strlat = Double.toString(lat);
                    String strlongitude = Double.toString(longitude);

                    v3 = strlat;
                    v4 = strlongitude;

                    Toast.makeText(var_get.this,"no1", LENGTH_LONG);
                }
            }
        }
        //............................location nikalne ke function se variable v1 aur v2 ki value is comment lines ke bivh daal...............................
**/

        Toast.makeText(var_get.this,"no2", LENGTH_LONG);

        boyLat2=30.2909096;//my coordinate
        boyLon2=78.0017146;




        iwillHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                willYouHelp();
            }
        });
       refresh_save.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if (temp==1){
                   isSheSafe();
               }
               else{
                   Toast.makeText(var_get.this,"You need to select \" I Will Help \" to mark the person safe", LENGTH_LONG).show();
               }
           }
       });
       //if (!(girlUniqueId.equals(boyUniqueId)))


        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            exit();
            }
        });
        refresh();

    }





    public void willYouHelp(){
        AlertDialog.Builder builder
                = new AlertDialog
                .Builder(var_get.this);
        builder.setMessage("Will You HELP ?");

        // Set Alert Title
        builder.setTitle("Someone is in Danger!!");

        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(false);

        // Set the positive button with yes name OnClickListener method is use of DialogInterface interface.
        builder
                .setPositiveButton(
                        "Yes",
                        new DialogInterface
                                .OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which)
                            {
                                temp=1;
                                userReference.child(boyUniqueId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String name=snapshot.child("name").getValue().toString();
                                    dangerReference.child("Name").setValue(name);
                                    dangerReference.child("latt2").setValue(boyLat2);
                                    dangerReference.child("long2").setValue(boyLon2);
                                    addDistanceToTextView();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                                Toast.makeText(var_get.this,"The distance shown here is Approximate distance", LENGTH_LONG).show();
                                // When the user click yes button
                                // then app will close
                            }
                        });

        // Set the Negative button with No name
        // OnClickListener method is use
        // of DialogInterface interface.
        builder
                .setNegativeButton(
                        "No",
                        new DialogInterface
                                .OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which)
                            {
                                // If user click no
                                // then dialog box is canceled.
                                dialog.cancel();
                            }
                        });

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();

        // Show the Alert Dialog box
        alertDialog.show();
    }
    public void isSheSafe(){
        AlertDialog.Builder builder // Create the object of AlertDialog Builder class
                = new AlertDialog
                .Builder(var_get.this);
        builder.setMessage("Do you confirm that she is safe now?");  // Set the message show for the Alert time
        // Set Alert Title
        builder.setTitle("Exit");
        builder.setCancelable(false);
        builder
                .setPositiveButton(
                        "Confirm",
                        new DialogInterface
                                .OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which)
                            { markSafe();
                                markSafe();
                            }
                        });
        builder
                .setNegativeButton(
                        "Don't Know",
                        new DialogInterface
                                .OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which)
                            {
                                // If user click no
                                // then dialog box is canceled.
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = builder.create(); // Create the Alert dialog
        alertDialog.show();  // Show the Alert Dialog box
    }
    public void exit(){
        AlertDialog.Builder builder // Create the object of AlertDialog Builder class
                = new AlertDialog
                .Builder(var_get.this);
        builder.setMessage("Do you confirm that she is safe now?");  // Set the message show for the Alert time
        // Set Alert Title
        builder.setTitle("Exit");
        builder.setCancelable(false);
        builder
                .setPositiveButton(
                        "Yes",
                        new DialogInterface
                                .OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which)
                            { markSafe();
                                Intent ii=new Intent(var_get.this,MainActivity.class);
                                startActivity(ii);
                            }
                        });
        builder
                .setNegativeButton(
                        "No",
                        new DialogInterface
                                .OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which)
                            {
                                // If user click no
                                // then dialog box is canceled.
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = builder.create(); // Create the Alert dialog
        alertDialog.show();  // Show the Alert Dialog box
    }


    public void checkNoOfPeopleHelpingNow(){
        rootReference.child("inDanger").child(girlUniqueIdplusDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                num=((int) snapshot.getChildrenCount())-3;
                number_save.setText(Integer.toString(num));
            }






            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void getNoOfPeopleWhoMarkedSafe(){
        girlReference.child("Safe").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                num=((int) snapshot.getChildrenCount());
                safeNo_save.setText(Integer.toString(num));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void addGirlData(){
        userReference.child(girlUniqueId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name=snapshot.child("name").getValue().toString();
                String age=snapshot.child("age").getValue().toString();
                name_save.setText("Name : "+name);
                age_save.setText("Age : "+age);
                uniqueid_save.setText(girlUniqueId);
                fetchImage();

            }@Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void refresh(){
        checkNoOfPeopleHelpingNow();
        addGirlData();
        fetchCoordinatesOfGirl();
        getNoOfPeopleWhoMarkedSafe();
        if (temp==1) {
            setCoordinatesOfBoy();
        }

    }



    public void fetchImage() {
        storage= FirebaseStorage.getInstance();
        storageReferenceFetchImage=storage.getReferenceFromUrl("gs://shieldappsih.appspot.com/Profile/"+girlUniqueId+"/").child("profile_pic.jpg");

        final File file;
        try {
            file = File.createTempFile("image","jpg");
            storageReferenceFetchImage.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap= BitmapFactory.decodeFile(file.getAbsolutePath());
                    image_save.setImageBitmap(bitmap);
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
    public void markSafe(){
        userReference.child(boyUniqueId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name=snapshot.child("name").getValue().toString();
                girlReference.child("Safe").child(boyUniqueId).setValue(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    public float getDistance(double lat1, double lon1, double lat2, double lon2) {
        android.location.Location homeLocation = new android.location.Location("");
        homeLocation .setLatitude(lat1);
        homeLocation .setLongitude(lon1);

        android.location.Location targetLocation = new android.location.Location("");
        targetLocation .setLatitude(lat2);
        targetLocation .setLongitude(lon2);

        float distanceInMeters =  targetLocation.distanceTo(homeLocation);

        return (distanceInMeters/1000) ;
    }
    public void addDistanceToTextView(){
        girlReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final String latt1=snapshot.child("latt1").getValue().toString();
                final String longg1=snapshot.child("long1").getValue().toString();



                dangerReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String lat2=snapshot.child("latt2").getValue().toString();
                        String lon2=snapshot.child("long2").getValue().toString();

                        Float f=getDistance(Double.parseDouble(latt1),Double.parseDouble(longg1),Double.parseDouble(lat2),Double.parseDouble(lon2));
                        BigDecimal bd = new BigDecimal(Float.toString(f));
                        bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
                        distanceAndDuration.setText("Distance : "+bd+ " Kilometers");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void setCoordinatesOfBoy(){
        dangerReference.child("latt2").setValue(boyLat2);
        dangerReference.child("long2").setValue(boyLon2);
    }
    public void fetchCoordinatesOfGirl(){
        girlReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final String latt1=snapshot.child("latt1").getValue().toString();
                final String longg1=snapshot.child("long1").getValue().toString();


                class LocationBroadcastReciver extends BroadcastReceiver {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if(intent.getAction().equals("act_location")){
                            String link = "www.google.com/maps/search/?api=1&query=" + latt1 + "," + longg1;
                            locationLink = link;
                        }
                    }
                }
                link_save.setText(locationLink);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


}


