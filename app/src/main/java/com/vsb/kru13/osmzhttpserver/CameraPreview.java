package com.vsb.kru13.osmzhttpserver;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Camera snapCamera;
    private boolean bTakeSnap;

    public CameraPreview(Context context, Camera camera, Boolean safe){

        super(context);
        snapCamera = camera;
        bTakeSnap = safe;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        Camera.Parameters parameters = snapCamera.getParameters();
        if(this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
            parameters.set("orientation", "portrait");
            snapCamera.setDisplayOrientation(90);
            parameters.setRotation(90);
        }else{
            parameters.set("orientation", "landscape");
            snapCamera.setDisplayOrientation(0);
            parameters.setRotation(0);
        }

        snapCamera.setParameters(parameters);
        try {
            snapCamera.setPreviewDisplay(holder);
            snapCamera.startPreview();
        } catch (IOException e) {
            Log.d("CameraPreview", "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        snapCamera.startPreview();
        bTakeSnap = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public boolean getbTakeSnap(){
        return bTakeSnap;
    }
}