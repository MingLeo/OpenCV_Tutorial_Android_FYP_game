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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;


//plot circle/squares @ the 4 edges of the screen!

public class IdentifyCornerOfInputScreen extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    //JavaCameraView javaCameraView;
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;

    private static final int MY_CAMERA_REQUEST_CODE = 100;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hsv_colour);   //setting the layout view in accordance to the specifications of one of the layout resource file under res/layout
//        setContentView(R.layout.diff_intervals);

        OpenCVLoader.initDebug();      //Performing a Debug check solely just for initialization purpose.

        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        } else {
            cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.hsvCameraView);
//            cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
            cameraBridgeViewBase.setCvCameraViewListener(this);
            cameraBridgeViewBase.setCameraIndex(1);     // 0 / 1  front,back cameras.
            cameraBridgeViewBase.enableView();    //super impt, w/o this it would not return

//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN);      //set Camera screen to Max height, width, FULL SCREEN!

            //Nvr Set cos alrdy set it to FullScreen under res/values/styles.xml & Manifest.xml files.
//            cameraBridgeViewBase.setMaxFrameSize(640,480);
//            cameraBridgeViewBase.setMaxFrameSize(1080,1980);   //the resolution dimension of my phone's screen display !
//            cameraBridgeViewBase.setMaxFrameSize(2000,1980);   //the resolution dimension of my phone's screen display !


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

//        return InputFrame;  //this line here alrdy the freeze frame prob start to show alrdy!

        int rowWidth = InputFrame.rows();
        int colWidth = InputFrame.cols();

        //https://medium.com/@ckyrkou/color-thresholding-in-opencv-91049607b06d - manage to extract out the color Blue
        //https://stackoverflow.com/questions/26218280/thresholding-rgb-image-in-opencv

        //Try to convert to GRAYSCALE color scheme instead following this article https://medium.com/@ckyrkou/color-thresholding-in-opencv-91049607b06d

        Imgproc.cvtColor(InputFrame, InputFrame, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(InputFrame, InputFrame, Imgproc.COLOR_RGB2HSV);


        Lower_Yellow = new Scalar(21, 150, 150);    //HSV color scale  H to adjust color, S to coontrol color variation, V is indicator of amt of light required to be shine on object to be seen.
//        Lower_Yellow = new Scalar(21,150,100);    //HSV color scale  H to adjust color, S to coontrol color variation, V is indicator of amt of light required to be shine on object to be seen.
//        Lower_Yellow = new Scalar(21,150,0);    //HSV color scale  H to adjust color, S to coontrol color variation, V is indicator of amt of light required to be shine on object to be seen.
        Upper_Yellow = new Scalar(31, 255, 360);    //HSV color scale

        //Imgproc.cvtColor(InputFrame,maskForBlue,Imgproc.COLOR_RGBA2GRAY);
        Core.inRange(InputFrame, Lower_Yellow, Upper_Yellow, maskForYellow);


//        return maskForYellow;


        //perform MOPRH ON MASK  fk it, SKIP IT Cos able to achieve reliable/stable Yellow color detection
        //@#https://www.programcreek.com/java-api-examples/?class=org.opencv.imgproc.Imgproc&method=getStructuringElement
        //https://www.programcreek.com/java-api-examples/?class=org.opencv.imgproc.Imgproc&method=morphologyEx
        //@#https://docs.opencv.org/3.4/d4/d76/tutorial_js_morphological_ops.html


//        final Size kernelSize = new Size(11, 11);   //Kernel Size placys a part in computation + also the end result, whether is 1/0.
        final Size kernelSize = new Size(5, 5);  //must be odd num size & greater than 1.
        final Point anchor = new Point(-1, -1);   //default (-1,-1) means that the anchor is at the center of the structuring element.
        final int iterations = 1;   //number of times dilation is applied.  https://docs.opencv.org/3.4/d4/d76/tutorial_js_morphological_ops.html
//        final int iterations = 2;   //Higher iterations, more dilation applied, slower cos nd compute more + output less desirable.

//        Mat kernel = new Mat(new Size(3, 3), CvType.CV_8UC4);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, kernelSize);

//dif morph operations - https://books.google.com.sg/books?id=LFtICgAAQBAJ&pg=PA55&lpg=PA55&dq=imgproc.getstructuringelement&source=bl&ots=48XIWI1YBS&sig=ACfU3U2f2MyXUndiBg3Y2SbWf5vJuJTQhA&hl=en&sa=X&ved=2ahUKEwixmI7mw9XkAhVHL48KHXWBCgIQ6AEwCXoECAkQAQ#v=onepage&q=imgproc.getstructuringelement&f=false
//        Imgproc.morphologyEx(maskForYellow, yellowMaskMorphed, Imgproc.MORPH_OPEN, kernel, anchor, iterations);    //Black regions becomes more pronounced, Erode away white regions.  Erode then dilate.
        Imgproc.morphologyEx(maskForYellow, yellowMaskMorphed, Imgproc.MORPH_CLOSE, kernel, anchor, iterations);   //dilate first to remove then erode.  White regions becomes more pronounced, erode away black regions

//        Imgproc.dilate(maskForYellow, yellowMaskMorphed, kernel, anchor, iterations);  //white region becomes more prominent .  dilate, vry useful in searching for connect components.
//        Imgproc.erode(maskForYellow, yellowMaskMorphed, kernel, anchor, iterations);  //black region becomes more prominent

////   Verdict: YUP, MORPH_CLOSE better option over MORPH_OPEN -> See https://docs.opencv.org/trunk/d9/d61/tutorial_py_morphological_ops.html


        //https://docs.opencv.org/3.4/da/d0c/tutorial_bounding_rects_circles.html
        //Convert that mask to Canny for edge detection., else too many points detected in the mask, cos inside region also filled/shaded

//        Apply Canny for edge detection
        Mat mIntermediateMat = new Mat();
//        Imgproc.GaussianBlur(yellowMaskMorphed,mIntermediateMat,new Size(9,9),2,2);
//        Imgproc.GaussianBlur(yellowMaskMorphed, mIntermediateMat, new Size(11, 11), 0, 0);
//        Imgproc.GaussianBlur(yellowMaskMorphed, mIntermediateMat, new Size(9, 9), 0, 0);   //better result than kernel size (3,3), maybe cos reference area wider, bigger, can decide better whether inrange / out of range.
//        Imgproc.GaussianBlur(yellowMaskMorphed,mIntermediateMat,new Size(5,5),0,0);
        Imgproc.GaussianBlur(yellowMaskMorphed,mIntermediateMat,new Size(3,3),0,0);    //kernel size (3,3) better accuracy cos scrutinizing over a smaller patch of area to decide whether that patch is in/out of range, so threshold/allowance smaller.
        //i.e Acceptable Value range 61-120 vs 5-9.  Any value from 61-120 would qualify, that gives it 60 possibilities, whilst 5-9 only has 5 possibilities
        //Therefore, 5/9 nd a higher degree of detection to qualify as compare to 61/120. Hence we say that 5/9 has a lower threshold/allowance than 61/120.

        Imgproc.Canny(mIntermediateMat, mIntermediateMat, 5, 120);   //try adjust threshold   //https://stackoverflow.com/questions/25125670/best-value-for-threshold-in-canny
        //https://stackoverflow.com/questions/21324950/how-to-select-the-best-set-of-parameters-in-canny-edge-detection-algorithm-imple
//        Imgproc.medianBlur(mIntermediateMat,mIntermediateMat,5);

//        return mIntermediateMat;


        //https://gist.github.com/six519/743e32c9879ffea299b0b175823adc88 - how to use IMGPROC.CONTOURAREA()
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        //O.O, my contours is alrdy on the thresholded YELLOW MASK ALRDY, so yellow objects, but not guarantee the Largest Object.

        Imgproc.findContours(yellowMaskMorphed, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));   //returns a list of Contours
//        Imgproc.findContours(mIntermediateMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));   //returns a list of Contours
        //http://opencvpython.blogspot.com/2013/01/contours-5-hierarchy.html
        //https://stackoverflow.com/questions/11782147/python-opencv-contour-tree-hierarchy
        //https://docs.opencv.org/3.4/d9/d8b/tutorial_py_contours_hierarchy.html

//        int contourSize = contours.size();
        Log.d("contour", "size = " + contours.size());   //returns 21, 91, 120, 104, 116, 58, ......  varying contour size detected & returned. Observe how contours can exceed 100.  :O WAOW!
//        byte[] arr = new byte[100];
//        List<double>hierarchyHolder = new ArrayList<>();
        int cols = hierarchy.cols();
        int rows = hierarchy.rows();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
//                hierarchyHolder.add(hierarchy.get(i,j));
                Log.d("hierarchy", " " + hierarchy.get(i, j).toString());   //hierarchy.get(i,j) is a double[] type.
            }
        }
        //prints out hierarchy in giberrish
        hierarchy.release();


//======================= YASSSS!! WORKED!  =D  RETURNS ONLY THE LARGEST CONTOUR!  =D =D =D ==== ===============
        double maxArea1 = 0;
        int maxAreaContourIndex1 = 0;   //try define globally, cos error msg say is index 0, so means not updated!


//        MatOfPoint max_contours = new MatOfPoint();
//        Rect r = null;
//        ArrayList<Rect> rect_array = new ArrayList<Rect>();

        for (int i = 0; i < contours.size(); i++) {
//            if(Imgproc.contourArea(contours.get(i)) > 300) {   //Size of Mat contour @ that particular point in ArrayList of Points.
            double contourArea1 = Imgproc.contourArea(contours.get(i));   //Size of Mat contour @ that particular point in ArrayList of Points.
            if (maxArea1 < contourArea1) {
                maxArea1 = contourArea1;
                maxAreaContourIndex1 = i;   //just return 1 that largest Contour out of many contours.
                Log.d("value of", "maxAreaContourIndex1=" + maxAreaContourIndex1);   //0
            }
        }
        Log.d("value of", "maxAreaContourIndex1=" + maxAreaContourIndex1);  //0


        Imgproc.cvtColor(InputFrame, InputFrame, Imgproc.COLOR_HSV2RGB);


        //this part got error, cos no matter whether got yellow object in frame, will perform this calculation, so when frame no yellow color, error occurs cos no Contour detected, contour Size = 0,
        //FATAL IndexOutOfBoundsException occurs!
        //Therefore nd to bound this sect of code within try catch loop to handle error gracefully!

        try {
            //   FATAL EXCEPTION IndexOutOfBoundsExeption: Index:0, Size:0;
            List<Moments> mu = new ArrayList<>(contours.size());
            mu.add(Imgproc.moments(contours.get(maxAreaContourIndex1)));    //Just adding that 1 Single Largest Contour (largest ContourArea) to arryalist to be computed for MOMENTS to compute CENTROID POS!
            // maxAreaContourIndex must be < contourSize in order to prevent IndexOutOfBoundsException being thrown1

            List<Point> mc = new ArrayList<>(contours.size());   //the Circle centre Point!
            //add 1e-5 to avoid division by zero
            mc.add(new Point(mu.get(0).m10 / (mu.get(0).m00 + 1e-5), mu.get(0).m01 / (mu.get(0).m00 + 1e-5)));   //index 0 cos there shld only be 1 contour now, the largest one only!
            //notice that it only adds 1 point, the centroid point. Hence only 1 point in the mc list<Point>, so ltr reference that point w an index 0!

            Random random = new Random(12345);

            Scalar color = new Scalar(random.nextInt(256), random.nextInt(256), random.nextInt(256));  //return a random color

            Imgproc.circle(InputFrame, mc.get(0), 15, color, -1);   //just to plot the small central point as a dot on the detected ImgObject.

        //====================== If want to put Bounding Rect ard Largest Contour Object! ==================
            Rect r = Imgproc.boundingRect(contours.get(maxAreaContourIndex1));    // sOLN Saviour =D :  https://ratiler.wordpress.com/2014/09/08/detection-de-mouvement-avec-javacv/
            Imgproc.rectangle(InputFrame, r.br(), r.tl(), new Scalar(255, 0, 255), 2);    //returns r & b combination, line also thicker.
            Log.d("placing boundingRect on","Largest Contour Object");
            Log.d("",""+r.br());   //return new Point(x + width, y + height);
            Log.d("",""+r.tl());   //return new Point(x, y);

        //===================================================================================================

        } catch (IndexOutOfBoundsException e) {
            Log.d("INDEX OUT OF BOUNDS EXCEPTION", "DETECTED" + e);
        }
//        }catch(ArrayIndexOutOfBoundsException Ae){
//            Log.d("ARRAYINDEX OUT OF BOUNDS EXCEPTION","DETECTED" + Ae);
//        }

//================ =D YASSS! ABOVE CODE Finally Fucking Working!  WORKED!  WORKED WORKED! :D :D :D  FUCKING FINALLY! ===================================


//=============== Plot Rectangle / Circle @ fixed positions, corner of the screen! ==========================

//        Scalar color = new Scalar(0,255,0);   //r,g,b, so only show green box

        //https://stackoverflow.com/questions/40120433/draw-rectangle-in-opencv?rq=1 -- how to use Rect = new Rect()
        //https://answers.opencv.org/question/122532/how-to-floodfill-an-image-with-java-api/ -- how to use floodfill using Rect.
        //https://qiita.com/yeb8jo/items/2ab97dc69b375b501fba - floodfill implementation in Android
        //@@--> THIS THE ONE :D -  https://github.com/zylo117/SpotSpotter/blob/master/src/pers/zylo117/spotspotter/patternrecognition/ROI_Irregular.java - floodfill implementation in Android



////        ObjectCreatorThread.createObject(InputFrame);   //run as a thread, out put each rect Thread by Thread
//        Log.d("creating Thread Object","");
//        ObjectCreatorThread o1 = new ObjectCreatorThread(InputFrame);   //run as a thread, out put each rect Thread by Thread
//        Log.d("Thread Object","created");
//        new Thread(o1).start();   //run as a thread, out put each rect Thread by Thread
//        Log.d("Running Thread Object","");
//
//        Set<ObjectCreatorThread> mySet = Collections.newSetFromMap(new ConcurrentHashMap<ObjectCreatorThread, Boolean>());
////        HashSet<ObjectCreatorThread> set = Collections.newSetFromMap( new ConcurrentHashMap<ObjectCreatorThread,o1>() );
//        synchronized(o1) {
////            Lock myLock= new Lock();   //Lock() is an abstract method, cannot be implemented, cos Lock is an interface class, empty constructor, no implementation for that method yet.
////            myLock.lock();
//            mySet.add(o1);
//        }
//        synchronized(mySet) {
//            for (Iterator<ObjectCreatorThread> i = mySet.iterator(); i.hasNext();) {
//                ObjectCreatorThread obj= i.next();
////                if (!obj.isSmt()) {
//                if (!obj.equals(o1)) {
//                    i.remove();
//                }
//            }
//        }

   ////=====================  =D WORKED! DREW 4 RECTANGLES ON THE 4 CORNERS OF THE SCREEN! ==============================


        Rect r1 = new Rect(0,0,300,300);
        Rect r2 = new Rect(772,0,300,300);
        Rect r3 = new Rect(0,772,300,300);
        Rect r4 = new Rect(772,772,300,300);

        ArrayList<Rect> rect_array = new ArrayList<Rect>();
        rect_array.add(r1);
        rect_array.add(r2);
        rect_array.add(r3);
        rect_array.add(r4);


//        final Mat maskCopyTo = Mat.zeros(InputFrame.size(), CvType.CV_8UC1); /// 创建copyTo方法的mask，大小与原图保持一致
        final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);    //as required in documentation https://docs.opencv.org/2.4/modules/imgproc/dType.8UC1);


        if (rect_array.size() > 0) {   //if got more than 1 rect found in rect_array, draw them out!

            Iterator<Rect> it2 = rect_array.iterator();
            while (it2.hasNext()) {
                Rect obj = it2.next();


                Imgproc.rectangle(InputFrame, obj.br(), obj.tl(), new Scalar(0, 255, 0), 4);

                Imgproc.floodFill(InputFrame, maskFloodFill,new Point((obj.tl().x +obj.br().x) / 2, (obj.tl().y + obj.br().y) / 2), new Scalar(0,255,0), new Rect() , new Scalar(120,120,120),new Scalar(120,120,120),4);

            }
        }


   ////============================================================================================


//   ////===================== 1ST SUCCESSFUL RECTANGLE DRAWN ON SCREEN! =D ==============================
//        final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);    //as required in documentation https://docs.opencv.org/2.4/modules/imgproc/doc/miscellaneous_transformations.html?highlight=floodfill
//        Rect r1 = new Rect(0,0,300,300);
//        Imgproc.rectangle(InputFrame,r1.tl(),r1.br(),new Scalar(0,255,0),4);
//
////        Imgproc.floodFill(InputFrame, maskFloodFill,new Point((r1.tl().x + r1.br().x) / 2, (r1.tl().y + r1.br().y) / 2), new Scalar(0,255,0), new Rect(), new Scalar(0,0,0),new Scalar(0,0,0),4);
//        Imgproc.floodFill(InputFrame, maskFloodFill,new Point((r1.tl().x + r1.br().x) / 2, (r1.tl().y + r1.br().y) / 2), new Scalar(0,255,0), null, new Scalar(20,20,20),new Scalar(20,20,20),4);   //YASS!! IT FKEN FINALLY WORKED!  =D   https://github.com/zylo117/SpotSpotter/blob/master/src/pers/zylo117/spotspotter/patternrecognition/ROI_Irregular.java  THANK YOU FKEN GOD! THANKS!  =D SAVIOUR!
//   ////============================================================================================
//        Imgproc.rectangle(InputFrame, new Point(0.0,0.0) ,new Point(300.0,300.0), new Scalar(0,255,0),4);    //top left corner
//        Imgproc.rectangle(InputFrame, new Point(772.0,0.0) ,new Point(1072.0,300.0), new Scalar(0,255,0),4);    //top right corner
//        Imgproc.rectangle(InputFrame, new Point(0.0,772.0) ,new Point(300.0,1072.0), new Scalar(0,255,0),4);    //bottom left corner
//        Imgproc.rectangle(InputFrame, new Point(772.0,772.0) ,new Point(1072.0,1072.0), new Scalar(0,255,0),4);    //bottom right corner


                
//Imgproc.floodFill();   //got such method =D
//Core.floodfill  //no such method

//==============================================================================================================



//        return mIntermediateMat;   //returns the canny edge of the yellowMaskMorphed
        return InputFrame;  //Works =D =D =D !  YASSSS!!            //Mat Type output.   So OnCameraFrame we only work on Mat!
//        return maskCopyTo;  //Works =D =D =D !  YASSSS!!            //Mat Type output.   So OnCameraFrame we only work on Mat!
//        return yellowMaskMorphed;  //test this. yup ok Working =D
//        return maskForYellow;  //test this. yup ok Working =D

    }



    //https://opencv-python-tutroals.readthedocs.io/en/latest/py_tutorials/py_gui/py_drawing_functions/py_drawing_functions.html  -  How to Draw stuffs on screen!


    //https://www.youtube.com/watch?v=QfQE1ayCzf8 - How to Start a Background Thread in Android, to let it run ASYNC in background, wont obstruct Main() thread.
    //1. extend Thread class, which itself implements Runnable, click into it to see
    //2. implement the runnable interface itself!
    class DisplayObjectThread extends Thread{


        //

    }


//    class myLock implements Lock{
//
//        @Override
//        public void lock() {
//
//        }
//    }

}


