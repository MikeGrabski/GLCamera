package com.g.mike.glcamera.handlers;

import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.Image;
import android.os.AsyncTask;
import android.widget.Toast;

import com.g.mike.glcamera.ImageStore;
import com.g.mike.glcamera.iCamera;

import java.io.IOException;
import java.security.Policy;
import java.security.PublicKey;
import java.util.List;

/**
 * Created by Mike on 25.06.2016.
 */
public class CameraHandler implements iCamera {
    /**
     * An empty Camera for testing purpose.
     */
    private Camera camera;
    private int width;
    private int height;
    private Camera.Parameters params;
    private boolean cameraSet = false;
    private boolean cameraFront = false;
    private boolean flashEnabled = false;
    private byte[] imageData;

    public CameraHandler() {

    }
    public void openCamera(){
        camera.open();
    }
    public void swapCam() {
        releaseCamera();
        if (!cameraFront) {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            cameraFront = true;
            flashEnabled = false;
        } else {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            cameraFront = false;
        }

    }
    public void setupCamera(int height, int width, SurfaceTexture st){
        if (!cameraSet) {
            camera = Camera.open();
            cameraSet = true;
        }
        try {
            camera.setPreviewTexture(st);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        Camera.Parameters param = camera.getParameters();
        List<Camera.Size> psize = param.getSupportedPreviewSizes();
        if (psize.size() > 0) {
            int i;
            for (i = 0; i < psize.size(); i++) {
                if (psize.get(i).width < width || psize.get(i).height < height)
                    break;
            }
            if (i > 0)
                i--;
            param.setPreviewSize(psize.get(i).width, psize.get(i).height);

            this.width = psize.get(i).width;
            this.height = psize.get(i).height;
            params = param;
        }
    }
    public void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void kill() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
        }

        camera = null;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, final Camera camera) {
            startPreview();
            imageData = data;
        }
    };

    public void setParameters() {
            camera.setParameters(params);
    }

    public int getWidth() {
            return width;
        }

    public int getHeight() {
            return height;
        }

    public void startPreview() {
            camera.startPreview();
        }


    public void capture(int orientation) {
        if(flashEnabled){
            //params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        }
        else {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);}
        setParameters();
        camera.takePicture(null, null, mPicture);
        while (true){
            if(imageData == null)
                continue;
            else
                break;
        }
        ImageStore img = new ImageStore(imageData);
        img.store(orientation);
        imageData = null;
    }

    public void toggleFlash() {
        if(!cameraFront){
            flashEnabled=!flashEnabled;
        }
    }

    public boolean getCameraId() {
        return cameraFront;
    }
}
