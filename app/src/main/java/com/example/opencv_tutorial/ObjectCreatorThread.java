package com.example.opencv_tutorial;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Handler;

public class ObjectCreatorThread implements Runnable{


    static ArrayList<Rect> rect_array = new ArrayList<Rect>();
    Mat inputFrame;

    ObjectCreatorThread(Mat InputFrame) {
        inputFrame = InputFrame;
        Rect r1 = new Rect(0, 0, 300, 300);
        Rect r2 = new Rect(772, 0, 300, 300);
        Rect r3 = new Rect(0, 772, 300, 300);
        Rect r4 = new Rect(772, 772, 300, 300);

        rect_array.add(r1);
        rect_array.add(r2);
        rect_array.add(r3);
        rect_array.add(r4);
        Log.d("rect_array","constructed" + rect_array);
    }


//    public static void createObject(Mat InputFrame) {
//        Rect r1 = new Rect(0, 0, 300, 300);
//        Rect r2 = new Rect(772, 0, 300, 300);
//        Rect r3 = new Rect(0, 772, 300, 300);
//        Rect r4 = new Rect(772, 772, 300, 300);
//
//
//        rect_array.add(r1);
//        rect_array.add(r2);
//        rect_array.add(r3);
//        rect_array.add(r4);
//    }


    @Override
    public void run() {


        //        final Mat maskCopyTo = Mat.zeros(InputFrame.size(), CvType.CV_8UC1); /// 创建copyTo方法的mask，大小与原图保持一致
        final Mat maskFloodFill = Mat.zeros(new Size(inputFrame.cols() + 2, inputFrame.rows() + 2), CvType.CV_8UC1);    //as required in documentation https://docs.opencv.org/2.4/modules/imgproc/dType.8UC1);


        if (rect_array.size() > 0) {   //if got more than 1 rect found in rect_array, draw them out!

            Iterator<Rect> it2 = rect_array.iterator();
            while (it2.hasNext()) {

//                Handler threadHandler = new Handler();
//
//                threadHandler.;
                Rect obj = it2.next();
                Imgproc.rectangle(inputFrame, obj.br(), obj.tl(), new Scalar(0, 255, 0), 4);
                Imgproc.floodFill(inputFrame, maskFloodFill, new Point((obj.tl().x + obj.br().x) / 2, (obj.tl().y + obj.br().y) / 2), new Scalar(0, 255, 0), new Rect(), new Scalar(120, 120, 120), new Scalar(120, 120, 120), 4);
                Log.d("floodfilling in process:","drawing boxes at corner");

                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

}
