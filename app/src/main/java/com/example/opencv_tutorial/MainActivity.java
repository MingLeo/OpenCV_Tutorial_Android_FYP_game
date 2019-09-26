package com.example.opencv_tutorial;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;   //define as global var outside of methods, so that var can be shared across all methods within this class!

    Mat mat1,mat2,mat3;   //The var Use to store/hold values of an/our image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Entered Oncreate","ContentView Set");
        cameraBridgeViewBase = (JavaCameraView)findViewById(R.id.myCameraView);   // Step 1, initializing the view (Java Camera View of OpenCv)
        //Log.d("JavaCameraView","get cameraId");
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        //Log.d("Visibility","set to true");
        cameraBridgeViewBase.setCvCameraViewListener(this);    //this reference


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

        mat1=inputFrame.rgba();

        return mat1;
    }

    @Override
    public void onCameraViewStopped() {
        mat1.release();
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
    protected void onResume() {
        super.onResume();
        Log.d("Entered OnResume","");
        if (!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"There is a problem with OpenCv",Toast.LENGTH_SHORT).show();  //if unable to conect to OpenCv upon resuming app, then thrwo this error
        }else{
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);   //else, connect invoke callback to connect back to CameraBridgeView.
            Log.d("Call BaseLoaderCallback","");
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
