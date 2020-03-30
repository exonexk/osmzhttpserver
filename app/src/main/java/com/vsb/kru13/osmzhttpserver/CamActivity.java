package com.vsb.kru13.osmzhttpserver;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.TimerTask;

public class CamActivity extends Activity {

    private Camera snapCamera;
    private FrameLayout frameLayout;
    private CameraPreview cameraPreview;
    private Button takeSnapshot, startStream, stopStream;
    private boolean bTakeSnap = true;
    public static byte[] imageInBytes;
    public static boolean bStream = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);
        frameLayout = (FrameLayout)findViewById(R.id.preview);
        takeSnapshot = (Button)findViewById(R.id.takeSnapshot);
        startStream = (Button)findViewById(R.id.startStream);
        stopStream = (Button)findViewById(R.id.stopStream);

        if(checkCameraHardware(this)){
            Toast.makeText(this,"Camera's ready ", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this,"Camera's not ready", Toast.LENGTH_LONG).show();
        }

        snapCamera = Camera.open();
        cameraPreview = new CameraPreview(this, snapCamera, bTakeSnap);
        takeSnapshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bTakeSnap) {
                    snapCamera.takePicture(null, null, mPictureCallback);
                    bTakeSnap = false;
                }
            }
        });
        startStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               
                timerTask.run();
            }
        });
        frameLayout.addView(cameraPreview);
        stopStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bStream = false;
            }
        });
    }
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            cameraPreview.getbTakeSnap();
            snapCamera.setPreviewCallback(previewCallback);
        }
    };

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File picture = getOutputMediaFile();

              if(picture == null){
                bTakeSnap = true;
                return;
            }else{
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(picture);
                    fileOutputStream.write(data);
                    fileOutputStream.close();
                    snapCamera.startPreview();
                    bTakeSnap = true;
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    };

    public static byte[] convertToImg(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();

        YuvImage image = new YuvImage(data, parameters.getPreviewFormat(), size.width, size.height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, out);
        byte[] imageBytes = out.toByteArray();
        return imageBytes;
    }

    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            imageInBytes = convertToImg(data, camera);
            try {
                snapCamera.startPreview();
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bTakeSnap = true;
        }
    };

    private File getOutputMediaFile() {
        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)){
            return null;
        }else{
            File folderSnapshot = new File(Environment.getExternalStorageDirectory() + File.separator + "Snapshot");
            if(!folderSnapshot.exists()){
                folderSnapshot.mkdirs();
            }
            File outputFile = new File(folderSnapshot, "Snapshot.jpg");
            return outputFile;
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }
}