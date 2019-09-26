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
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class hsv_color_3_yellow extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
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

            //Nvr Set cos alrdy set it to FullScreen under res/values/styles.xml & Manifest.xml files.
//            cameraBridgeViewBase.setMaxFrameSize(640,480);
//            cameraBridgeViewBase.setMaxFrameSize(1080,1980);   //the resolution dimension of my phone's screen display !
//            cameraBridgeViewBase.setMaxFrameSize(2000,1980);   //the resolution dimension of my phone's screen display !


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
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        InputFrame = inputFrame.rgba();

        Core.transpose(InputFrame,mat1); //transpose mat1(src) to mat2(dst), sorta like a Clone!
        Imgproc.resize(mat1,mat2,InputFrame.size(),0,0,0);    // params:(Mat src, Mat dst, Size dsize, fx, fy, interpolation)   Extract the dimensions of the new Screen Orientation, obtain the new orientation's surface width & height.  Try to resize to fit to screen.
        Core.flip(mat2,InputFrame,-1);   // mat3 now get updated, no longer is the Origi inputFrame.rgba BUT RATHER the transposed, resized, flipped version of inputFrame.rgba().

//        return InputFrame;  //this line here alrdy the freeze frame prob start to show alrdy!

        int rowWidth = InputFrame.rows();
        int colWidth = InputFrame.cols();

        //https://medium.com/@ckyrkou/color-thresholding-in-opencv-91049607b06d - manage to extract out the color Blue
        //https://stackoverflow.com/questions/26218280/thresholding-rgb-image-in-opencv

        //Try to convert to GRAYSCALE color scheme instead following this article https://medium.com/@ckyrkou/color-thresholding-in-opencv-91049607b06d

        Imgproc.cvtColor(InputFrame,InputFrame,Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(InputFrame,InputFrame,Imgproc.COLOR_RGB2HSV);


        Lower_Yellow = new Scalar(21,150,150);    //HSV color scale  H to adjust color, S to coontrol color variation, V is indicator of amt of light required to be shine on object to be seen.
//        Lower_Yellow = new Scalar(21,150,100);    //HSV color scale  H to adjust color, S to coontrol color variation, V is indicator of amt of light required to be shine on object to be seen.
//        Lower_Yellow = new Scalar(21,150,0);    //HSV color scale  H to adjust color, S to coontrol color variation, V is indicator of amt of light required to be shine on object to be seen.
        Upper_Yellow = new Scalar(31,255,360);    //HSV color scale

        //Imgproc.cvtColor(InputFrame,maskForBlue,Imgproc.COLOR_RGBA2GRAY);
        Core.inRange(InputFrame,Lower_Yellow, Upper_Yellow, maskForYellow);

//        Imgproc.GaussianBlur(maskForYellow, maskForYellow, new Size(3,3),0,0);   //nope, BLURS only works on edge detection, so only after apply threshold/ Canny img?
//        Imgproc.medianBlur(maskForYellow, maskForYellow, 3);   //must be odd num size & greater than 1 .  doesnt work too well ?!?

        //Imgproc.cvtColor(maskForBlue,rgbMasked_leftBlue,Imgproc.COLOR_GRAY2RGBA);


//        //line 193-195 not working!
//        Core.bitwise_and(maskForYellow,inputFrame.rgba(),rgbMasked_leftYellow);

//        return rgbMasked_leftYellow;
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
//        Imgproc.morphologyEx(maskForYellow, yellowMaskMorphed, Imgproc.MORPH_TOPHAT, kernel, anchor, iterations);    //returns only the boundary parameter of the object.
//        Imgproc.dilate(maskForYellow, yellowMaskMorphed, kernel, anchor, iterations);  //white region becomes more prominent .  dilate, vry useful in searching for connect components.
//        Imgproc.erode(maskForYellow, yellowMaskMorphed, kernel, anchor, iterations);  //black region becomes more prominent
        Imgproc.morphologyEx(maskForYellow, yellowMaskMorphed, Imgproc.MORPH_CLOSE, kernel, anchor, iterations);   //dilate first to remove then erode.  White regions becomes more pronounced, erode away black regions
//        Imgproc.morphologyEx(maskForYellow, yellowMaskMorphed, Imgproc.MORPH_BLACKHAT, kernel, anchor, iterations);   //also returns boundary just like TOPHAT.
                                                                                                                //YUP, MORPH_CLOSE better option over MORPH_OPEN -> See https://docs.opencv.org/trunk/d9/d61/tutorial_py_morphological_ops.html



        //https://docs.opencv.org/3.4/da/d0c/tutorial_bounding_rects_circles.html
        //Convert that mask to Canny for edge detection., else too many points detected in the mask, cos inside region also filled/shaded

        //a. convert to grayscale first
//        Mat mgray = new Mat();
//        Imgproc.cvtColor(maskForYellow,mgray,Imgproc.COLOR_HSV2RGB);  //HMM no HSV2GRAY conversion, Technically, my mask is alrdy Thresholded, only not Canny, so can just proceed to canny? to achieve a MORPH_TOPHAT like EFFECT, Just detect the boundary edges.

        //b. then Canny for edge detection
        Mat mIntermediateMat = new Mat();
//        Imgproc.GaussianBlur(yellowMaskMorphed,mIntermediateMat,new Size(9,9),2,2);
        Imgproc.GaussianBlur(yellowMaskMorphed,mIntermediateMat,new Size(9,9),0,0);   //better result than kernel size (3,3, maybe cos reference area wider, bigger, can decide better whether inrange / out of range.
//        Imgproc.GaussianBlur(yellowMaskMorphed,mIntermediateMat,new Size(5,5),0,0);
//        Imgproc.GaussianBlur(yellowMaskMorphed,mIntermediateMat,new Size(3,3),0,0);
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
        Log.d("contour","size = " + contours.size());   //returns 21, 91, 120, 104, 116, 58, ......  varying contour size detected & returned. Observe how contours can exceed 100.  :O WAOW!
//        byte[] arr = new byte[100];
//        List<double>hierarchyHolder = new ArrayList<>();
        int cols = hierarchy.cols();
        int rows = hierarchy.rows();
        for (int i=0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
//                hierarchyHolder.add(hierarchy.get(i,j));
                Log.d("hierarchy"," " + hierarchy.get(i,j).toString());   //hierarchy.get(i,j) is a double[] type.
            }
        }
        //prints out hierarchy in giberrish
        hierarchy.release();




//===================== YASSSS!! WORKED!  =D  RETURNS ONLY THE LARGEST CONTOUR!  =D =D =D ==== =================
        double maxArea1 = 0;
        int maxAreaContourIndex1 = 0;   //try define globally, cos error msg say is index 0, so means not updated!


//        MatOfPoint max_contours = new MatOfPoint();
//        Rect r = null;
//        ArrayList<Rect> rect_array = new ArrayList<Rect>();

        for(int i=0; i < contours.size(); i++) {
//            if(Imgproc.contourArea(contours.get(i)) > 300) {   //Size of Mat contour @ that particular point in ArrayList of Points.
            double contourArea1 = Imgproc.contourArea(contours.get(i));   //Size of Mat contour @ that particular point in ArrayList of Points.
                if (maxArea1 < contourArea1){
                    maxArea1 = contourArea1;
                    maxAreaContourIndex1 = i;   //just return 1 that largest Contour out of many contours.
                    Log.d("value of","maxAreaContourIndex1=" + maxAreaContourIndex1);   //0
                }
        }
        Log.d("value of","maxAreaContourIndex1=" + maxAreaContourIndex1);  //0


        Imgproc.cvtColor(InputFrame, InputFrame, Imgproc.COLOR_HSV2RGB);


//this part got error, cos no matter whether got yellow object in frame, will perform this calculation, so when frame no yellow color, error occurs cos no Contour detected, contour Size = 0,
//FATAL IndexOutOfBoundsException occurs!
//There for nd to bound this sect of code within try catch loop to handle error gracefully!

        try {
//        List<MatOfPoint> largestContour = new ArrayList<>();   //Error Msg -> java.lang.IndexOutOfBoundsException: Index: 0, Size: 0.
//                                                               //Cos nvr define Size of ArrayList like  E.g ArrayList<>(10)

//        List<MatOfPoint> largestContour = new ArrayList<>(contourSize*2);    //CONTENTS OF largestContour are 'updated' but never queried.
////        for (int j=0; j<maxAreaIndex1; j++){     //no nd to loop thru contour again, just plot assign that 1 contour
////            newContours.add(contours.get(j));
////        }
//        largestContour.add(contours.get(maxAreaContourIndex1));   //contours.get(i) is a MAT array[i] type. so we adding a MAT to  ArrayList largestContour.
            //Error Msg, arrayIndexOutOfBoundsExweption: Index: 84, Size:58, clearly the index has overshot the Size which varies, see the LogCat print output in LINE 284 I inserted to print out.  So in this case we shld not asisgn arrayList to size of contour, we need it to be 100, to be more than required for the Index!


            //========= Compute CENTROID POS! ======================  WHAT WE WANT TO SHOW ON SCREEN EVENTUALLY!

//        List<Moments> mu = new ArrayList<>(newContours.size());    //HUMoments
//        for (int i = 0; i < newContours.size(); i++) {     //similarly n0o nd to compute moments of each indiv contour, just that 1 single Largest contour can alrdy.
//            mu.add(Imgproc.moments(newContours.get(i)));
//        }


            //   FATAL EXCEPTION IndexOutOfBoundsExeption: Index:0, Size:0;
            List<Moments> mu = new ArrayList<>(contours.size());
            mu.add(Imgproc.moments(contours.get(maxAreaContourIndex1)));    //maxAreaContourIndex must be < contourSize in order to prevent IndexOutOfBoundsException being thrown1

//        List<Point> mc = new ArrayList<>(newContours.size());   //the Circle centre Point!
//        for (int i = 0; i < newContours.size(); i++) {
//            //add 1e-5 to avoid division by zero
//            mc.add(new Point(mu.get(i).m10 / (mu.get(i).m00 + 1e-5), mu.get(i).m01 / (mu.get(i).m00 + 1e-5)));
//        }

            List<Point> mc = new ArrayList<>(contours.size());   //the Circle centre Point!
            //add 1e-5 to avoid division by zero
            mc.add(new Point(mu.get(0).m10 / (mu.get(0).m00 + 1e-5), mu.get(0).m01 / (mu.get(0).m00 + 1e-5)));   //index 0 cos there shld only be 1 contour now, the largest one only!
            //notice that it only adds 1 point, the centroid point. Hence only 1 point in the mc list<Point>, so ltr reference that point w an index 0!

            Random random = new Random(12345);

//        for (int i = 0; i < newContours.size(); i++) {
            Scalar color = new Scalar(random.nextInt(256), random.nextInt(256), random.nextInt(256));  //return a random color

            Imgproc.circle(InputFrame, mc.get(0), 20, color, -1);   //just to plot the small central point as a dot on the detected ImgObject.
//        }

//        //====================== If want to put Bounding Rect ard Largest Contour Object! ==================
//            Rect r = Imgproc.boundingRect(contours.get(maxAreaContourIndex1));    // sOLN Saviour =D :  https://ratiler.wordpress.com/2014/09/08/detection-de-mouvement-avec-javacv/
//            Imgproc.rectangle(InputFrame, r.br(), r.tl(), new Scalar(255, 0, 255), 1);    //returns r & b combination, line also thicker.
//            Log.d("placing boundingRect on","Largest Contour Object");
//        //===================================================================================================

        }catch(IndexOutOfBoundsException e) {
            Log.d("INDEX OUT OF BOUNDS EXCEPTION", "DETECTED" + e);
        }
//        }catch(ArrayIndexOutOfBoundsException Ae){
//            Log.d("ARRAYINDEX OUT OF BOUNDS EXCEPTION","DETECTED" + Ae);
//        }


//===================== =D YASSS! ABOVE CODE Working!  WORKED!  WORKED WORKED! :D :D :D  FUCKING FINALLY! =================================




//=============== If want to detect multiple contours & output multiple Bounding Rects, supply rects to arrayList & use an iterator to iterate/cycle/loop thru ArrayList ===============
//        Rect r = null;
//        ArrayList<Rect> rect_array = new ArrayList<Rect>();
//
//        r = Imgproc.boundingRect(contours.get(maxAreaContourIndex1));    // sOLN Saviour =D :  https://ratiler.wordpress.com/2014/09/08/detection-de-mouvement-avec-javacv/
//        rect_array.add(r);

//        if (rect_array.size() > 0) {   //if got more than 1 rect found in rect_array, draw them out!
//
//            Iterator<Rect> it2 = rect_array.iterator();
//            while (it2.hasNext()) {
//                Rect obj = it2.next();
//                Imgproc.rectangle(InputFrame, obj.br(), obj.tl(),
//                        new Scalar(0, 255, 0), 1);
//            }
//
//        }
//==================================================================================================================




////=========== WORKING, BUT detects ALL Yellow COntours Detected + returns ALL Centroid Pos of ALL the CONTOURS detected! Not what we want/intent! =============
//
//        // ==========  SOLN SAVIOURS =D  https://ratiler.wordpress.com/2014/09/08/detection-de-mouvement-avec-javacv/   ===============
//        //want to find largest contour opencv Java - use ContourArea, just nd to track/return the largest contourArea.
//        //https://stackoverflow.com/questions/38759925/how-to-find-largest-contour-in-java-opencv
//
//
//        double maxArea1 = 0;
//        int maxAreaIndex1 = 0;
////        MatOfPoint max_contours = new MatOfPoint();
//        Rect r = null;
//        ArrayList<Rect> rect_array = new ArrayList<Rect>();
//
//        for(int i=0; i < contours.size(); i++) {
////            if(Imgproc.contourArea(contours.get(i)) > 300) {   //Size of Mat contour @ that particular point in ArrayList of Points.
//            double contourArea1 = Imgproc.contourArea(contours.get(i));   //Size of Mat contour @ that particular point in ArrayList of Points.
//                if (maxArea1 < contourArea1){
//                    maxArea1 = contourArea1;
//                    maxAreaIndex1 = i;
//                }
////                maxArea1 = Imgproc.contourArea(contours.get(i));    //assigned but nvr used
////                max_contours = contours.get(i);
//                r = Imgproc.boundingRect(contours.get(maxAreaIndex1));    // sOLN Saviour =D :  https://ratiler.wordpress.com/2014/09/08/detection-de-mouvement-avec-javacv/
//         // format: Imgproc.boundingRect(Mat array)  so now we know/can deduce that contours.get(i) is a Mat array[i] type.
//                rect_array.add(r);  //will only have 1 r in the array eventually, cos we will only take the one w largestContourArea.
//        }
//
//
//        Imgproc.cvtColor(InputFrame, InputFrame, Imgproc.COLOR_HSV2RGB);
//
//
//        if (rect_array.size() > 0) {   //if got more than 1 rect found in rect_array, draw them out!
//
//            Iterator<Rect> it2 = rect_array.iterator();    //only got 1 though, this method much faster than drawContour, wont lag. =D
//            while (it2.hasNext()) {
//                Rect obj = it2.next();
////                if
//                Imgproc.rectangle(InputFrame, obj.br(), obj.tl(),
//                        new Scalar(0, 255, 0), 1);
//            }
//
//        }
//
//
//        //========= Compute CENTROID POS! ======================  WHAT WE WANT TO SHOW ON SCREEN EVENTUALLY!
//
//        List<Moments> mu = new ArrayList<>(contours.size());    //HUMoments
//        for (int i = 0; i < contours.size(); i++) {
//            mu.add(Imgproc.moments(contours.get(i)));
//        }
//
//        List<Point> mc = new ArrayList<>(contours.size());   //the Circle centre Point!
//        for (int i = 0; i < contours.size(); i++) {
//            //add 1e-5 to avoid division by zero
//            mc.add(new Point(mu.get(i).m10 / (mu.get(i).m00 + 1e-5), mu.get(i).m01 / (mu.get(i).m00 + 1e-5)));
//        }
//
//
//        Random random = new Random(12345);
//
//        for (int i = 0; i < contours.size(); i++) {
//            Scalar color = new Scalar(random.nextInt(256), random.nextInt(256), random.nextInt(256));
//
//            Imgproc.circle(InputFrame, mc.get(i), 20, color, -1);   //just to plot the small central point as a dot on the detected ImgObject.
//        }
//



////================== 1st Initial Amatuer Try, detect contours w Areas bigger than 300! Not even the Largest, so returns Multiple objects! ============================

//        double maxArea;
////        MatOfPoint max_contours = new MatOfPoint();
//        Rect r = null;
//        ArrayList<Rect> rect_array = new ArrayList<Rect>();
//
//        for(int i=0; i < contours.size(); i++) {
//            if(Imgproc.contourArea(contours.get(i)) > 300) {   //Size of Mat contour @ that particular point in ArrayList of Points.
//                Log.d("DetectAreaOfObjectInFocus","Success");
////                maxArea = Imgproc.contourArea(contours.get(i));    //assigned but nvr used
////                max_contours = contours.get(i);
//                r = Imgproc.boundingRect(contours.get(i));    // sOLN Saviour =D :  https://ratiler.wordpress.com/2014/09/08/detection-de-mouvement-avec-javacv/
//                rect_array.add(r);
////                break;    //else will keep looping/going on forever never ending, never exit program
//            }
//            else{
//                continue;
//            }
//        }
//
//        Imgproc.cvtColor(InputFrame, InputFrame, Imgproc.COLOR_HSV2RGB);
//
//        if (rect_array.size() > 0) {   //if got more than 1 rect found in rect_array, draw them out!
//
//            Iterator<Rect> it2 = rect_array.iterator();
//            while (it2.hasNext()) {
//                Rect obj = it2.next();
//                Imgproc.rectangle(InputFrame, obj.br(), obj.tl(),
//                        new Scalar(0, 255, 0), 1);
//            }
//
//        }



//        return mIntermediateMat;   //returns the canny edge of the yellowMaskMorphed
        return InputFrame;  //Works =D =D =D !  YASSSS!!
//        return yellowMaskMorphed;  //test this. yup ok Working =D
//        return maskForYellow;  //test this. yup ok Working =D




////=============================================================================================
//
//        double epsilon = 0.1*Imgproc.arcLength(new MatOfPoint2f(max_contours.toArray()),true);   //as small as poss to make error of dist btwn 2 points as small as poss, less drastic changes means more plotting a accurate curve.
//        MatOfPoint2f approx = new MatOfPoint2f();
//        Imgproc.approxPolyDP(new MatOfPoint2f(max_contours.toArray()),approx, epsilon,true);    //The functions approxPolyDP approximate a curve or a polygon with another curve/polygon with less vertices so that the distance between them is less or equal to the specified precision. It uses the Douglas-Peucker algorithm
//
//        RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(max_contours.toArray()));



////=========================  SIMPLE BLOB DETECTOR ================================
//        blobdetector:
//        1.https://github.com/Benjoyo/MotionTracker_SmartRoom/blob/master/params
//        2.https://github.com/Benjoyo/MotionTracker_SmartRoom
//        3. https://www.programcreek.com/java-api-examples/?code=Benjoyo/MotionTracker_SmartRoom/MotionTracker_SmartRoom-master/src/de/bennet_krause/motiontracking/MotionTracker.java
//        4. https://programtalk.com/vs/GRIP/core/src/main/java/edu/wpi/grip/core/operations/composite/FindBlobsOperation.java/
//
//
//        Centroid POS



        //@#Nxt now move on to simpleBlobDetector/
        // ConnnectedComponents/
        // findContour hIERARCHY & ContourArea



//        final SimpleBlobDetector blobDetector = SimpleBlobDetector.create(new SimpleBlobDetector
//                .Params()
//                .filterByArea(true)
//                .minArea(minArea.intValue())
//                .maxArea(Integer.MAX_VALUE)
//
//                .filterByColor(true)
//                .blobColor(darkBlobs ? (byte) 0 : (byte) 255)
//
//                .filterByCircularity(true)
//                .minCircularity(circularity.get(0).floatValue())
//                .maxCircularity(circularity.get(1).floatValue()));



////======================  previously TRIED STUFFs , Intial Amatuer Stage, testing Threshold cvInRange() for Yellow COlor Objects =========================

//        Core.bitwise_and(InputFrame, InputFrame, rgbMasked_leftBlue ,maskForOrange);   //Prof goh say no nd to apply Mask back, just nd to extract the centroid cross hair

//        maskForOrange.release();
//        return maskForYellow;    //WORKING!  USE THIS TO TEST MASK!
//        return rgbMasked_leftOrange;



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
////   blobdetector:
//        1. https://programtalk.com/vs/GRIP/core/src/main/java/edu/wpi/grip/core/operations/composite/FindBlobsOperation.java/
//        2. https://www.programcreek.com/java-api-examples/?code=Benjoyo/MotionTracker_SmartRoom/MotionTracker_SmartRoom-master/src/de/bennet_krause/motiontracking/MotionTracker.java

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
