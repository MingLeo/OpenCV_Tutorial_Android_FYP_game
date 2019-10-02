package com.example.opencv_tutorial;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

import java.util.List;

public class customSurfaceView extends JavaCameraView {

    private static final String TAG = "OpenCustomSufaceView";

    public customSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPreviewFPS(double min, double max){
        Camera.Parameters p = mCamera.getParameters();
        p.setPreviewFpsRange((int)(min*1000), (int)(max*1000));
        mCamera.setParameters(p);
    }


    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }

    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }

    public String getEffect() {
        return mCamera.getParameters().getColorEffect();
    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }

    public Camera.Parameters getParameters(){
        Camera.Parameters params = mCamera.getParameters();
        return params;
    }

    public void setParameters(Camera.Parameters params){
        mCamera.setParameters(params);
    }

    public List<Camera.Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Camera.Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public Camera.Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }


}
