package com.example.opencv_tutorial;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.InputStreamReader;


public class hsv_color_2_blue extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    //JavaCameraView javaCameraView;
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;

    private static final int MY_CAMERA_REQUEST_CODE = 100;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hsv_colour);   //setting the layout view in accordance to the specifications of one of the layout resource file under res/layout

        OpenCVLoader.initDebug();      //Performing a Debug check solely just for initialization purpose.

        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
        else {
            cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.hsvCameraView);
//            cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
            cameraBridgeViewBase.setCvCameraViewListener(this);
            cameraBridgeViewBase.setCameraIndex(1);     // 0 / 1  front,back cameras.
            cameraBridgeViewBase.enableView();    //super impt, w/o this it would not return

//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN);      //set Camera screen to Max height, width, FULL SCREEN!

//            cameraBridgeViewBase.setMaxFrameSize(640,480);
            cameraBridgeViewBase.setMaxFrameSize(1080,1980);   //the resolution dimension of my phone's screen display !


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
//            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);   //else, connect invoke callback to connect back to CameraBridgeView.

            Log.d("Calling BaseLoaderCallback","");

        }
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



    @Override
    public void onCameraViewStopped() {

        InputFrame.release();
        matFinal.release();

    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        InputFrame = new Mat (width, height, CvType.CV_16UC4);
        mat1 = new Mat (width, height, CvType.CV_8UC4);
        mat2 = new Mat (width, height, CvType.CV_8UC4);

        matFinal = new Mat(width, height, CvType.CV_8UC4);

        maskForBlue = new Mat(width, height, CvType.CV_16UC4);
        rgbMasked_leftBlue = new Mat(width, height, CvType.CV_16UC4);
    }

    Mat maskForBlue, rgbMasked_leftBlue;
    Mat InputFrame;
    Mat matFinal;
    Mat mat1, mat2;

    Scalar scalarLow, scalarHigh;


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        InputFrame = inputFrame.rgba();

        Core.transpose(InputFrame,mat1); //transpose mat1(src) to mat2(dst), sorta like a Clone!
        Imgproc.resize(mat1,mat2,InputFrame.size(),0,0,0);    // params:(Mat src, Mat dst, Size dsize, fx, fy, interpolation)   Extract the dimensions of the new Screen Orientation, obtain the new orientation's surface width & height.  Try to resize to fit to screen.
        Core.flip(mat2,InputFrame,0);   // mat3 now get updated, no longer is the Origi inputFrame.rgba BUT RATHER the transposed, resized, flipped version of inputFrame.rgba().

//        return InputFrame;  //this line here alrdy the freeze frame prob start to show alrdy!

        int rowWidth = InputFrame.rows();
        int colWidth = InputFrame.cols();

        //https://medium.com/@ckyrkou/color-thresholding-in-opencv-91049607b06d - manage to extract out the color Blue
        //https://stackoverflow.com/questions/26218280/thresholding-rgb-image-in-opencv

        //Try to convert to GRAYSCALE color scheme instead following this article https://medium.com/@ckyrkou/color-thresholding-in-opencv-91049607b06d

        Imgproc.cvtColor(InputFrame,InputFrame,Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(InputFrame,InputFrame,Imgproc.COLOR_RGB2HSV);

//        Scalar Lower_Blue = new Scalar(0,0,100);     //rgb color scale
//        Scalar Upper_Blue = new Scalar(100,100,225);    ////rgb color scale

        Scalar Lower_Blue = new Scalar(110,80,0);    //HSV color scale
        Scalar Upper_Blue = new Scalar(121,255,140);    //HSV color scale

        //Imgproc.cvtColor(InputFrame,maskForBlue,Imgproc.COLOR_RGBA2GRAY);
        Core.inRange(InputFrame,Lower_Blue, Upper_Blue,maskForBlue);
        //Imgproc.cvtColor(maskForBlue,rgbMasked_leftBlue,Imgproc.COLOR_GRAY2RGBA);



//        Core.bitwise_and(InputFrame, InputFrame, rgbMasked_leftBlue ,maskForBlue);

//        maskForBlue.release();
        return maskForBlue;
//        return rgbMasked_leftBlue;


    //detect the Orange first
    //findContour
    //findhierarchy?
    //circle the orange
    //multiple detect if nd detect more than 1 object? - Technically i shld only nd to track 1, the circle on the screen not counted.
//        //convert to HSV color scheme
//        Mat hsvFrame = new Mat(rowWidth, colWidth, CvType.CV_8UC3);
//        Imgproc.cvtColor(InputFrame,hsvFrame,Imgproc.COLOR_RGB2HSV,3);
//
//        //Mask the image for Orange colors
//        Mat OrangeColorMask = new Mat(hsvFrame.rows(),hsvFrame.cols(),CvType.CV_8UC3);
////        Core.inRange(hsvFrame,new Scalar(15,100,140),new Scalar(23,200,255), OrangeColorMask);
//        Core.inRange(hsvFrame,new Scalar(10,40,160),new Scalar(23,100,255), OrangeColorMask);
////        Core.inRange(hsvFrame,new Scalar(15,20,140),new Scalar(100,100,255),OrangeColorMask);
//
//        return OrangeColorMask;



        //findCOntour of the circle

//// 2. how to create the Mask properly! - https://www.programcreek.com/java-api-examples/?class=org.opencv.core.Core&method=bitwise_and  CAN'T THANK THIS LINK ENUFF, HELP ME THRU MOTHER FUCKING HELL!
//
//        //1. where the hell does this line clone concept come in? - find in one of project!
//        Mat matMaskClone = InputFrame.clone();  //Cloned Mask to work on
//
//        Mat hsvFrame = new Mat(InputFrame.rows(), InputFrame.cols(), CvType.CV_8U, new Scalar(3));
//
//        Imgproc.cvtColor(InputFrame, hsvFrame, Imgproc.COLOR_RGB2HSV, 3);
//
//
//        //https://docs.opencv.org/3.2.0/df/d9d/tutorial_py_colorspaces.html - outline the steps required to perform HSV Color space conversion
//        Imgproc.cvtColor(matMaskClone,mat1,Imgproc.COLOR_RGB2HSV, 4);    //alrdy converted into HSV Color scale.    //H is the color space, S is the saturation/amt of gray, & V is the brightness value.
////        scalarLow = new Scalar(45,20,10);    //Lowerbound HSV value
////        scalarHigh = new Scalar(75,100,255);    //Upperbound HSV value
//
////no.1 Analyze this! @@%% https://stackoverflow.com/questions/4063965/how-can-i-convert-an-rgb-image-to-grayscale-but-keep-one-color  -  GLIMMER of HOPE!   :O :D  EXACT SAME SITUATION AS ME!!   Keep 1 Color + performed think code performed COlor Segmentation.
////        Scalar Low_Orange = new Scalar(15,20,140);    //Lowerbound HSV value  brightness is the glossiness of the surface, amt of light required to shine on the object in order to be detected.
////        Scalar High_Orange = new Scalar(100,100,255);    //Upperbound HSV value
//
////        Core.inRange(mat1,scalarLow,scalarHigh,mat2);     // Params: inRange(Mat src, Scalar lowerb, Scalar upperb, Mat dst).    So mat1 is the src input  mat2 is the dest output.    scalarLow as the lowerbound value   scalarHigh as the upperbound value    ,  dst(I)= [ lowerb(I)0 ≤src(I)0 ≤upperb(I)0 ]    So dst = src confined to within/clipped to its lower & upper bound limits.   https://docs.opencv.org/3.4/d2/de8/group__core__array.html#ga48af0ab51e36436c5d04340e036ce981
////        Core.inRange(mat1,Low_Orange,High_Orange,mat2);     // Params: inRange(Mat src, Scalar lowerb, Scalar upperb, Mat dst).    So mat1 is the src input  mat2 is the dest output.    scalarLow as the lowerbound value   scalarHigh as the upperbound value    ,  dst(I)= [ lowerb(I)0 ≤src(I)0 ≤upperb(I)0 ]    So dst = src confined to within/clipped to its lower & upper bound limits.   https://docs.opencv.org/3.4/d2/de8/group__core__array.html#ga48af0ab51e36436c5d04340e036ce981
////        Core.inRange(mat1,new Scalar(15,20,140),new Scalar(100,100,255),mat2);     // Params: inRange(Mat src, Scalar lowerb, Scalar upperb, Mat dst).    So mat1 is the src input  mat2 is the dest output.    scalarLow as the lowerbound value   scalarHigh as the upperbound value    ,  dst(I)= [ lowerb(I)0 ≤src(I)0 ≤upperb(I)0 ]    So dst = src confined to within/clipped to its lower & upper bound limits.   https://docs.opencv.org/3.4/d2/de8/group__core__array.html#ga48af0ab51e36436c5d04340e036ce981
//        Core.inRange(mat1,new Scalar(15,20,140),new Scalar(23,100,255),mat2);     // HSV Mask   https://stackoverflow.com/questions/48528754/what-are-recommended-color-spaces-for-detecting-orange-color-in-open-cv
//        //at such a low Saturation level, even my hand which is only slightly orange will be detected!
//        //Try this ORANGE BOUNDARY RANGE - https://stackoverflow.com/questions/10948589/choosing-the-correct-upper-and-lower-hsv-boundaries-for-color-detection-withcv
//
//        //Hue - color , S - Diff shades of the color (0-255 all shades), V - brightness of image (0-255detect @ all brightness level of the color)
//
////        Core.bitwise_not(mat2,mat2);   //opp of the mask, convert from from black scale image to white scale image.
//        // Param: bitwise_and(Mat src1, Mat src2, Mat dst, Mat mask)
////=======SOMETHING WRONG W THIS , Think is cos i did not clone for the mask! ===================
//        Core.bitwise_and(, mat3, matFinal, mat2);    //returns the object in focus 'matFinal', which in this case will be the orange,  while the rest of the other region of the img
//
//        //Try bitwise_and || or || not || xor etc......
//
//////=============== NEW DIRECTION to take ==================
////1. ok, can work, now to apply a simple blob detection algorithm, & retrieve/find the largest blob!
////
////2. hmm what we doing here is a IN_FOCUS process/procedure, but i want to set to OUT_of_FOCUS instead!
////meaning to set those regions out of the allowable range to be ignored.
////Use color segmentation!
////// ============================================================
//
//
////        Imgproc.cvtColor(mat2,matFinal,Imgproc.COLOR_HSV2RGB,4);    //Invalid number of channels in input image: > 'VScn::contains(scn)' > where > 'scn' is 1
////        Imgproc.cvtColor(matFinal,matFinal,Imgproc.COLOR_RGB2RGBA);
//
////        screenOrientationCorrection(inputFrame.rgba());
//
////        return mat1;   //If we nvr do any preprocessing, just simply return aft doing cvtColor conversion from bgr2hsv line 178 //will simply display a HSV img, nvr restrain to any particular color range.
////        return matFinal;
//
//
//        return matFinal;    //constantly returning alot of frames, frames do not auto clear out! - cause screen to become obscure over time..
//
//        //but why does my HSV image looks grayscale?  ApaRENTLY THE cORE.INrANGE part returns a Mask, so is BW, Nd to perform Bitwise operations to project post-compute masked objects
//        //                                             Soln: 1 ) https://stackoverflow.com/questions/48528754/what-are-recommended-color-spaces-for-detecting-orange-color-in-open-cv
//        //                                                   2)https://stackoverflow.com/questions/47483951/how-to-define-a-threshold-value-to-detect-only-green-colour-objects-in-an-image/47483966#47483966
//        //                                                     3)https://stackoverflow.com/questions/48109650/how-to-detect-two-different-colors-using-cv2-inrange-in-python-opencv/48117624?noredirect=1#comment83996826_48117624
    }



//    //return value of this Method is not used
//    public Mat screenOrientationCorrection(Mat inputFrame){   //this will be pass in as inputFrame.rgba which is a Mat type since [final Mat frame = inputFrame.rgba()];
//        //mat3= inputFrame;    //mat3 receives the input from incoming Frame
//        //We nd to rotate the frame 90 degree. WHY??  Cos apparently the cameraview is rotated 90 degrees off!
//        Core.transpose(inputFrame,mat4); //transpose mat1(src) to mat2(dst), sorta like a Clone!
//        Log.d("core.transpose done","");
//        Imgproc.resize(mat4,mat5,inputFrame.size(),0,0,0);    // params:(Mat src, Mat dst, Size dsize, fx, fy, interpolation)   Extract the dimensions of the new Screen Orientation, obtain the new orientation's surface width & height.  Try to resize to fit to screen.
//        Log.d("resize done","");
//        Core.flip(mat5,inputFrame,1);   // flipcode=0(flip ard x-axis)/ flipcode=1(flip ard y-axis) .   Params:(Mat src, Mat dst, int flipCode)     Map the new Screen Orientaiton's dimensions back to Mat1 which is the destination we wanna map it to.  We wanna do it INPLACE!  INPLACE "SWAP"/Change/alter Screen Orientation!
//        Log.d("core.flip done","");
//        return mat3;
//    }

    //public Mat
}
