package com.example.getlocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    // Date and Time
    Calendar c = Calendar.getInstance();
    SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yy-hh:mm:ss aa");
    String datetime = dateformat.format(c.getTime());

    //initializing a button for emergency contacts
    Button button;
    Button notifybtn;
    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";

    // Declaring Camera Button and Texture View
    private Button btnCapture,btnDomesticViolence,profile;
    private TextureView textureView;

    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // Declaring Get Data Button
    private Button btnGD;

    // Check orientation
    private static final SparseIntArray ORIENTATIONS  = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }

    // Camera
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;

    // Save File
    private File file;
    private static final int REQUEST_CAMERA_PREMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice=null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //------------------------------------------Bottom Navigation----------------------------
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setSelectedItemId(R.id.homeNav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.emergency:
                        Intent intent = new Intent(getApplicationContext(), SMSsender.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        break;

                    case R.id.domestic:
                        Intent intent1 = new Intent(getApplicationContext(),DomesticVoilence.class);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent1);
                        overridePendingTransition(0,0);
                        break;

                    case R.id.homeNav:
                        break;

                    case R.id.repcomplain:
                        Intent intent2 = new Intent(getApplicationContext(),reportbutton.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent2);
                        overridePendingTransition(0,0);
                        break;

                    case R.id.userProfile:
                        Intent intent3 = new Intent(getApplicationContext(), ProfileActivity.class);
                        intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent3);
                        overridePendingTransition(0,0);
                        break;
                }
                return false;
            }
        });





        mRequestQue = Volley.newRequestQueue(this);
        FirebaseMessaging.getInstance().subscribeToTopic("news");

        Button btttt=findViewById(R.id.send_push_button);
        btttt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendnotification();
            }
        });

        notifybtn = (Button) findViewById(R.id.smsandnotificationbutton);
        notifybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendnotification();
                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS);

                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    MyMessage();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, 0);
                }
            }
        });


        // Camera Button and TextureView
        textureView = findViewById(R.id.textureView);
        assert textureView !=null;


        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

            }
        });



        // Taking Picture
        btnCapture = findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        //Checking for Permission
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //REQUEST PERMISSION SINCE PERMISSION IS NOT GRANTED
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else{
            startService();
        }

    }
    // -----------------------------------------------------------> Domestic Voilence <-------------------------------------------------------
    private void openDomesticVoilence() {
        Intent intent = new Intent(this, DomesticVoilence.class);
        startActivity(intent);
    }

    // -------------------------------------------------------------Get Data Opening------------------------------------------------------------------
    public void openGetData() {
        Intent intent = new Intent(this, CasesReport.class);
        startActivity(intent);
    }

    // Creating a method to start the location service
    void startService() {
        LocationBroadcastReciver reciver = new LocationBroadcastReciver();
        IntentFilter filter = new IntentFilter("act_location");
        registerReceiver(reciver, filter);
        Intent intent = new Intent(MainActivity.this, LocationService.class);
        startService(intent);
    }

    // To Check If User Has Provided Required Persmisions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startService();
                }
                else{
                    Toast.makeText(this, ":Permission Needed", Toast.LENGTH_LONG).show();

                }
        }
    }

    //-----------------------------------------------< Locations Service ~ bhaji <--------------------------------------------------------

    public String smslink = "Location Service Off";
    public String v1, v2;

    public class LocationBroadcastReciver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("act_location")){
                double lat = intent.getDoubleExtra("Latitude", 0f);
                double longitude = intent.getDoubleExtra("Longitude", 0f);
                //Toast.makeText(MainActivity.this, "Lat :" + lat + "Long :" + longitude, Toast.LENGTH_LONG).show();

                String strlat = Double.toString(lat);
                String strlongitude = Double.toString(longitude);

                v1 = strlat;
                v2 = strlongitude;
                String link = "www.google.com/maps/search/?api=1&query=" + strlat + "," + strlongitude;

                //Toast.makeText(MainActivity.this, link, Toast.LENGTH_LONG).show();

                smslink = link;
                //sendCurrentLocationToSecondActivity();
            }
        }
    }


    //---------------------------------------------< Camera Service ~ milannzz <------------------------------------------------

    private void takePicture() {
        if(cameraDevice == null)
            return;
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSize = null;
            if(characteristics != null)
                jpegSize = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);

            // ----------> Capture Image With Custom Size <-----------
            int width = 640;
            int height = 480;
            if(jpegSize != null && jpegSize.length > 0 )
            {
                width = jpegSize[0].getWidth();
                height = jpegSize[0].getWidth();
            }

            final ImageReader reader = ImageReader.newInstance(width,height, ImageFormat.JPEG,1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(cameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Check Rotation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation));

            // --------> Folder Creation <--------------
            File root = new File(getCacheDir()+"/shieldapp/images");
            if(!root.exists()){
                root.mkdirs();
                if(!root.mkdirs()){
                    Toast.makeText(MainActivity.this, "Failed to create directory", Toast.LENGTH_LONG).show();
                }
            }
            // -----------> File Creation <--------------

            file = new File(getCacheDir()+"/shieldapp/images/testing.jpg");

            ImageReader.OnImageAvailableListener readerLister = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(image != null){
                            image.close();
                        }
                    }

                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream outputStream = null;
                    try{
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    }finally {
                        if(outputStream != null)
                        {
                            outputStream.close();
                        }
                    }

                }
            };

            reader.setOnImageAvailableListener(readerLister,mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try{
                        cameraCaptureSession.capture(captureBuilder.build(),captureListener,mBackgroundHandler);
                    } catch (CameraAccessException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            },mBackgroundHandler);

        } catch (CameraAccessException e){
            e.printStackTrace();
        };

        //---------------------------------------------> Upload Image to Firebase ~ milannzz <------------------------------------------------

        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        final String UniqueID = mAuth.getCurrentUser().getUid();

        StorageReference storageReference;
        storageReference  = FirebaseStorage.getInstance().getReference();

        Uri upfile = Uri.fromFile(file);
        StorageReference riversRef = storageReference.child("images/"+UniqueID+"/"+"IMG"+datetime+".jpg");

        riversRef.putFile(upfile)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Handle sucessful uploads
                        Toast.makeText(MainActivity.this, "Image Uploaded", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(MainActivity.this, "Upload Error", Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void createCameraPreview() {
        try{
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert  texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if(cameraDevice == null)
            Toast.makeText(this, "Camera Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(),null,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            //Check realtime permission if run higher API 23
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },REQUEST_CAMERA_PREMISSION);
                return;
            }
            manager.openCamera(cameraId,stateCallback,null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable())
            openCamera();
        else
            textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread= null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    // -------------------------------------> SMS Service ~ muku <--------------------------------------------------------

    public void MyMessage() {

        Intent intent = getIntent();
        int n_o_c = intent.getIntExtra(SMSsender.EXTRA_NUMBER3, 0);
        String m1 = intent.getStringExtra(SMSsender.EXTRA_NUMBER);
        String m2 = intent.getStringExtra(SMSsender.EXTRA_NUMBER1);
        String m3 = intent.getStringExtra(SMSsender.EXTRA_NUMBER2);

        String message = intent.getStringExtra(SMSsender.EXTRA_TEXT);

        if (n_o_c >= 1) {

            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(m1);
            arrayList.add(m2);
            arrayList.add(m3);

            SmsManager smsManager = SmsManager.getDefault();

            //String l= smslink;

            for (String string : arrayList) {
                smsManager.sendTextMessage(string, null, message + " " + smslink, null, null);
            }
            Toast.makeText(this, "sent", Toast.LENGTH_SHORT).show();

        } else {
            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add("8630199070");
            arrayList.add("7983105956");
            //arrayList.add("8630199070");
            SmsManager smsManager = SmsManager.getDefault();

            String l = smslink;

            for (String string : arrayList) {
                smsManager.sendTextMessage(string, null, "Help!" + smslink, null, null);
            }

            Toast.makeText(this, "message sent to default contacts please press emergency contacts first.", Toast.LENGTH_SHORT).show();
        }
    }


    private void sendnotification() {
        String UniqueID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        String titleNotification;


        UniqueID=UniqueID.concat(datetime);
        DatabaseReference safeRef=FirebaseDatabase.getInstance().getReference().child("inDanger").child(UniqueID);
        safeRef.child("Safe").setValue("0");
        safeRef.child("latt1").setValue(v1);
        safeRef.child("long1").setValue(v2);


        JSONObject json = new JSONObject();
        try {
            json.put("to","/topics/"+"news");
            JSONObject notificationObj = new JSONObject();

            notificationObj.put("title","Shield");
            notificationObj.put("body","Save Me!! I am in Danger");
            JSONObject extraData = new JSONObject();
            Toast.makeText(MainActivity.this,datetime,Toast.LENGTH_LONG).show();
            extraData.put("UniqueId",UniqueID);
            extraData.put("link",smslink);
            extraData.put("category","Shoes");



            json.put("notification",notificationObj);
            json.put("data",extraData);


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("MUR", "onResponse: ");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("MUR", "onError: "+error.networkResponse);
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AAAAfIXwoyU:APA91bFlV4Lknz7g4lVtmVRmMZurvC3QtmSdB2e1GEQcHmzE7BOUoV-RSsErhwnaznanhr57Urz347rEQTv9P9UPzm-Vn5Ns-j-OrBRPQb1bMTf9-NIBL9W-0NOeUQrogl7AP_nDEf2B");
                    return header;
                }
            };
            mRequestQue.add(request);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

public void readData(){
        String id=FirebaseAuth.getInstance().getCurrentUser().getUid();
    FirebaseDatabase.getInstance().getReference().child("Users").child(id).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String name=snapshot.child("name").getValue().toString();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
 //               Log.d(Tag.DatabaseError.getMessage());
        }
    });
}

public void sendCurrentLocationToSecondActivity(){
    Intent intent = new Intent(MainActivity.this,var_get.class);
    intent.putExtra("boyLattitude", v1);
    intent.putExtra("boyLongitude", v2);
    startActivity(intent);
}
}