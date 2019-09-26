package com.example.opencv_tutorial;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;


//here we not using our camera
public class grayscale extends AppCompatActivity {

    ImageView imageView;
    Bitmap grayBitmap, imageBitmap;   // pixel related resolutions for displaying captured image / video on Screen.

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grayscale);
        imageView = (ImageView)findViewById(R.id.imageView);
    }


    public void openGallery(View v){
        Intent myIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(myIntent,100);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100 && resultCode==RESULT_OK && data!= null){
            Uri imageUri = data.getData();    //setImageURI() sets the content of this ImageView to the specified Uri. This does Bitmap reading and decoding on the UI thread.  Consider using BitmapFactory.

            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);   //retrieves an image for the given url as a Bitmap/
            } catch (IOException e) {
                e.printStackTrace();
            }

            imageView.setImageBitmap(imageBitmap);    //Sets a Bitmap as the content os this ImageView.

        }
    }



    Mat rbgA, grayMat;
    //Size size;
    public void convertToGray(View v){
        Mat mat1= new Mat();
        //rbgA = new Mat(Size(imageView.getHeight(),imageView.getWidth()),CvType.CV_8UC4);   //set as our SOURCE Mat
        grayMat = new Mat(CvType.CV_8UC1);   //set as our DESTINATION Mat

        // C:/Users/User/AppData/Local/Android/Sdk/docs/reference/android/graphics/BitmapFactory.Options.html
        // Reason for using BitmapFactory: any mutable bitmap can be reused by BitmapFactory to decode any other bitmaps.
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inDither = false;    // Deprecated in API Level 24 & above.
        o.inSampleSize=4;    // If set to a value > 1, requests the decoder to subsample the original image, returning a smaller image to save memory. The sample size is the number of pixels in either dimension that correspond to a single pixel in the decoded bitmap. For example, inSampleSize == 4 returns an image that is 1/4 the width/height of the original, and 1/16 the number of pixels

        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();

        grayBitmap = Bitmap.createBitmap(width , height, Bitmap.Config.RGB_565);    //Each pixel is stored on 2 bytes (16bits cos 5+6+5) and only the RGB channels are encoded: red is stored with 5 bits of precision (32 possible values), green is stored with 6 bits of precision (64 possible values) and blue is stored with 5 bits of precision.  If nvr apply dithering, result might show a greenish tint. Therefore To get better results, dithering should be applied.


        //convert bitmap To Mat using utils.bitmapToMat() method.
        Utils.bitmapToMat(imageBitmap,rbgA);    //Map bitmap Src to Matrix dest.

        Imgproc.cvtColor(rbgA,grayMat,Imgproc.COLOR_RGB2GRAY);     //convert to Grayscale. Map  Mat src to Mat dest.

        //HERE, Once we obtain the Grayscale Img, IN FACT, WE CAN ACTL Furthur process THE INFO using THRESHOLD FUNCTIONS, See https://docs.opencv.org/3.4.0/d7/d4d/tutorial_py_thresholding.html  Boundless options/things that we can do w this Threshold Function!

        Utils.matToBitmap(grayMat,grayBitmap);   //Convert/Map back from Matrix to Bitmap, inorder to obtain pixel related configurations to inform screen how to display the image. (displays are pixel dependent, nd to be told the exact pixels, what to show what, which pixel correspond/responsible for which part of the image?

        imageView.setImageBitmap(grayBitmap);   //set the bitmap in accordance to the specifications of the converted Grayscale Bitmap image result that we now have to diplay the corresponding pixel results onto the screen.
        //whenever we nd to set/Produce an Image, we nd its Bitmap equivalent, for that, we convert its Matrix info ONTO a Bitmap using  MatToBitmap, but to do so, we 1st nd to initialize that Bitmap using BitMatFactory.
        //THEN, to set/create the image, we use BitMapFactory which is responsible for creation of ALL Bitmaps as well as managing their memory usages.
        //BitMapFactory handles Garbage Collection + Reuses Memory of Bitmaps w same sizes! So it acts/serves abit like a Manager class, managing all these Bitmap usages.

    }

}
