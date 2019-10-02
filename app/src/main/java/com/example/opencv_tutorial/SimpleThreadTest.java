package com.example.opencv_tutorial;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.animation.CycleInterpolator;
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
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;


//plot circle/squares @ the 4 edges of the screen!

public class SimpleThreadTest extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    //JavaCameraView javaCameraView;
//    CameraBridgeViewBase cameraBridgeViewBase;
    customSurfaceView cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;

    private static final int MY_CAMERA_REQUEST_CODE = 100;

    Timer t1 = new Timer();

    long secsLeft;
    boolean flag1 = false;
    boolean flag2 = false;
    boolean flag3 = false;
    boolean flag4 = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hsv_colour);   //setting the layout view in accordance to the specifications of one of the layout resource file under res/layout

        OpenCVLoader.initDebug();      //Performing a Debug check solely just for initialization purpose.

        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        } else {
//            cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.hsvCameraView);
            cameraBridgeViewBase = (customSurfaceView) findViewById(R.id.hsvCameraView);
//            CustomizableCameraView c1 = new CustomizableCameraView();
//            c1.setPreviewFPS(10000,18000);
//            cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
            cameraBridgeViewBase.setCvCameraViewListener(this);
            cameraBridgeViewBase.setCameraIndex(1);     // 0 / 1  front,back cameras.
            cameraBridgeViewBase.enableView();    //super impt, w/o this it would not return


            baseLoaderCallback = new BaseLoaderCallback(this) {     //"this" reference refers to the current class you are in. The view constructor requires you to provide a context and the only nearest context you have is this which is the current class.
                @Override
                public void onManagerConnected(int status) {
                    super.onManagerConnected(status);
                    switch (status) {
                        case BaseLoaderCallback.SUCCESS:
                            cameraBridgeViewBase.enableView();
                            Log.d("View Enabled", "");
                            break;
                        default:
                            super.onManagerConnected(status);
                            break;
                    }
                }
            };

        }


        //        long secsLeft;
        new CountDownTimer(16000, 4000) {


            public void onTick(long millisUntilFinished) {
                secsLeft = (millisUntilFinished / 1000);

                Log.d("seconds remaining: ","" + secsLeft);  // =D works

//                for (int i=0; i<secsLeft; i++){
//                    int mynumber = new Random().nextInt(100);
//                    e3.setText(String.valueOf(mynumber));
//                }

                if (secsLeft == 16 || secsLeft==15){    //if this works, the next step is to test on OnCameraFrame
                    //set flag 1, DO SOMETHING/ RUN SOME CODE.
                    Log.d("flag 1","yay");   //:o finally showing :)
                    flag1=true;
                    flag2=false;
                    flag3=false;
                    flag4=false;

                    Log.d("bool ","flag1: "+flag1);
                    Log.d("bool ","flag2: "+flag2);
                    Log.d("bool ","flag3: "+flag3);
                    Log.d("bool ","flag4: "+flag4);
                }
                if (secsLeft == 11){    //if this works, the next step is to test on OnCameraFrame
                    //set flag 2, DO SOMETHING/ RUN SOME CODE.
                    Log.d("flag 2","HOHO");   //:o finally showing :)
                    flag1=false;
                    flag2=true;
                    flag3=false;
                    flag4=false;

                    Log.d("bool ","flag1: "+flag1);
                    Log.d("bool ","flag2: "+flag2);
                    Log.d("bool ","flag3: "+flag3);
                    Log.d("bool ","flag4: "+flag4);
                }
                if (secsLeft == 7){    //if this works, the next step is to test FLAG on OnCameraFrame
                    //set flag 3, DO SOMETHING.
                    Log.d("flag 3","heehee");   //:o NABEI, finally Works/showing KNN!
                    flag1=false;
                    flag2=false;
                    flag3=true;
                    flag4=false;

                    Log.d("bool ","flag1: "+flag1);
                    Log.d("bool ","flag2: "+flag2);
                    Log.d("bool ","flag3: "+flag3);
                    Log.d("bool ","flag4: "+flag4);
                }
                if (secsLeft == 3){    //if this works, the next step is to test on OnCameraFrame
                    //set flag 4, DO SOMETHING ELSE.
                    Log.d("flag 4","gaga");   //:o finally showing :)
                    flag1=false;
                    flag2=false;
                    flag3=false;
                    flag4=true;

                    Log.d("bool ","flag1: "+flag1);
                    Log.d("bool ","flag2: "+flag2);
                    Log.d("bool ","flag3: "+flag3);
                    Log.d("bool ","flag4: "+flag4);
                }

//   =D =D =D FLAG FUCKING WORKS  :)   Now to test it in conjunction w a simple onCameraFrame().
//NEXT STEP, TO FUCKING TEST IT ON CODE, See if flags triggered in onCREATE CAN TRIGGER ONCAMERAFRAME?



////            editText.setText(mynumber);    //android.content.res.Resources$NotFoundException: String resource ID #0x12
//            editText.setText(String.valueOf(mynumber));    // :D =D WORKS!
            }

            public void onFinish() {
                Log.d("Elapsed Time since bootup bef sleep: ","" + SystemClock.elapsedRealtime());
                SystemClock.sleep(50);
                Log.d("SystemClock.Sleep","5s");   // =D  Rly Worked, added a delay of 5s, puase the Whole Damn Fucking thing.  Rly slept for 5s, to "Recharge", Dun Fk up!
                                                           //NOT IDEAL TO PUT SLEEP ON MAIN THREAD, COS IT STOPS THE ENTIRE MAIN THREAD, FRAME FREEZES!  Hang Type of feel.
                Log.d("Elapsed Time since bootup aft sleep: ","" + SystemClock.elapsedRealtime());

//                editText.setText("done!");  //can be as simple and elegant as a text/even a Logcat output :D
//                Log.d("done!","");  // ??? :( Log.d does not work/output on the android Terminal why?
//                what if i leave it as empty?? See what happens, whether got throw any Error? - nOPE ALSO OK, cAN!

                start();    // :D :D :D YASSS!  IT FREAKING WORKED!  Loop on forever . HAHAHAHHAA! YAY! JOY TO THE WORLD!
            }
        }.start();


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
        Log.d("Entered OnResume", "");

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "There is a problem with OpenCv", Toast.LENGTH_SHORT).show();  //if unable to conect to OpenCv upon resuming app, then thrwo this error
        } else {
//            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);   //else, connect invoke callback to connect back to CameraBridgeView.

            Log.d("Calling BaseLoaderCallback", "");

        }


    }


    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();   //This method is provided for clients, so they can disable camera connection and stop the delivery of frames even though the surface view itself is not destroyed and still stays on the screen
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
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

//================ Code Logic for controlling Camera Frame Rate via custom method  setPreviewFPS() in our CustomSurfaceView class, But actl since now we do away w Thread, might one to speed up the Frame Rate so as to make it less Laggy! =============================

//        cameraBridgeViewBase.setPreviewFPS(10,15);    //  :( Not working leh.


        Camera.Parameters l_params = cameraBridgeViewBase.getParameters();
        List<int[]> frameRates = l_params.getSupportedPreviewFpsRange();    //@@https://stackoverflow.com/questions/22456817/how-to-limit-preview-fps-range-in-android-for-camera -  check what FPS range supported by your device
        Log.d("print","framerates" + frameRates);    //prints out gibberish    [I@427f4d, [I@1a7da02, [I@8709f13, [I@41db550].   Ideally shld return something like (10000,10000),(15000,15000),(15000,30000),(30000,30000) instead.
//            for (int i=0; i<frameRates.size();i++){
//                Log.d("printing","framerate(i)" + frameRates.get(i).toString());    //prints out gibberish, no idea WTF is it?    [I@427f4d, [I@1a7da02, [I@8709f13, [I@41db550]
////                Log.d("printing","framerate(i)" + Integer.valueOf(frameRates.get(i).toString()));   //nope does not work, app crashes
//            }

        int l_first = 0;    //index of the lowest possible frame rate of the frameRates array.
        int l_last = frameRates.size() - 1;
        int minFps = (frameRates.get(l_first))[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];   //set to the min frame of the lowest poss framte rate index
        int maxFps = (frameRates.get(l_first))[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];   //set to the max frame of the lowest poss framte rate index
                                                                        // E.g frameRate = [(10000,10000),(15000,15000),(15000,30000),(30000,30000)]
                                                                        // l_first = (10000,10000)
                                                                        // minFps = 10000
                                                                        // maxFps = 10000
        Log.d("minFps: ",""+minFps);   //output: 15000, 15 frames
        Log.d("maxFps: ",""+maxFps);   //output: 15000, 15 frames

        l_params.setPreviewFpsRange(minFps, maxFps);
        cameraBridgeViewBase.setParameters(l_params);   //:D it worked! Use the frameRates supported by my device!

//=========================================================================================================================================


        InputFrame = new Mat(width, height, CvType.CV_16UC4);
        mat1 = new Mat(width, height, CvType.CV_8UC4);
        mat2 = new Mat(width, height, CvType.CV_8UC4);

        matFinal = new Mat(width, height, CvType.CV_8UC4);

        maskForYellow = new Mat(width, height, CvType.CV_8UC4);
        rgbMasked_leftYellow = new Mat(width, height, CvType.CV_16UC4);
        yellowMaskMorphed = new Mat(width, height, CvType.CV_8UC4);
    }

    Mat maskForYellow, rgbMasked_leftYellow, yellowMaskMorphed;
    Mat InputFrame;
    Mat matFinal;
    Mat mat1, mat2;

    Scalar Lower_Yellow, Upper_Yellow;


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {    //see into method CvCameraViewFrame, 2

        InputFrame = inputFrame.rgba();        //Mat type Input

        Core.transpose(InputFrame, mat1); //transpose mat1(src) to mat2(dst), sorta like a Clone!
        Imgproc.resize(mat1, mat2, InputFrame.size(), 0, 0, 0);    // params:(Mat src, Mat dst, Size dsize, fx, fy, interpolation)   Extract the dimensions of the new Screen Orientation, obtain the new orientation's surface width & height.  Try to resize to fit to screen.
        Core.flip(mat2, InputFrame, -1);   // mat3 now get updated, no longer is the Origi inputFrame.rgba BUT RATHER the transposed, resized, flipped version of inputFrame.rgba().



//================ =D YASSS! ABOVE CODE Finally Fucking Working!  WORKED!  WORKED WORKED! :D :D :D  FUCKING FINALLY! ===================================


//=============== Plot Rectangle / Circle @ fixed positions, corner of the screen! ==========================

//        Scalar color = new Scalar(0,255,0);   //r,g,b, so only show green box

        //https://stackoverflow.com/questions/40120433/draw-rectangle-in-opencv?rq=1 -- how to use Rect = new Rect()
        //https://answers.opencv.org/question/122532/how-to-floodfill-an-image-with-java-api/ -- how to use floodfill using Rect.
        //https://qiita.com/yeb8jo/items/2ab97dc69b375b501fba - floodfill implementation in Android
        //@@--> THIS THE ONE :D -  https://github.com/zylo117/SpotSpotter/blob/master/src/pers/zylo117/spotspotter/patternrecognition/ROI_Irregular.java - floodfill implementation in Android


        Imgproc.cvtColor(InputFrame, InputFrame, Imgproc.COLOR_RGBA2RGB);
        objectBox(InputFrame);


        return InputFrame;  //Works =D =D =D !  YASSSS!!            //Mat Type output.   So OnCameraFrame we only work on Mat!

    }


    public void objectBox(Mat inputFrame) {

        final Mat InputFrame = inputFrame;    //variable 'InputFrame' is accessed from within inner class, needs to be declared final.
        //timer t1 has been created as a global variable at the top!  See line 64.


        final Handler handler = new Handler(Looper.getMainLooper());    //java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
                                                                        //Soln: Call Looper.getMainLooper()  @@https://stackoverflow.com/questions/3875184/cant-create-handler-inside-thread-that-has-not-called-looper-prepare


                             //YUPPA DEEDEE DOODLE FK EM THREADS!  IT WAS THE THEAD THAT CAUSE THE PROB!
        if(flag1 == true){   // FLATHERLY FATHERLY FUCKING WORKS!  =D

            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);
            Rect r1 = new Rect(0,0,300,300);
            Imgproc.rectangle(InputFrame, r1.br(), r1.tl(), new Scalar(0, 255, 0), 4);    //top left corner
            Imgproc.floodFill(InputFrame, maskFloodFill,new Point((r1.br().x +r1.tl().x) / 2, (r1.br().y +r1.tl().y) / 2), new Scalar(0,255,0), new Rect() , new Scalar(20,20,20),new Scalar(20,20,20),4);   //mm, 20 seems better, cos 120 sometimes will glitch whole screen becomes green, instead of just the 4 corners only.
            Log.d("running top LEFT corner object", "");

        }


        if(flag2 == true){

            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);
            Rect r2 = new Rect(772,0,300,300);
            Imgproc.rectangle(InputFrame, r2.br(), r2.tl(), new Scalar(0, 255, 0), 4);    //top right corner
            Imgproc.floodFill(InputFrame, maskFloodFill,new Point((r2.br().x +r2.tl().x) / 2, (r2.br().y +r2.tl().y) / 2), new Scalar(0,255,0), new Rect() , new Scalar(20,20,20),new Scalar(20,20,20),4);
            Log.d("running top RIGHT corner object", "");

        }


        if(flag3 == true){

            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);
            Rect r3 = new Rect(0,772,300,300);
            Imgproc.rectangle(InputFrame, r3.br(), r3.tl(), new Scalar(0, 255, 0), 4);    //top right corner
            Imgproc.floodFill(InputFrame, maskFloodFill,new Point((r3.br().x +r3.tl().x) / 2, (r3.br().y +r3.tl().y) / 2), new Scalar(0,255,0), new Rect() , new Scalar(20,20,20),new Scalar(20,20,20),4);
            Log.d("running BOTTOM LEFT corner object", "");

        }



        if(flag4 == true){

            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);
            Rect r4 = new Rect(772,772,300,300);
            Imgproc.rectangle(InputFrame, r4.br(), r4.tl(), new Scalar(0, 255, 0), 4);    //top right corner
            Imgproc.floodFill(InputFrame, maskFloodFill,new Point((r4.br().x +r4.tl().x) / 2, (r4.br().y +r4.tl().y) / 2), new Scalar(0,255,0), new Rect() , new Scalar(20,20,20),new Scalar(20,20,20),4);
            Log.d("running BOTTOM RIGHT corner object", "");

        }


    }






//        if(flag1 == true){   // FLATHERLY FATHERLY FUCKING WORKS!  =D
//
//
//            handler.postDelayed(new Runnable() {
//
//                    public void run(){    //observe 2 run() here inside, that's why very slow. 1st run()
//                        t1.schedule(new TimerTask() {
//                            @Override
//                            public void run() {  //2nd run(), not efficient, cut it down to 1 run(), either handler / timer scheduler.
////                                final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);
//                                Rect r1 = new Rect(0,0,300,300);
//                                Imgproc.rectangle(InputFrame, r1.br(), r1.tl(), new Scalar(0, 255, 0), 4);    //top left corner
////                                Imgproc.floodFill(InputFrame, maskFloodFill,new Point((r1.br().x +r1.tl().x) / 2, (r1.br().y +r1.tl().y) / 2), new Scalar(0,255,0), new Rect() , new Scalar(20,20,20),new Scalar(20,20,20),4);   //mm, 20 seems better, cos 120 sometimes will glitch whole screen becomes green, instead of just the 4 corners only.
//                                Log.d("running top LEFT corner object", "");
//                            }
//                        }, 0);
//                    }
//                },0);
//
//        }
//
//
//        if(flag2 == true){
//
//
//            handler.postDelayed(new Runnable() {
//
//                public void run(){
//                    t1.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);
//                            Rect r2 = new Rect(772,0,300,300);
//                            Imgproc.rectangle(InputFrame, r2.br(), r2.tl(), new Scalar(0, 255, 0), 4);    //top right corner
//                            Imgproc.floodFill(InputFrame, maskFloodFill,new Point((r2.br().x +r2.tl().x) / 2, (r2.br().y +r2.tl().y) / 2), new Scalar(0,255,0), new Rect() , new Scalar(20,20,20),new Scalar(20,20,20),4);
//                            Log.d("running top RIGHT corner object", "");
//                        }
//                    }, 0);  //since i use flag to control, i dun nd delay alrdy, everything run accroding to flag
//                                  //only the thing that controls the flag will have delay, so every 4 seconds, see countDownTimer in OnCreate() @ Top!
//                }
//            },0);
//
//
//        }
//
//
//        if(flag3 == true){
//
//
//            handler.postDelayed(new Runnable() {
//
//                public void run(){
//                    t1.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
////                            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);
//                            Rect r3 = new Rect(0,772,300,300);
//                            Imgproc.rectangle(InputFrame, r3.br(), r3.tl(), new Scalar(0, 255, 0), 4);    //top right corner
////                            Imgproc.floodFill(InputFrame, maskFloodFill,new Point((r3.br().x +r3.tl().x) / 2, (r3.br().y +r3.tl().y) / 2), new Scalar(0,255,0), new Rect() , new Scalar(20,20,20),new Scalar(20,20,20),4);
//                            Log.d("running BOTTOM LEFT corner object", "");
//                        }
//                    }, 0);
//                }
//            },0);
//
//
//        }
//
//
//
//        if(flag4 == true){
//
//
//            handler.postDelayed(new Runnable() {
//
//                public void run(){
//                    t1.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
////                            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);
//                            Rect r4 = new Rect(772,772,300,300);
//                            Imgproc.rectangle(InputFrame, r4.br(), r4.tl(), new Scalar(0, 255, 0), 4);    //top right corner
////                            Imgproc.floodFill(InputFrame, maskFloodFill,new Point((r4.br().x +r4.tl().x) / 2, (r4.br().y +r4.tl().y) / 2), new Scalar(0,255,0), new Rect() , new Scalar(20,20,20),new Scalar(20,20,20),4);
//                            Log.d("running BOTTOM RIGHT corner object", "");
//                        }
//                    }, 0);
//                }
//            },0);
//
//        }
//
//
//    }





//    public class CustomizableCameraView extends JavaCameraView {
//
//        public CustomizableCameraView(Context context, AttributeSet attrs) {
//            super(context, attrs);
//        }
//
//        public void setPreviewFPS(double min, double max){
//            Camera.Parameters params = mCamera.getParameters();
//            params.setPreviewFpsRange((int)(min*1000), (int)(max*1000));
//            mCamera.setParameters(params);
//        }
//    }

//    public class CustomizeCamera extends Camera {    //no default constructor available in 'android.hardware.Camera', so super won't work. cos Super references the constructor of the Parent class, assuming that it has one. else it will fail.
//
//        public CustomizableCameraView(Context context, AttributeSet attrs) {
//            super(context, attrs);
//        }
//
//        public void setPreviewSize(int width, int height) {
////            super(width, height);   //no constructor
//            Camera.Parameters params = mCamera.getParameters();
////            Camera.Parameters params;   //not initialized.
//            Camera.Parameters.setPreviewSize((int)frameSize.width, (int)frameSize.height);   //Non-static method 'setPreviewSize(int,int)' cannot be referenced from a static context.
//            params.setPreviewSize((int)frameSize.width, (int)frameSize.height);
//        }
//
//    }





}
