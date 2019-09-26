package com.example.opencv_tutorial;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ColorSplitRangeDetection extends AppCompatActivity {

    //Aim : just want to read  in an image from drawable - a static orange image - and detect the HSV Values,
    //split them up & read the individual H, S, V channel values that is detected form that 1 image alone.

    TextView hChannel, sChannel, vChannel;   //nd to define globally, not just in the onCreate method!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.colorsegmentation);
        OpenCVLoader.initDebug();


       hChannel = (TextView) findViewById(R.id.hChannelUpdate);   //Type cast (TextView) to make sure that the View type is laso of the same (TextView) Type, form of checking correctness of datatype.
       sChannel = (TextView) findViewById(R.id.sChannelUpdate);   //another way is to just Log the output, no nd create to display the value in a widget textView!
       vChannel = (TextView) findViewById(R.id.vChannelUpdate);
//       TextView hChannel = (TextView) findViewById(R.id.hChannelUpdate);   //Type cast (TextView) to make sure that the View type is laso of the same (TextView) Type, form of checking correctness of datatype.
//       TextView sChannel = (TextView) findViewById(R.id.sChannelUpdate);   //another way is to just Log the output, no nd create to display the value in a widget textView!
//       TextView vChannel = (TextView) findViewById(R.id.vChannelUpdate);

    }



// no nd to flip image here cos we not using the JavaCameraView, we just reading from drawable folder our img file. which is alrdy in Portrait mode!

    public void displayToast(View v){

        Mat img = null;   //:O img is of MAT Type.

        Mat orangeHSVmask = new Mat();

        try {
            img = Utils.loadResource(getApplicationContext(), R.drawable.orange1min);     // Utils is the Matrix Core operation Library, load the img src path of (R.drawable.test)  to the img  var.

        } catch (IOException e) {
            e.printStackTrace();
        }


//        //rotate Mat 90 degrees in OpenCv - https://amin-ahmadi.com/2017/06/01/how-to-rotate-andor-flip-mirror-images-in-opencv/
//        Core.transpose(img, img);
//        Core.flip(img, img, 1);


//================== find the circle - which is my orange CIRCLE =============================

        Mat img_clone2 = img.clone();

        Imgproc.cvtColor(img_clone2,img_clone2,Imgproc.COLOR_BGR2GRAY);   //convert to gray scale to prepare for edge detection Hough Transform.

        Imgproc.medianBlur(img_clone2, img_clone2, 5);   //a ksize x ksize aperture linear size, must be odd and greater than 1.  Each channel of a multi-channel image is processed independently.
        //blur it to reduce the noise.
        Mat circles = new Mat();

        //https://docs.opencv.org/3.4/d4/d70/tutorial_hough_circle.html
        //The Hough gradient method is made up of 2 stages - First stage involves edge detection and finding the possible circle centers.  Second stage finds the best radius for each candidate center.
        Imgproc.HoughCircles(img_clone2, circles, Imgproc.HOUGH_GRADIENT, 1.0,    //Applies the Hough Circle Transform to the blurred image
                (double)img_clone2.rows()/2,                          // change this value to detect circles with different distances to each other
//            (double)mGray.rows()/2,                          // change this value to detect circles with different distances to each other
                100.0, 30.0, 1, 200);   // change the last two parameters (min_radius & max_radius) IN PIXELS!  to detect larger circles

//    int col = circles.cols();   //foreach not applicable to int type

        for (int x = 0; x < circles.cols(); x++) {    //Display the detected circle & Centre pos point
            double[] c = circles.get(0, x);

            Point center = new Point(Math.round(c[0]), Math.round(c[1]));   //Circle centre

            // circle perimeter outline
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(img_clone2, center, radius, new Scalar(255,0,255), 3, 8, 0 );

            //CORE.SUMELEMS - SUM the HSV Values in this region??

        }



// ====================== Prepare HSV iMage ==============================

//     Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2HSV, 4);
        Imgproc.cvtColor(img_clone2, img, Imgproc.COLOR_GRAY2BGR);
        Imgproc.cvtColor(img_clone2, img, Imgproc.COLOR_BGR2HSV);  //Convert GRAYSCALE TO HSV color scheme.

        //perform Color segmentation to extract out the color  //what i want to do rn!
        //opencv detect color range within picture
        //opencv simple blob detector

//        //CORE.SUMELEMS - SUM the HSV Values in this circle region?? - Nope, not what i intended to do !
//        Core.sumElems(circles, ? ,? ,? );
//        List<Mat> split_channel = new ArrayList<>();
//        Core.split(circles, split_channel);

//        Mat HChannel = split_channel.get(0);
//        Mat SChannel = split_channel.get(1);
//        Mat VChannel = split_channel.get(2);   //another way is to just Log the output, no nd create to display the value in a widget textView!




//        // Orange Mask
//        Core.inRange(img, new Scalar(15,20,140), new Scalar(23,100,255), orangeHSVmask);


//perform color segmentation.
// https://stackoverflow.com/questions/51871134/hsv-opencv-colour-range - split to indiv color range for detection of color pane range.

//        Wanna try to read in HSV plane
//                then split to the individual channels H, S, V respectively, to read the natural values

//        double Hue = 0.0;
//        double Saturation = 0.0;
//        double Birghtness = 0.0;



////===================== NEW DIRECTION ======================================
//
//I dun want to supply the Orange Mask, I want program to detect the color within the CIRCLE Region of Interest (ROI) we identify using Hough Transform,
//convert the ROI into HSV color scheme, then we split it into the indiv chnnels, return back the individual values H, S, V respectively!
//
//        List<Mat> split_channel = new ArrayList<>();    //https://www.programcreek.com/java-api-examples/?class=org.opencv.core.Core&method=split
//        Core.split(orangeHSVmask, split_channel);
//
//        Mat HChannel = split_channel.get(0);
//        Mat SChannel = split_channel.get(1);
//        Mat VChannel = split_channel.get(2);
//
//        int HChannelInt = (int)Core.sumElems(HChannel).val[0];   //returns as Scalar, then convert Scalar to int.
//        int SChannelInt = (int)Core.sumElems(SChannel).val[0];   //returns as Scalar, then convert Scalar to int.
//        int VChannelInt = (int)Core.sumElems(VChannel).val[0];   //returns as Scalar, then convert Scalar to int.
//
//
//        hChannel.setText(HChannelInt);
//        sChannel.setText(HChannelInt);
//        vChannel.setText(HChannelInt);
//
//
//
////        Mat img_result = img.clone();     //clone an exact duplicate of the original Mat img.
//
////        Imgproc.Canny(img, img_result, 0, 150);   //perform Canny threshold & edge detection map the result from the original to
//        // Imgproc.Canny(Mat image, Mat edges, threshold1, threshold2)    //detecting edges
//
//        //How to set/create/display the processed image back onto screen!
//        Bitmap img_bitmap = Bitmap.createBitmap(img_result.cols(), img_result.rows(),Bitmap.Config.ARGB_8888);    //=D create Bitmap img that I can then work with!
//        Utils.matToBitmap(img_result, img_bitmap);   //convert/map Matrix info to Bitmap Pixels.
//        ImageView imageView = findViewById(R.id.img);    //UI Resource ID.
//        imageView.setImageBitmap(img_bitmap);    //set the BitMap img to the corresp Resource Id of the imageView =D !


    }
}
