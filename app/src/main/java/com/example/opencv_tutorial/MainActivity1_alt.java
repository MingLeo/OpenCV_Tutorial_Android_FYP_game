package com.example.opencv_tutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity1_alt extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;   //define as global var outside of methods, so that var can be shared across all methods within this class!

    Mat mat1,mat2,mat3;   //The var Use to store/hold values of an/our image
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("Entered Oncreate","ContentView Set");

        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
        else {
            // permission has been already granted, you can use camera straight away
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // user accepted your request, you can use camera now from here
                Toast.makeText(getApplicationContext(), "Application will not run if permission not granted for camera services", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.d("Entered OnResume","");

        if (!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"There is a problem with OpenCv",Toast.LENGTH_SHORT).show();  //if unable to conect to OpenCv upon resuming app, then thrwo this error
        }else{
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);   //else, connect invoke callback to connect back to CameraBridgeView.

            Log.d("Calling BaseLoaderCallback","");

        }
    }


    public void startCamera(){
        cameraBridgeViewBase = (JavaCameraView)findViewById(R.id.myCameraView);   // Step 1, initializing the view (Java Camera View of OpenCv)
        //Log.d("JavaCameraView","get cameraId");
        cameraBridgeViewBase.setMaxFrameSize(720,1080);    //this is to set screen resolution to take up maximum screen width and height.

        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        //Log.d("Visibility","set to true");
        cameraBridgeViewBase.setCvCameraViewListener(this);    //sets Frame Format to RGBA. 4 channels.  since we set our activity as the listener object to be referenced to, we put/use the "this' reference.


        /*
        if(OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "OpenCv loaded Successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),"Could not load OpenCv",Toast.LENGTH_SHORT).show();
        }
        */


        baseLoaderCallback = new BaseLoaderCallback(this) {     //"this" reference refers to the current class you are in. The view constructor requires you to provide a context and the only nearest context you have is this which is the current class.
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch(status)
                {
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        Log.d("View Enabled","");
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };

    }





    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {    //Frame will be coming from the camera as an input frame format.

        mat1=inputFrame.rgba();    //mat1 receives the input from incoming Frame

        //rotate the frame 90 degree. WHY??  Cos apparently the cameraview is rotated 90 degrees off!

        Core.transpose(mat1,mat2); //transpose mat1(src) to mat2(dst), sorta like a Clone!
        Log.d("core.transpose done","");
        Imgproc.resize(mat2,mat3,mat1.size(),0,0,0);    // params:(Mat src, Mat dst, Size dsize, fx, fy, interpolation)   Extract the dimensions of the new Screen Orientation, obtain the new orientation's surface width & height.  Try to resize to fit to screen.
        Log.d("resize done","");
        Core.flip(mat3,mat1,1);   // flipcode=0(flip ard x-axis)/ flipcode=1(flip ard y-axis) .   Params:(Mat src, Mat dst, int flipCode)     Map the new Screen Orientaiton's dimensions back to Mat1 which is the destination we wanna map it to.  We wanna do it INPLACE!  INPLACE "SWAP"/Change/alter Screen Orientation!
        Log.d("core.flip done","");
        return mat1;  //return as a rgba image Frame

//    //Online solution - opencv rotate input frame rgba object -  https://answers.opencv.org/question/7313/rotating-android-camera-to-portrait/
//        public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
//            mRgba = inputFrame.rgba();
//            Mat mRgbaT = mRgba.t();
//            Core.flip(mRgba.t(), mRgbaT, 1);
//            Imgproc.resize(mRgbaT, mRgbaT, mRgba.size());
//            return mRgbaT; }
    }

    @Override
    public void onCameraViewStopped() {
        mat1.release();   ///release t to prevent memory leaks, cos openCv lib are all native libs which are C codes, so uses malloc(), no garbage collection to handle object destruction gracefully.
        mat2.release();
        mat3.release();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {   //this method will be called bef the onCameraFrame() method above
        mat1 = new Mat(width, height, CvType.CV_8UC4);    //4 channels(rbga) of 8bits(0-255) Unsigned integers.   Alpha channel measures/indicates the degree of transparency/opacity of a colour, sorta like "grayscale" component/feature.
        mat2 = new Mat(width, height, CvType.CV_8UC4);
        mat3 = new Mat(width, height, CvType.CV_8UC4);
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();   //This method is provided for clients, so they can disable camera connection and stop the delivery of frames even though the surface view itself is not destroyed and still stays on the screen
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();   //This method is provided for clients, so they can disable camera connection and stop the delivery of frames even though the surface view itself is not destroyed and still stays on the screen
        }
    }

}
