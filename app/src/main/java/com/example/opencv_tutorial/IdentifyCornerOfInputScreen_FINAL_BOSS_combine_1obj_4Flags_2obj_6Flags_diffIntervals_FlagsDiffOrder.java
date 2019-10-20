package com.example.opencv_tutorial;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.CycleInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.opencv_tutorial.R;
import com.example.opencv_tutorial.customSurfaceView;

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

import static android.hardware.Camera.Parameters.PREVIEW_FPS_MIN_INDEX;


//plot circle/squares @ the 4 edges of the screen!

public class IdentifyCornerOfInputScreen_FINAL_BOSS_combine_1obj_4Flags_2obj_6Flags_diffIntervals_FlagsDiffOrder extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    //JavaCameraView javaCameraView;
    //    CameraBridgeViewBase cameraBridgeViewBase;
    customSurfaceView cameraBridgeViewBase;    //cameraBridgeViewBase change to instance of customSurfaceView class to access both Camera mCamera object + CBase class!
    BaseLoaderCallback baseLoaderCallback;

    private static final int MY_CAMERA_REQUEST_CODE = 100;

    //    Timer t1 = new Timer();

    long secsLeft;

    /* 1 Object */
    boolean flag1 = false;  //the "switch" responsible for activation of certain section of code to run.
    boolean flag2 = false;  // + whether or not object to be displayed
    boolean flag3 = false;
    boolean flag4 = false;

    /* 2 Objects */
    boolean flag5 = false;  //flag1 - object 1 & 4   //the "switch" responsible for activation of certain section of code to run.
    boolean flag6 = false;  //flag2 - object 2 & 4   // + whether or not object to be displayed
    boolean flag7 = false;  //flag3 - object 3 & 1
    boolean flag8 = false;  //flag3 - object 3 & 4
    boolean flag9 = false;  //flag5 - object 1 & 2      //simply just have more configurations/combinations, but rect obj positions still the same!
    boolean flag10 = false; //flag6 - object 2 & 3      //aimply just have more configurations/combinations, but rect obj positions still the same!



    boolean clear1 = false;  //flag to keep track of lives
    boolean clear2 = false;
    boolean clear3 = false;
    boolean clear4 = false;
//    boolean flag5 = false;  //flag5 - object 1 & 2      //simply just have more configurations/combinations, but rect obj positions still the same!
//    boolean flag6 = false;  //flag6 - object 2 & 3      //simply just have more configurations/combinations, but rect obj positions still the same!


    CountDownTimer Timer1, Timer2, Timer3, Timer4, Timer5, Timer6, Timer7, Timer8, Timer9;


    Point p1 = new Point(0,0);    //centroid of object detected

    Point r1centroid = new Point(0,0);    //centroid of position of each object displayed on the screen
    Point r2centroid = new Point(0,0);
    Point r3centroid = new Point(0,0);
    Point r4centroid = new Point(0,0);

    double distance1 = 0.0 ,  distance2 = 0.0;   //var for the euclidean distance computation

    //    long startTime1, timeInterval1;
    //    long startTime2, timeInterval2;
    //    long startTime3, timeInterval3;
    //    long startTime4, timeInterval4;

    int rowWidth, colWidth;

    TextView LivesText;
    int Lives = 5;  //initialize 3 Lives @ start of game
    //maybe if want more complex, after each 4 rounds, if (Lives<3) {Lives+=1}
    TextView scoreText;
    int score=0;   //to display score in Alert Dialogue box for me to record.

//    int currentLevel=0;    //keep track of the highest level Player currently in before death,
                           // to display back to Player in the Alert Dialogue box upon Lives end
                           // just so to facilitate me to record it.
                           // ACTL IS THAT A GOOD THING? Cos from score I can alrdy determine which Level user is in.


    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diff_intervals);   //setting the layout view in accordance to the specifications of one of the layout resource file under re
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  //https://stackoverflow.com/questions/5712849/how-do-i-keep-the-screen-on-in-my-app
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        OpenCVLoader.initDebug();      //Performing a Debug check solely just for initialization purpose.

        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        } else {

            LivesText = (TextView) findViewById(R.id.textView3);    //update the score to this textbox
            LivesText.setText(String.valueOf(Lives));
            Log.d("Set the text for Lives","" + LivesText);
            scoreText = (TextView) findViewById(R.id.textView5);

            cameraBridgeViewBase = (customSurfaceView) findViewById(R.id.hsvCameraView);
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


            baseLoaderCallback = new BaseLoaderCallback(this) {     //"this" reference refers to the current class you are in. The view constructor requires a context and the only nearest context you have is this which is the current class.
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


    }    //enclosing bracket for OnCreate() method.




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
            Toast.makeText(getApplicationContext(), "There is a problem with OpenCv", Toast.LENGTH_SHORT).show();  //if unable to conect to OpenCv upon resuthrwo this error
        } else {
            //            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);   //else, connect invoke callback to connect back to CameraBridgeView.

            Log.d("Calling BaseLoaderCallback", "");
        }


        //ok now lives count down working, but when 0, nd to terminate/restart game/pop, show something back to user.

//        if (Lives!=0){

        //Logic for displaying 2 or more objects at once on the screen
        // For 2 or more, no nd use flag any more, str away define the location to draw rect_array.get(rand1|rand2|rand3...)
        //
        //    ArrayList<Rect> rect_array = new ArrayList<Rect>();
        //    Int rand1 = new Random().nextInt(6); - 6 cos 6 diff locations that rect object can appear. can have more but nd specify coordinates.
        //    Int rand2 = new Random().nextInt(6);
        //
        //    while(rand2 == rand1){
        //       rand2 = new Random().nextInt(6);
        //    }   //exits only when rand2 != rand1, to ensure that does not reference/call the same rect object index, want to display 2 diff objects
        //
        //    rect_array.get(rand1);   //display the 1st object
        //    rect_array.get(rand2);   //display the 2nd object





        //Test diff CountDown Intervals + the detected interval number to CATCH!
        // VARIOUS Tried & Tested Time Intervals!

//            Timer5 = new CountDownTimer(20000, 5000) {    //5 sec intervals
//                    if (secsLeft == 19) {    //if this works, the next step is to test on OnCameraFrame}
//                    if (secsLeft == 14) {    //if this works, the next step is to test on OnCameraFrame}
//                    if (secsLeft == 9) {    //if this works, the next step is to test on OnCameraFrame}
//                    if (secsLeft == 4) {    //if this works, the next step is to test on OnCameraFrame}
//                }

//            Timer4 = new CountDownTimer(16000, 4000) {    //4 sec intervals
//                    if (secsLeft == 15) {    //if this works, the next step is to test on OnCameraFrame}
//                    if (secsLeft == 11) {    //if this works, the next step is to test on OnCameraFrame}
//                    if (secsLeft == 7) {    //if this works, the next step is to test on OnCameraFrame}
//                    if (secsLeft == 3) {    //if this works, the next step is to test on OnCameraFrame}
//                }

//            Timer3 = new CountDownTimer(12000, 3000) {    //3 sec intervals
//                    if (secsLeft == 11) {    //if this works, the next step is to test on OnCameraFrame}
//                    if (secsLeft == 8) {    //if this works, the next step is to test on OnCameraFrame}
//                    if (secsLeft == 5) {    //if this works, the next step is to test on OnCameraFrame}
//                    if (secsLeft == 2) {    //if this works, the next step is to test on OnCameraFrame}
//                }


//            Timer2 = new CountDownTimer(8000, 2000) {    //2 sec intervals
//                    if (secsLeft == 7) {    //if this works, the next step is to test on OnCameraFrame}
//                    if (secsLeft == 5) {    //if this works, the next step is to test on OnCameraFrame}
//                    if (secsLeft == 3) {    //if this works, the next step is to test on OnCameraFrame}
//                    if (secsLeft == 1) {    //if this works, the next step is to test on OnCameraFrame}
//                }


//            Timer1 = new CountDownTimer(4000, 1000) {    //1 sec intervals
//                    if (secsLeft == 3) {    //if this works, the next step is to test on OnCameraFrame}
//                    if (secsLeft == 2) {    //if this works, the next step is to test on OnCameraFrame}
//                    if (secsLeft == 1) {    //if this works, the next step is to test on OnCameraFrame}
//                    if (secsLeft == 0) {    //if this works, the next step is to test on OnCameraFrame}
//                }


//                if (score < 20) {   //Here no nd condition checker alrdy, cos we not going faster than this.
//                                    //UNLESS, We going to include Timer for 2 objects Space aft this!
//                                    //Can mix and match, intermingle 1obj w 2obj.
//                                    //Eiither band by objects OR Time
//                                    //Maybe something like:
//                                    //Time banding:   1obj 4sec, 2obj 4sec  (4sec band) |  1obj 2sec, 2obj 2sec  (2sec band) | can't go any faster
//                                 //Or Object banding: 1obj 4sec, 1obj 2sec  (1obj band) |  2obj 4sec, 2obj 2sec  (2obj band) | 3 obj | 4 obj
//                                  //Shld customize,  1obj 4sec,  1 obj 3sec,  2obj 4sec,  1obj 2sec,  2obj 3sec,  2obj 2sec,  1obj 1sec,  2obj 1sec   //8 timers
//                                           //8 timers in total!
//                                  //But, how fast it progresses depends on SCORE! Which is a measure of user's hand-eye coordination ability & fitness!

//              //LEAVE IT/FK IT FOR NOW! OK, GO AHEAD TEST THIS 1 OBJECT THE "SWITCH BY SCORE" LOGIC!



        Timer9 = new CountDownTimer(4000, 1000) {
//        Timer1 = new CountDownTimer(24000, 4000) {

            public void onTick(long millisUntilFinished) {

                secsLeft = (millisUntilFinished / 1000);

                Log.d("seconds remaining: ", "" + secsLeft);  // =D works, only prints out @ every 4 secs interval !
                Log.d("", "Timer9");

//                Runtime.getRuntime().gc();    //call garbage collection @ every interval 4s/3s/2s/1s depending OnTick Interval of Timer, at least not on every frame, else will freeze/lag each frame = 0.04 sec, 1 sec will allow at least 20 frames to pass alrdy!
                //Here too fast alrdy, GC every secon cos OnTick interval is 1 sec, maybe can make it 2 sec / 4 sec in the if logic/Perhaps even in onFinish() method?!?

//                if (score < 10) {   //no nd to check anymore cos final stage, just let user keep playing until die.

                if (secsLeft == 3) {    //flag1==true   //object 1 & 4

                    if (clear1 == true) {
                        Lives -= 1;
                    }
                    if (clear3 == true) {
                        Lives -= 1;
                    }
                    LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                    if (Lives <= 0) {
//                        onStop();
                        onPause();
                    }
                    Log.d("flag 5", "whoopsie");   //:o finally showing :)
                    Log.d("f1ag 5", "object 1 & 4");
                    //EACH TIME, SET ALL FLAGS TO FALSE FIRST. Bef setting 1 to true.
                    flag5 = true; clear1 = true;   //flag1 - object 1 & 4   //Specifying rect coords directly here won't work, cos OnCameraFrame() method not even ready yet, so InputFrame won't be avail at this moment in time!
                    flag6 = false; clear2 = false;
                    flag7 = false; clear3 = false;
                    flag8 = false; clear4 = true;
                    flag9 = false;
                    flag10 = false;

                    Log.d("bool ", "clear1 " + clear1);
                    Log.d("bool ", "clear2 " + clear2);
                    Log.d("bool ", "clear3 " + clear3);
                    Log.d("bool ", "clear4 " + clear4);
                }


                if (secsLeft == 2) {    //flag2==true   //object 2 & 4

                    if (clear1 == true) {
                        Lives -= 1;
                    }
                    if (clear4 == true) {
                        Lives -= 1;
                    }
                    LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                    if (Lives <= 0) {
//                        onStop();
                        onPause();
                    }
                    Log.d("flag 10", "Holy Grenade");   //:o finally showing :)
                    Log.d("f1ag 10", "object 2 & 3");
                    flag5 = false; clear1 = false;
                    flag6 = false; clear2 = true;     //flag2 - object 2 & 4
                    flag7 = false; clear3 = true;
                    flag8 = false; clear4 = false;
                    flag9 = false;
                    flag10 = true;

                    Log.d("bool ", "clear1 " + clear1);
                    Log.d("bool ", "clear2 " + clear2);
                    Log.d("bool ", "clear3 " + clear3);
                    Log.d("bool ", "clear4 " + clear4);
                }


                if (secsLeft == 1) {    //flag3==true   //object 3 & 1

                    if (clear2 == true) {
                        Lives -= 1;
                    }
                    if (clear3 == true) {
                        Lives -= 1;
                    }
                    LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                    if (Lives <= 0) {
//                        onStop();
                        onPause();
                    }

                    Log.d("flag 6", "doopsey");   //:o finally showing :)
                    Log.d("f1ag 6", "object 2 & 4");
                    flag5 = false; clear1 = false;
                    flag6 = true; clear2 = true;
                    flag7 = false; clear3 = false;   //flag3 - object 3 & 1
                    flag8 = false; clear4 = true;
                    flag9 = false;
                    flag10 = false;

                    Log.d("bool ", "clear1 " + clear1);
                    Log.d("bool ", "clear2 " + clear2);
                    Log.d("bool ", "clear3 " + clear3);
                    Log.d("bool ", "clear4 " + clear4);
                }


                if (secsLeft == 0) {    //flag4==true   //object 3 & 4

                    if (clear2 == true) {
                        Lives -= 1;
                    }
                    if (clear4 == true) {
                        Lives -= 1;
                    }
                    LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                    if (Lives <= 0) {
//                        onStop();
                        onPause();
                    }

                    Log.d("flag 7", "naonia");   //:o finally showing :)
                    Log.d("f1ag 7", "object 3 & 1");
                    flag5 = false; clear1 = true;
                    flag6 = false; clear2 = false;
                    flag7 = true; clear3 = true;
                    flag8 = false; clear4 = false;    //flag4   //object 3 & 4
                    flag9 = false;
                    flag10 = false;

                    Log.d("bool ", "clear1 " + clear1);
                    Log.d("bool ", "clear2 " + clear2);
                    Log.d("bool ", "clear3 " + clear3);
                    Log.d("bool ", "clear4 " + clear4);
                }

//                }else{onFinish();}

            }  //enclosing bracket for OnTick()


            public void onFinish() {
                Log.d("Elapsed Time since bootup bef sleep: ", "" + SystemClock.elapsedRealtime());
                SystemClock.sleep(50);
                Log.d("SystemClock.Sleep", "50ms");   // =D  Rly Worked, added a delay of 5s, puase the Whole Damn Fucking thing.  Rly slept for 5s, to "Recharge"
                //NOT IDEAL TO PUT SLEEP ON MAIN THREAD, COS IT STOPS THE ENTIRE MAIN THREAD, FRAME FREEZES!  Hang Type of feel.
                Log.d("Elapsed Time since bootup aft sleep: ", "" + SystemClock.elapsedRealtime());

                Runtime.getRuntime().gc();   //so that every 4 sec then invoke 1 call to GC, smoothen the process, not so lag, let nativeObjs graudually build up naturally over 4 secs.

                start();    //Let it Whip/Rip.  Let it loop on & on forever to  Whoop any  ZAI GOD PLAYER's  A S S   till they Ran out of Lives (a.k.a DIE)
                //FINAL TIMER no nd to cancel, keep going on until user Dies when Lives zero, OnPause() method will call the cancel for this Timer9.

                //Final level shall be set as the hardest level, w the shortest time interval.
                // Lives=0 to end the game, maybe can set 1 super SUK KI KILAT  sibei killer one, just to make sure end the game.
                // Ideally if can, go on to 3 objs, cos sure got ppl CAN MAKE IT ONE!  But i no time alrdy, lack Development Time.  Can is can, more Timers & more Flags, plus prolly nd explore more Rect obj coordinates alrdy!

                // For those find it easy, Make it to master Level ONE, AHH..... 3 ways:
                // 1. Ask USER/PLAYER  STAND FURTHUR AWAY!  AH!!!  =D  Dist furthur, Obj detected becomes smaller, PLAYER ND TO STRETCH ARMS OUT WIDER/FURTHUR!
                // 2. reduce the threshold for detection in MatchObject() method reduce 120->60, so now have to be more precise, harder to hit sweet spot when reach Interval of 1sec for 2 objects.
                // 3. Reduce num of Lives @/from the start.
            }

        };


        Timer8 = new CountDownTimer(8000, 2000) {
//        Timer1 = new CountDownTimer(24000, 4000) {

            public void onTick(long millisUntilFinished) {

                secsLeft = (millisUntilFinished / 1000);

                Log.d("seconds remaining: ", "" + secsLeft);  // =D works, only prints out @ every 4 secs interval !
                Log.d("", "Timer8");

//                Runtime.getRuntime().gc();    //call garbage collection @ every interval 4s/3s/2s/1s depending OnTick Interval of Timer, at least not on every frame, else will freeze/lag each frame = 0.04 sec, 1 sec will allow at least 20 frames to pass alrdy!
                //Here too fast alrdy, got 2 flags to set, but clear every 2 sec, maybe can alternate, like every 4 sec, specify gc under If statement!

                if (score < 78) {

                    if (secsLeft == 7) {    //flag1==true   //object 1 & 4

                        if (clear1 == true) {
                            Lives -= 1;
                        }
                        if (clear4 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }
                        Log.d("flag 9", "flabagast");   //:o finally showing :)
                        Log.d("f1ag 9", "object 1 & 2");
                        //EACH TIME, SET ALL FLAGS TO FALSE FIRST. Bef setting 1 to true.
                        flag5 = false; clear1 = true;   //flag1 - object 1 & 4   //Specifying rect coords directly here won't work, cos OnCameraFrame() method not even ready yet, so InputFrame won't be avail at this moment in time!
                        flag6 = false; clear2 = true;
                        flag7 = false; clear3 = false;
                        flag8 = false; clear4 = false;
                        flag9 = true;
                        flag10 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }


                    if (secsLeft == 5) {    //flag2==true   //object 2 & 4

                        if (clear1 == true) {
                            Lives -= 1;
                        }
                        if (clear2 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }
                        Log.d("flag 10", "Holy Grenade");   //:o finally showing :)
                        Log.d("f1ag 10", "object 2 & 3");
                        flag5 = false; clear1 = false;
                        flag6 = false; clear2 = true;     //flag2 - object 2 & 4
                        flag7 = false; clear3 = true;
                        flag8 = false; clear4 = false;
                        flag9 = false;
                        flag10 = true;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }


                    if (secsLeft == 3) {    //flag3==true   //object 3 & 1
                        Runtime.getRuntime().gc();

                        if (clear2 == true) {
                            Lives -= 1;
                        }
                        if (clear3 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }

                        Log.d("flag 8", "Gobsmack");   //:o finally showing :)
                        Log.d("f1ag 8", "object 3 & 4");
                        flag5 = false; clear1 = false;
                        flag6 = false; clear2 = false;
                        flag7 = false; clear3 = true;   //flag3 - object 3 & 1
                        flag8 = true; clear4 = true;
                        flag9 = false;
                        flag10 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }


                    if (secsLeft == 1) {    //flag4==true   //object 3 & 4

                        if (clear3 == true) {
                            Lives -= 1;
                        }
                        if (clear4 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }

                        Log.d("flag 5", "Whoopsie");   //:o finally showing :)
                        Log.d("f1ag 5", "object 1 & 4");
                        flag5 = true; clear1 = true;
                        flag6 = false; clear2 = false;
                        flag7 = false; clear3 = false;
                        flag8 = false; clear4 = true;    //flag4   //object 3 & 4
                        flag9 = false;
                        flag10 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                }else{onFinish();}

            }  //enclosing bracket for OnTick()


            public void onFinish() {
                Log.d("Elapsed Time since bootup bef sleep: ","" + SystemClock.elapsedRealtime());
                SystemClock.sleep(50);
                Log.d("SystemClock.Sleep","50ms");   // =D  Rly Worked, added a delay of 5s, puase the Whole Damn Fucking thing.  Rly slept for 5s, to "Rechar
                //NOT IDEAL TO PUT SLEEP ON MAIN THREAD, COS IT STOPS THE ENTIRE MAIN THREAD, FRAME FREEZES!  Hang Type of feel.
                Log.d("Elapsed Time since bootup aft sleep: ","" + SystemClock.elapsedRealtime());

                if (score<78){
                    Runtime.getRuntime().gc();
                    start();
                }else {
                    cancel();    // :D :D :D YASSS!  IT FREAKING WORKED!  Loop on forever . HAHAHAHHAA! YAY! JOY TO THE WORLD!
                    Runtime.getRuntime().gc();
                    Timer9.start();
                }
            }  //enclosing bracket for onFinish()

        };


//        final CountDownTimer Timer4 = new CountDownTimer(8000, 2000) {
        Timer4 = new CountDownTimer(4000, 1000) {    //ORDER OF APPEARANCE:  2, 4, 3, 1

            public void onTick(long millisUntilFinished) {

                secsLeft = (millisUntilFinished / 1000);

                Log.d("seconds remaining: ", "" + secsLeft);  // =D works, only prints out @ every 4 secs interval !
                Log.d("", "Timer4");

//                Runtime.getRuntime().gc();    //call garbage collection @ every interval 4s/3s/2s/1s depending OnTick Interval of Timer, at least not on every frame, else will freeze/lag each frame = 0.04 sec, 1 sec will allow at least 20 frames to pass alrdy!
                //Interval too fast 1 sec, not enuff time to let NativeObj build up naturally graudually resulting in freeze/lagged frames, Try move to onFinish() instead where Interval aft 4 sec then we force a GC!

                if (score < 66) {

                    if (secsLeft == 3) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear1 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
//                            onStop();
                                onPause();
                            }
                        }

                        Log.d("flag 1", "yay");   //:o finally showing :)
                        flag1 = false;
                        clear1 = false;
                        flag2 = true;
                        clear2 = true;
                        flag3 = false;
                        clear3 = false;
                        flag4 = false;
                        clear4 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                    if (secsLeft == 2) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear2 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
//                            onStop();
                                onPause();
                            }
                        }

                        Log.d("flag 2", "HOHO");   //:o finally showing :)
                        flag1 = false;
                        clear1 = false;
                        flag2 = false;
                        clear2 = false;
                        flag3 = false;
                        clear3 = false;
                        flag4 = true;
                        clear4 = true;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                    if (secsLeft == 1) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear4 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
//                            onStop();
                                onPause();
                            }
                        }

                        Log.d("flag 3", "heehee");   //:o finally showing :)
                        flag1 = false;
                        clear1 = false;
                        flag2 = false;
                        clear2 = false;
                        flag3 = true;
                        clear3 = true;
                        flag4 = false;
                        clear4 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                    if (secsLeft == 0) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear3 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
                                //                        onStop();
                                onPause();
                            }
                        }

                        Log.d("flag 4", "gaga");   //:o finally showing :)
                        flag1 = true;
                        clear1 = true;
                        flag2 = false;
                        clear2 = false;
                        flag3 = false;
                        clear3 = false;
                        flag4 = false;
                        clear4 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }
                }else{onFinish();}

            }    //enclosing block for onTick()


            public void onFinish() {


                Log.d("Elapsed Time since bootup bef sleep: ", "" + SystemClock.elapsedRealtime());
                SystemClock.sleep(50);
                Log.d("SystemClock.Sleep", "50ms");   // =D  Rly Worked, added a delay of 5s, puase the Whole Damn Fucking thing.  Rly slept for 5s, to "Rechar
                //NOT IDEAL TO PUT SLEEP ON MAIN THREAD, COS IT STOPS THE ENTIRE MAIN THREAD, FRAME FREEZES!  Hang Type of feel.
                Log.d("Elapsed Time since bootup aft sleep: ", "" + SystemClock.elapsedRealtime());

                if (score<66){
                    Runtime.getRuntime().gc();
                    start();
                }else {
                    cancel();    // :D :D :D YASSS!  IT FREAKING WORKED!  Loop on forever . HAHAHAHHAA! YAY! JOY TO THE WORLD!
                    Runtime.getRuntime().gc();
                    Timer8.start();
                }
            }   //enclosing block for onFinish()
        };



        Timer7 = new CountDownTimer(12000, 3000) {
//        Timer1 = new CountDownTimer(24000, 4000) {

            public void onTick(long millisUntilFinished) {

                secsLeft = (millisUntilFinished / 1000);

                Log.d("seconds remaining: ", "" + secsLeft);  // =D works, only prints out @ every 4 secs interval !
                Log.d("", "Timer7");

//                Runtime.getRuntime().gc();    //call garbage collection @ every interval 4s/3s/2s/1s depending OnTick Interval of Timer, at least not on every frame, else will freeze/lag each frame = 0.04 sec, 1 sec will allow at least 20 frames to pass alrdy!


                if (score < 58) {

                    if (secsLeft == 11) {    //flag1==true   //object 1 & 4
                        Runtime.getRuntime().gc();

                        if (clear1 == true) {
                            Lives -= 1;
                        }
                        if (clear2 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }
                        Log.d("flag 8", "Gobsmack");   //:o finally showing :)
                        Log.d("f1ag 8", "object 3 & 4");
                        //EACH TIME, SET ALL FLAGS TO FALSE FIRST. Bef setting 1 to true.
                        flag5 = false; clear1 = false;   //flag1 - object 1 & 4   //Specifying rect coords directly here won't work, cos OnCameraFrame() method not even ready yet, so InputFrame won't be avail at this moment in time!
                        flag6 = false; clear2 = false;
                        flag7 = false; clear3 = true;
                        flag8 = true; clear4 = true;
                        flag9 = false;
                        flag10 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }


                    if (secsLeft == 8) {    //flag2==true   //object 2 & 4

                        if (clear3 == true) {
                            Lives -= 1;
                        }
                        if (clear4 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }
                        Log.d("flag 7", "naonia");   //:o finally showing :)
                        Log.d("f1ag 7", "object 3 & 1");
                        flag5 = false; clear1 = true;
                        flag6 = false; clear2 = false;     //flag2 - object 2 & 4
                        flag7 = true; clear3 = true;
                        flag8 = false; clear4 = false;
                        flag9 = false;
                        flag10 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }


                    if (secsLeft == 5) {    //flag3==true   //object 3 & 1
                        Runtime.getRuntime().gc();

                        if (clear1 == true) {
                            Lives -= 1;
                        }
                        if (clear3 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }

                        Log.d("flag 10", "Holy Grenade");   //:o finally showing :)
                        Log.d("f1ag 10", "object 2 & 3");
                        flag5 = false; clear1 = false;
                        flag6 = false; clear2 = true;
                        flag7 = false; clear3 = true;   //flag3 - object 3 & 1
                        flag8 = false; clear4 = false;
                        flag9 = false;
                        flag10 = true;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }


                    if (secsLeft == 2) {    //flag4==true   //object 3 & 4

                        if (clear2 == true) {
                            Lives -= 1;
                        }
                        if (clear3 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }

                        Log.d("flag 9", "flabagast");   //:o finally showing :)
                        Log.d("f1ag 9", "object 1 & 2");
                        flag5 = false; clear1 = true;
                        flag6 = false; clear2 = true;
                        flag7 = false; clear3 = false;
                        flag8 = false; clear4 = false;    //flag4   //object 3 & 4
                        flag9 = true;
                        flag10 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                }else{onFinish();}

            }  //enclosing bracket for OnTick()


            public void onFinish() {
                Log.d("Elapsed Time since bootup bef sleep: ","" + SystemClock.elapsedRealtime());
                SystemClock.sleep(50);
                Log.d("SystemClock.Sleep","50ms");   // =D  Rly Worked, added a delay of 5s, puase the Whole Damn Fucking thing.  Rly slept for 5s, to "Rechar
                //NOT IDEAL TO PUT SLEEP ON MAIN THREAD, COS IT STOPS THE ENTIRE MAIN THREAD, FRAME FREEZES!  Hang Type of feel.
                Log.d("Elapsed Time since bootup aft sleep: ","" + SystemClock.elapsedRealtime());

                if (score<58){
                    start();
                }else {
                    cancel();    // :D :D :D YASSS!  IT FREAKING WORKED!  Loop on forever . HAHAHAHHAA! YAY! JOY TO THE WORLD!
                    Runtime.getRuntime().gc();
                    Timer4.start();
                }
            }  //enclosing bracket for onFinish()

        };



        Timer6 = new CountDownTimer(16000, 4000) {
//        Timer1 = new CountDownTimer(24000, 4000) {

            public void onTick(long millisUntilFinished) {

                secsLeft = (millisUntilFinished / 1000);

                Log.d("seconds remaining: ", "" + secsLeft);  // =D works, only prints out @ every 4 secs interval !
                Log.d("", "Timer6");

                Runtime.getRuntime().gc();    //call garbage collection @ every interval 4s/3s/2s/1s depending OnTick Interval of Timer, at least not on every frame, else will freeze/lag each frame = 0.04 sec, 1 sec will allow at least 20 frames to pass alrdy!


                if (score < 44) {

                    if (secsLeft == 15) {    //flag1==true   //object 1 & 4

                        if (clear1 == true) {
                            Lives -= 1;
                        }
                        if (clear2 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }
                        Log.d("flag 6", "doopsey");   //:o finally showing :)
                        Log.d("f1ag 6", "object 2 & 4");
                        //EACH TIME, SET ALL FLAGS TO FALSE FIRST. Bef setting 1 to true.
                        flag5 = false; clear1 = false;   //flag1 - object 1 & 4   //Specifying rect coords directly here won't work, cos OnCameraFrame() method not even ready yet, so InputFrame won't be avail at this moment in time!
                        flag6 = true; clear2 = true;
                        flag7 = false; clear3 = false;
                        flag8 = false; clear4 = true;
                        flag9 = false;
                        flag10 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }


                    if (secsLeft == 11) {    //flag2==true   //object 2 & 4

                        if (clear2 == true) {
                            Lives -= 1;
                        }
                        if (clear4 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }
                        Log.d("flag 7", "naonia");   //:o finally showing :)
                        Log.d("f1ag 7", "object 3 & 1");
                        flag5 = false; clear1 = true;
                        flag6 = false; clear2 = false;     //flag2 - object 2 & 4
                        flag7 = true; clear3 = true;
                        flag8 = false; clear4 = false;
                        flag9 = false;
                        flag10 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }


                    if (secsLeft == 7) {    //flag3==true   //object 3 & 1

                        if (clear1 == true) {
                            Lives -= 1;
                        }
                        if (clear3 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }

                        Log.d("flag 8", "Gobsmack");   //:o finally showing :)
                        Log.d("f1ag 8", "object 3 & 4");
                        flag5 = false; clear1 = false;
                        flag6 = false; clear2 = false;
                        flag7 = false; clear3 = true;   //flag3 - object 3 & 1
                        flag8 = true; clear4 = true;
                        flag9 = false;
                        flag10 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }


                    if (secsLeft == 3) {    //flag4==true   //object 3 & 4

                        if (clear3 == true) {
                            Lives -= 1;
                        }
                        if (clear4 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }

                        Log.d("flag 9", "flabagast");   //:o finally showing :)
                        Log.d("f1ag 9", "object 1 & 2");
                        flag5 = false; clear1 = true;
                        flag6 = false; clear2 = true;
                        flag7 = false; clear3 = false;
                        flag8 = false; clear4 = false;    //flag4   //object 3 & 4
                        flag9 = true;
                        flag10 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                }else{onFinish();}

            }  //enclosing bracket for OnTick()


            public void onFinish() {
                Log.d("Elapsed Time since bootup bef sleep: ","" + SystemClock.elapsedRealtime());
                SystemClock.sleep(50);
                Log.d("SystemClock.Sleep","50ms");   // =D  Rly Worked, added a delay of 5s, puase the Whole Damn Fucking thing.  Rly slept for 5s, to "Rechar
                //NOT IDEAL TO PUT SLEEP ON MAIN THREAD, COS IT STOPS THE ENTIRE MAIN THREAD, FRAME FREEZES!  Hang Type of feel.
                Log.d("Elapsed Time since bootup aft sleep: ","" + SystemClock.elapsedRealtime());

                if (score<44){
                    start();
                }else {
                    cancel();    // :D :D :D YASSS!  IT FREAKING WORKED!  Loop on forever . HAHAHAHHAA! YAY! JOY TO THE WORLD!
                    Timer7.start();

                }
            }  //enclosing bracket for onFinish()

        };


//        final CountDownTimer Timer3 = new CountDownTimer(20000, 5000) {
        Timer3 = new CountDownTimer(8000, 2000) {    //ORDER OF APPEARANCE:  1, 2, 3, 4

            public void onTick(long millisUntilFinished) {

                secsLeft = (millisUntilFinished / 1000);

                Log.d("seconds remaining: ", "" + secsLeft);  // =D works, only prints out @ every 4 secs interval !
                Log.d("", "Timer3");

                Runtime.getRuntime().gc();    //call garbage collection @ every interval 4s/3s/2s/1s depending OnTick Interval of Timer, at least not on every frame, else will freeze/lag each frame = 0.04 sec, 1 sec will allow at least 20 frames to pass alrdy!


                if (score < 34) {   //same var score as the one used globally to keep track of all the scores!

                    if (secsLeft == 7) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear4 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
//                            onStop();
                                onPause();
                            }
                        }
                        //set flag 4, DO SOMETHING ELSE.
                        Log.d("flag 1", "yay");   //:o finally showing :)
                        flag1 = true; clear1 = true;
                        flag2 = false; clear2 = false;
                        flag3 = false; clear3 = false;
                        flag4 = false; clear4 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                    if (secsLeft == 5) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear1 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
                                //                        onStop();
                                onPause();
                            }
                        }
                        //set flag 4, DO SOMETHING ELSE.
                        Log.d("flag 2", "HOHO");   //:o finally showing :)
                        flag1 = false; clear1 = false;
                        flag2 = true; clear2 = true;
                        flag3 = false; clear3 = false;
                        flag4 = false; clear4 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                    if (secsLeft == 3) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear2 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
//                            onStop();
                                onPause();
                            }
                        }
                        //set flag 4, DO SOMETHING ELSE.
                        Log.d("flag 3", "heehee");   //:o finally showing :)
                        flag1 = false; clear1 = false;
                        flag2 = false; clear2 = false;
                        flag3 = true; clear3 = true;
                        flag4 = false; clear4 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                    if (secsLeft == 1) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear3 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
//                            onStop();
                                onPause();
                            }
                        }
                        //set flag 4, DO SOMETHING ELSE.
                        Log.d("flag 4", "gaga");   //:o finally showing :)
                        flag1 = false; clear1 = false;
                        flag2 = false; clear2 = false;
                        flag3 = false; clear3 = false;
                        flag4 = true; clear4 = true;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                }else{onFinish();}

            }

            public void onFinish() {
                Log.d("Elapsed Time since bootup bef sleep: ", "" + SystemClock.elapsedRealtime());
                SystemClock.sleep(50);
                Log.d("SystemClock.Sleep", "50ms");   // =D  Rly Worked, added a delay of 5s, puase the Whole Damn Fucking thing.  Rly slept for 5s, to "Rechar
                //NOT IDEAL TO PUT SLEEP ON MAIN THREAD, COS IT STOPS THE ENTIRE MAIN THREAD, FRAME FREEZES!  Hang Type of feel.
                Log.d("Elapsed Time since bootup aft sleep: ", "" + SystemClock.elapsedRealtime());

//                start();    // :D :D :D YASSS!  IT FREAKING WORKED!  Loop on forever . HAHAHAHHAA! YAY! JOY TO THE WORLD!
                if (score<34){
                    start();   //loop/restart current Timer
                }
                else {
                    cancel();  //cancel current Timer
                    Timer6.start();   //start the New Timer, Timer2, with a faster Interval!


                    //CHEY, Apparently must put Timer2 above, in roder to INITIALIZE IT befroe Timer1, else code will think Timer2 not initialize yet. \CHEY, small but IMPT Concept! Same goes for all other variables! Rmbr to intialize like this! If nd to call a var within a method body, ND to place it ahead/bef omethod body in order to INITIALIZE IT!
                    //\**Seemingly small but IMPT Concept!  Same goes for all other variables! Rmbr to intialize like this!
                    //\**Whenever nd to call a var within a method body, ND to place it ahead/bef omethod body in order to INITIALIZE IT!
                    //start the second Timer @ 2 seconds Interval   //works, BUT, code ran both timer tgt too in the first instance, so nd a boolean Flag to control Logic instead
                }

            }
        };



        //Test diff CountDown Intervals + the detected interval number to CATCH!
        Timer5 = new CountDownTimer(20000, 5000) {
//        Timer1 = new CountDownTimer(24000, 4000) {

            public void onTick(long millisUntilFinished) {

                secsLeft = (millisUntilFinished / 1000);

                Log.d("seconds remaining: ", "" + secsLeft);  // =D works, only prints out @ every 4 secs interval !
                Log.d("", "Timer5");

                Runtime.getRuntime().gc();    //call garbage collection @ every interval 4s/3s/2s/1s depending OnTick Interval of Timer, at least not on every frame, else will freeze/lag each frame = 0.04 sec, 1 sec will allow at least 20 frames to pass alrdy!


                if (score < 26) {

                    if (secsLeft == 19) {    //flag1==true   //object 1 & 4

                        if (clear3 == true) {
                            Lives -= 1;
                        }
                        if (clear4 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }
                        Log.d("flag 5", "whoopsie");   //:o finally showing :)
                        Log.d("f1ag 5", "object 1 & 4");
                        //EACH TIME, SET ALL FLAGS TO FALSE FIRST. Bef setting 1 to true.
                        flag5 = true; clear1 = true;   //flag1 - object 1 & 4   //Specifying rect coords directly here won't work, cos OnCameraFrame() method not even ready yet, so InputFrame won't be avail at this moment in time!
                        flag6 = false; clear2 = false;
                        flag7 = false; clear3 = false;
                        flag8 = false; clear4 = true;
                        flag9 = false;
                        flag10 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }


                    if (secsLeft == 14) {    //flag2==true   //object 2 & 4

                        if (clear1 == true) {
                            Lives -= 1;
                        }
                        if (clear4 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }
                        Log.d("flag 6", "doopsey");   //:o finally showing :)
                        Log.d("f1ag 6", "object 2 & 4");
                        flag5 = false; clear1 = false;
                        flag6 = true; clear2 = true;     //flag2 - object 2 & 4
                        flag7 = false; clear3 = false;
                        flag8 = false; clear4 = true;
                        flag9 = false;
                        flag10 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }


                    if (secsLeft == 9) {    //flag3==true   //object 3 & 1

                        if (clear2 == true) {
                            Lives -= 1;
                        }
                        if (clear4 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }

                        Log.d("flag 7", "naonia");   //:o finally showing :)
                        Log.d("f1ag 7", "object 3 & 1");
                        flag5 = false; clear1 = true;
                        flag6 = false; clear2 = false;
                        flag7 = true; clear3 = true;   //flag3 - object 3 & 1
                        flag8 = false; clear4 = false;
                        flag9 = false;
                        flag10 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }


                    if (secsLeft == 4) {    //flag4==true   //object 3 & 4

                        if (clear1 == true) {
                            Lives -= 1;
                        }
                        if (clear3 == true) {
                            Lives -= 1;
                        }
                        LivesText.setText(String.valueOf(Lives));    //Display remaining Lives back to user on UI screen!

                        if (Lives <= 0) {
//                        onStop();
                            onPause();
                        }

                        Log.d("flag 8", "Gobsmack");   //:o finally showing :)
                        Log.d("f1ag 8", "object 3 & 4");
                        flag5 = false; clear1 = false;
                        flag6 = false; clear2 = false;
                        flag7 = false; clear3 = true;
                        flag8 = true; clear4 = true;    //flag4   //object 3 & 4
                        flag9 = false;
                        flag10 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                }else{onFinish();}

            }  //enclosing bracket for OnTick()


            public void onFinish() {
                Log.d("Elapsed Time since bootup bef sleep: ","" + SystemClock.elapsedRealtime());
                SystemClock.sleep(50);
                Log.d("SystemClock.Sleep","50ms");   // =D  Rly Worked, added a delay of 5s, puase the Whole Damn Fucking thing.  Rly slept for 5s, to "Rechar
                //NOT IDEAL TO PUT SLEEP ON MAIN THREAD, COS IT STOPS THE ENTIRE MAIN THREAD, FRAME FREEZES!  Hang Type of feel.
                Log.d("Elapsed Time since bootup aft sleep: ","" + SystemClock.elapsedRealtime());

                if (score<26){
                    start();
                }else {
                    cancel();    // :D :D :D YASSS!  IT FREAKING WORKED!  Loop on forever . HAHAHAHHAA! YAY! JOY TO THE WORLD!
                    Timer3.start();

                }
            }  //enclosing bracket for onFinish()

        };



//        final CountDownTimer Timer2 = new CountDownTimer(8000, 2000) {
        Timer2 = new CountDownTimer(12000, 3000) {

            public void onTick(long millisUntilFinished) {    //ORDER OF APPEARANCE:  3, 1, 4, 2

                secsLeft = (millisUntilFinished / 1000);

                Log.d("seconds remaining: ", "" + secsLeft);  // =D works, only prints out @ every 4 secs interval !
                Log.d("", "Timer2");
//                for (int i=0; i<secsLeft; i++){
//                    int mynumber = new Random().nextInt(100);
//                    e3.setText(String.valueOf(mynumber));
//                }


                Runtime.getRuntime().gc();    //call garbage collection @ every interval 4s/3s/2s/1s depending OnTick Interval of Timer, at least not on every frame, else will freeze/lag each frame = 0.04 sec, 1 sec will allow at least 20 frames to pass alrdy!


                if (score < 16) {   //same var score as the one used globally to keep track of all the scores!

                    if (secsLeft == 11) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear2 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
//                            onStop();
                                onPause();
                            }
                        }
                        //set flag 4, DO SOMETHING ELSE.
                        Log.d("flag 1", "yay");   //:o finally showing :)
                        flag1 = false; clear1 = false;
                        flag2 = false; clear2 = false;
                        flag3 = true; clear3 = true;
                        flag4 = false; clear4 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                    if (secsLeft == 8) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear3 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
//                            onStop();
                                onPause();
                            }
                        }
                        //set flag 4, DO SOMETHING ELSE.
                        Log.d("flag 2", "HOHO");   //:o finally showing :)
                        flag1 = true; clear1 = true;
                        flag2 = false; clear2 = false;
                        flag3 = false; clear3 = false;
                        flag4 = false; clear4 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                    if (secsLeft == 5) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear1 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
//                            onStop();
                                onPause();
                            }
                        }
                        //set flag 4, DO SOMETHING ELSE.
                        Log.d("flag 3", "heehee");   //:o finally showing :)
                        flag1 = false; clear1 = false;
                        flag2 = false; clear2 = false;
                        flag3 = false; clear3 = false;
                        flag4 = true; clear4 = true;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                    if (secsLeft == 2) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear4 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
//                            onStop();
                                onPause();
                            }
                        }
                        //set flag 4, DO SOMETHING ELSE.
                        Log.d("flag 4", "gaga");   //:o finally showing :)
                        flag1 = false; clear1 = false;
                        flag2 = true; clear2 = true;
                        flag3 = false; clear3 = false;
                        flag4 = false; clear4 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                }else{onFinish();}

            }

            public void onFinish() {
                Log.d("Elapsed Time since bootup bef sleep: ", "" + SystemClock.elapsedRealtime());
                SystemClock.sleep(50);
                Log.d("SystemClock.Sleep", "50ms");   // =D  Rly Worked, added a delay of 5s, puase the Whole Damn Fucking thing.  Rly slept for 5s, to "Rechar
                //NOT IDEAL TO PUT SLEEP ON MAIN THREAD, COS IT STOPS THE ENTIRE MAIN THREAD, FRAME FREEZES!  Hang Type of feel.
                Log.d("Elapsed Time since bootup aft sleep: ", "" + SystemClock.elapsedRealtime());

//                start();    // :D :D :D YASSS!  IT FREAKING WORKED!  Loop on forever . HAHAHAHHAA! YAY! JOY TO THE WORLD!

                if (score<16){
                    start();   //loop/restart current Timer
                }
                else {
                    cancel();  //cancel current Timer
                    Timer5.start();   //start the New Timer, Timer2, with a faster Interval!


                    //CHEY, Apparently must put Timer2 above, in roder to INITIALIZE IT befroe Timer1, else code will think Timer2 not initialize yet. \CHEY, small but IMPT Concept! Same goes for all other variables! Rmbr to intialize like this! If nd to call a var within a method body, ND to place it ahead/bef omethod body in order to INITIALIZE IT!
                    //\**Seemingly small but IMPT Concept!  Same goes for all other variables! Rmbr to intialize like this!
                    //\**Whenever nd to call a var within a method body, ND to place it ahead/bef omethod body in order to INITIALIZE IT!
                    //start the second Timer @ 2 seconds Interval   //works, BUT, code ran both timer tgt too in the first instance, so nd a boolean Flag to control Logic instead
                }

            }
        };



//        final CountDownTimer Timer1 = new CountDownTimer(16000, 4000) {    //Cannot call final, or else cannot call Timer from inside onPause() method of APP!
        Timer1 = new CountDownTimer(16000, 4000) {    //ORDER OF APPEARANCE:  1, 4, 3, 2

            public void onTick(long millisUntilFinished) {

                secsLeft = (millisUntilFinished / 1000);

                Log.d("seconds remaining: ", "" + secsLeft);  // =D works, only prints out @ every 4 secs interval !
                Log.d("", "Timer1");


                Runtime.getRuntime().gc();    //call garbage collection @ every interval 4s/3s/2s/1s depending OnTick Interval of Timer, at least not on every frame, else will freeze/lag each frame = 0.04 sec, 1 sec will allow at least 20 frames to pass alrdy!


                if (score < 8) {   //same var score as the one used globally to keep track of all the scores!

                    if (secsLeft == 15) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear2 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
//                            onStop();
                                onPause();
                            }
                        }

                        //set flag 4, DO SOMETHING ELSE.
                        Log.d("flag 1", "yay");   //:o finally showing :)
                        flag1 = true; clear1 = true;
                        flag2 = false; clear2 = false;
                        flag3 = false; clear3 = false;
                        flag4 = false; clear4 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }


                    if (secsLeft == 11) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear1 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
//                            onStop();
                                onPause();
                            }
                        }
                        //set flag 4, DO SOMETHING ELSE.
                        Log.d("flag 2", "HOHO");   //:o finally showing :)
                        flag1 = false; clear1 = false;
                        flag2 = false; clear2 = false;
                        flag3 = false; clear3 = false;
                        flag4 = true; clear4 = true;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                    if (secsLeft == 7) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear4 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
//                            onStop();
                                onPause();
                            }
                        }
                        //set flag 4, DO SOMETHING ELSE.
                        Log.d("flag 3", "heehee");   //:o finally showing :)
                        flag1 = false; clear1 = false;
                        flag2 = false; clear2 = false;
                        flag3 = true; clear3 = true;
                        flag4 = false; clear4 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }

                    if (secsLeft == 3) {    //if this works, the next step is to test on OnCameraFrame
                        if (clear3 == true) {
                            Lives -= 1;
                            LivesText.setText(String.valueOf(Lives));   //NOT WORKING, EVERYTHING IN ONCREATE only creates the first time, but aft that does not
                            if (Lives <= 0) {
//                            onStop();
                                onPause();
                            }
                        }
                        //set flag 4, DO SOMETHING ELSE.
                        Log.d("flag 4", "gaga");   //:o finally showing :)
                        flag1 = false; clear1 = false;
                        flag2 = true; clear2 = true;
                        flag3 = false; clear3 = false;
                        flag4 = false; clear4 = false;

                        Log.d("bool ", "clear1 " + clear1);
                        Log.d("bool ", "clear2 " + clear2);
                        Log.d("bool ", "clear3 " + clear3);
                        Log.d("bool ", "clear4 " + clear4);
                    }


                    //   =D =D =D FLAG FUCKING WORKS  :)   Now to test it in conjunction w a simple onCameraFrame().
                    //NEXT STEP, TO FUCKING TEST IT ON CODE, See if flags triggered in onCREATE CAN TRIGGER ONCAMERAFRAME?


                    ////            editText.setText(mynumber);    //android.content.res.Resources$NotFoundException: String resource ID #0x12
                    //            editText.setText(String.valueOf(mynumber));    // :D =D WORKS!

                }else{onFinish();}
            }

            public void onFinish() {

                Log.d("Elapsed Time since bootup bef sleep: ", "" + SystemClock.elapsedRealtime());
                SystemClock.sleep(50);
                Log.d("SystemClock.Sleep", "50ms");   // =D  Rly Worked, added a delay of 5s, puase the Whole Damn Fucking thing.  Rly slept for 5s, to "Rechar
                //NOT IDEAL TO PUT SLEEP ON MAIN THREAD, COS IT STOPS THE ENTIRE MAIN THREAD, FRAME FREEZES!  Hang Type of feel.
                Log.d("Elapsed Time since bootup aft sleep: ", "" + SystemClock.elapsedRealtime());

//                editText.setText("done!");  //can be as simple and elegant as a text/even a Logcat output :D
//                Log.d("done!","");  // ??? :( Log.d does not work/output on the android Terminal why?
//                what if i leave it as empty?? See what happens, whether got throw any Error? - nOPE ALSO OK, cAN!

//                start();    // :D :D :D YASSS!  IT FREAKING WORKED!  Loop on forever . HAHAHAHHAA! YAY! JOY TO THE WORLD!

                if (score<8){
                    start();   //loop/restart current Timer
                }
                else {
                    cancel();  //cancel current Timer
                    Timer2.start();   //start the New Timer, Timer2, with a faster Interval!

                    //CHEY, Apparently must put Timer2 above, in roder to INITIALIZE IT befroe Timer1, else code will think Timer2 not initialize yet. \CHEY, small but IMPT Concept! Same goes for all other variables! Rmbr to intialize like this! If nd to call a var within a method body, ND to place it ahead/bef omethod body in order to INITIALIZE IT!
                    //\**Seemingly small but IMPT Concept!  Same goes for all other variables! Rmbr to intialize like this!
                    //\**Whenever nd to call a var within a method body, ND to place it ahead/bef omethod body in order to INITIALIZE IT!
                    //start the second Timer @ 2 seconds Interval   //works, BUT, code ran both timer tgt too in the first instance, so nd a boolean Flag to control Logic instead
                }
            }
        }.start();


    }   //enclosing bracket for onResume() method!



    @Override
    protected void onPause() {    //PROB W HAVING MULTIPLE TIMERS NOW IS THAT IN ONPAUSE() HOW TO KNOW WHICH TIMER CALLED THIS?
        //ND TO KNOW cos Nd to call the specific Timer to Cancel!  Actl what if dun cancel? Cos anyway OnPause() freezes the frame.
        //Think the prob is that onPause will create MULTIPLE FRAGMENTs of the MAIN UI Thread(so each Framgent sorta behaves likea "thread"), so when we call restart(), App doesn't know which FRAGMENT to run, a& the Application CRASHES!
        //tRY/tEST ALRDY, Even if click immediately, App still crashes! So rly nd to handle the specific Timer!  CALL IT & cancel() to disable it.

        //Soln: Try just set to Switch off/cancel() ALL Timers? I mean no harm right? Cancel w/o instantiating? Not sure if will result in NPE, NullPointerException Error cos cannot find object reference if instance of Timer not even created yet.
        //Mmm.... seems ok leh, no prob even when Timer4 not yet instantiated, nvr throw NPE. Seems like the specific Timer will reference and see if it has a corresponding cancel() function attach to it, else it just ignores.
        // :O SEEMS LIKE THE SOLN WORKED!  :D
        super.onPause();
        Log.v("","onPause() called");
//        Toast.makeText(getApplicationContext(), "You have ran out of Lives. Please try again! :D", Toast.LENGTH_LONG).show();    //display msg dailog box back to user.

        //objects stop moving, imply mainThread stop? / frames Stop? BUT  Lives still counting down! Implying that CountDownRunner still alive.
        //Lives=0; //permanently assign to this to prevent it from continuing to count down
        Timer1.cancel();//What i nd is to stop the CountDownTimer.  //Yuppadeedeedoodledoo. Yup This is it!  =D
        Timer2.cancel();
        Timer3.cancel();
        Timer4.cancel();// Mmm.... seems ok leh, no prob even when Timer4 not yet instantiated, nvr throw NPE. Seems like the specific Timer will reference and see if it has a corresponding cancel() function attach to it, else it just ignores.
        Timer5.cancel();
        Timer6.cancel();
        Timer7.cancel();
        Timer8.cancel();
        Timer9.cancel();

        Log.v("","ALL Timers Cancelled");

//        //Not working Leh :(

        Log.v("Bef executing AlertDialog","");

        String alert1= "You have ran out of Lives. Please try again! :D";
        String alert2= "Your highest score is: ";

        new AlertDialog.Builder(this)
                .setTitle("Game Ended")
//                .setMessage("You have ran out of Lives. Please try again! :D")
//                .setMessage("")
//                .setMessage("Your highest score is: ")
                .setMessage(alert1 + "\n\n"+ alert2 + score)
//                .setView(scoreText)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // restart() the entire Fking Activity (aka Application)!
                        recreate();   //  YASSS, IT FUCKING/fUgging WORKED!  =D

                    }
                }).show();
        Log.d("Executed Dialog Box","");

        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();   //This method is provided for clients, so they can disable camera connection and stop the delivery of framthe surface view itself is not destroyed and still stays on the screen
        }
    }


    @Override
    public void recreate()
    {
        if (android.os.Build.VERSION.SDK_INT >= 11)
        {
            super.recreate();
        }
        else   // FOR OS vers SDK_INT < 11  Bef 11)
        {
            startActivity(getIntent());
            finish();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();   //This method is provided for clients, so they can disable camera connection and stop the delivery of framthe surface view itself is not destroyed and still stays on the screen
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    public void onCameraViewStopped() {

        InputFrame.release();
        matFinal.release();

    }

    @Override
    public void onCameraViewStarted(int width, int height) {

//        setOptimatlFpsFrameRateScreenResolution();

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
        Imgproc.resize(mat1, mat2, InputFrame.size(), 0, 0, 0);    // params:(Mat src, Mat dst, Size dsize, fx, fy, interpolation)   Extract the dimensions en Orientation, obtain the new orientation's surface width & height.  Try to resize to fit to screen.
        Core.flip(mat2, InputFrame, -1);   // mat3 now get updated, no longer is the Origi inputFrame.rgba BUT RATHER the transposed, resized, flipped versie.rgba().

        rowWidth = InputFrame.rows();
        colWidth = InputFrame.cols();


        Imgproc.cvtColor(InputFrame, InputFrame, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(InputFrame, InputFrame, Imgproc.COLOR_RGB2HSV);


//        Lower_Yellow = new Scalar(21, 100, 50);    //HSV color scale  H to adjust color, S to coontrol color variation, V is indicator of amt of light reque on object to be seen.
//        Lower_Yellow = new Scalar(21, 100, 150);    //HSV color scale  H to adjust color, S to coontrol color variation, V is indicator of amt of light reque on object to be seen.
        Lower_Yellow = new Scalar(21, 120, 75);    //Latest NEWEST Best condition.
//        Lower_Yellow = new Scalar(21, 150, 150);    //ORIGINAL VALUE, NOT VRY GOOD IN LOW LIGHT Dark conditions though! -  HSV color scale  H to adjust color, S to coontrol color variation, V is indicator of amt of light reque on object to be seen.
        Upper_Yellow = new Scalar(31, 255, 360);    //HSV color scale

        Core.inRange(InputFrame, Lower_Yellow, Upper_Yellow, maskForYellow);


        final Size kernelSize = new Size(5, 5);  //must be odd num size & greater than 1.
        final Point anchor = new Point(-1, -1);   //default (-1,-1) means that the anchor is at the center of the structuring element.
        final int iterations = 1;   //number of times dilation is applied.  https://docs.opencv.org/3.4/d4/d76/tutorial_js_morphological_ops.html

        //        Mat kernel = new Mat(new Size(3, 3), CvType.CV_8UC4);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, kernelSize);

        Imgproc.morphologyEx(maskForYellow, yellowMaskMorphed, Imgproc.MORPH_CLOSE, kernel, anchor, iterations);   //dilate first to remove then erode.  Whimes more pronounced, erode away black regions

        //        Apply Canny for edge detection
        Mat mIntermediateMat = new Mat();

        Imgproc.GaussianBlur(yellowMaskMorphed,mIntermediateMat,new Size(3,3),0,0);    //kernel size (3,3) better accuracy cos scrutinizing over a smaller p decide whether that patch is in/out of range, so threshold/allowance smaller.

        Imgproc.Canny(mIntermediateMat, mIntermediateMat, 5, 120);   //try adjust threshold   //https://stackoverflow.com/questions/25125670/best-value-for-nny



        LargestBlobDetection();
//        plotObjectBox1atAtimeOnUI();

        plotObjectsOnUI();

        matchObject();

        return InputFrame;  //Works =D =D =D !  YASSSS!!            //Mat Type output.   So OnCameraFrame we only work on Mat!

    }




//-------------------  METHODS SELF-DEFINED  -----------------------------


    public void LargestBlobDetection(){

        //https://gist.github.com/six519/743e32c9879ffea299b0b175823adc88 - how to use IMGPROC.CONTOURAREA()
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(yellowMaskMorphed, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));   //returns a lis

        Log.d("contour", "size = " + contours.size());   //returns 21, 91, 120, 104, 116, 58, ......  varying contour size detected & returned. Observe how ceed 100.  :O WAOW!
        hierarchy.release();    //Maybe cos i nvr release the hierarchy Mat?

//        Log.d("hierarchy", "dimensions= " + hierarchy.dims());   //print out the dimensions of the hierarchy!
//        Log.d("hierarchy", "" + hierarchy);   //prints out the hierarchy Mat type & memory address reference!
//
//        int cols = hierarchy.cols();
//        int rows = hierarchy.rows();
//        for (int i = 0; i < rows; i++) {
//            for (int j = 0; j < cols; j++) {
////                hierarchyHolder.add(hierarchy.get(i,j));
//                Log.d("hierarchy", "" + hierarchy.get(i, j).toString());   //hierarchy.get(i,j) is a double[] type.
//            }
//        }


        //======================= detect LARGEST CONTOUR function!  =D =D =D ======================
        double maxArea1 = 0;
        int maxAreaContourIndex1 = 0;   //try define globally, cos error msg say is index 0, so means not updated!

        for (int i = 0; i < contours.size(); i++) {

            double contourArea1 = Imgproc.contourArea(contours.get(i));   //Size of Mat contour @ that particular point in ArrayList of Points.
            if (maxArea1 < contourArea1) {
                maxArea1 = contourArea1;
                maxAreaContourIndex1 = i;   //just return 1 that largest Contour out of many contours.
                Log.d("value of", "maxAreaContourIndex1=" + maxAreaContourIndex1);   //0
            }
        }
        Log.d("Largest", "maxAreaContourIndex1 is=" + maxAreaContourIndex1);  //0


        Imgproc.cvtColor(InputFrame, InputFrame, Imgproc.COLOR_HSV2RGB);


        //nd try-catch cos no matter what we always perform the centroid computation, assuming that got yellow objects in the frame, but when there isnt, noetected, no largest contour pass in, so nullPointerExceptionError will arise cos no Object to reference to & work on!  Plus the FATAL IndexOutOfBound Excepti the contours array but there is no object in it.  So we bound this sect of code within try catch loop to handle error gracefully!
        try {
            List<Moments> mu = new ArrayList<>(contours.size());
            mu.add(Imgproc.moments(contours.get(maxAreaContourIndex1)));    //Just adding that 1 Single Largest Contour (largest ContourArea) to arryalist tor MOMENTS to compute CENTROID POS!

            List<Point> mc = new ArrayList<>(contours.size());   //the Circle centre Point!

            p1.x = (mu.get(0).m10 / (mu.get(0).m00 + 1e-5));    //add 1e-5 to avoid division by zero
            p1.y = (mu.get(0).m01 / (mu.get(0).m00 + 1e-5));    //Point1 updated here to Centroid of LargestBlob detected on our Hand!
            //Extract out p1, so that we can use it to do computation for the computeEuclideanDistance() method!

            //add 1e-5 to avoid division by zero
            //            mc.add(new Point(mu.get(0).m10 / (mu.get(0).m00 + 1e-5), mu.get(0).m01 / (mu.get(0).m00 + 1e-5)));   //index 0 cos there shld only be 1 contouest one only!
            mc.add(p1);   //index 0 cos there shld only be 1 contour now, the largest one only!
            //notice that it only adds 1 point, the centroid point. Hence only 1 point in the mc list<Point>, so ltr reference that point w an index 0!

            Random random = new Random(12345);

            Scalar color = new Scalar(random.nextInt(256), random.nextInt(256), random.nextInt(256));  //return a random color

            Imgproc.circle(InputFrame, mc.get(0), 15, color, -1);   //just to plot the small central point as a dot on the detected ImgObject.

            //====================== If want to put Bounding Rect ard Largest Contour Object! ==================
            Rect r = Imgproc.boundingRect(contours.get(maxAreaContourIndex1));    // sOLN Saviour =D :  https://ratiler.wordpress.com/2014/09/08/detection-dc-javacv/
            Imgproc.rectangle(InputFrame, r.tl(), r.br(), new Scalar(255, 0, 255), 2);    //returns r & b combination, line also thicker.
            Log.d("placing boundingRect on","Largest Contour Object");
            //            Log.d("r.br(), upper left corner coordinate of RectBox ",""+r.br());   //return new Point(x + width, y + height);
            //            Log.d("r.tl(), bottom right corner coordinate of RectBox ",""+r.tl());   //return new Point(x, y);

            Log.d("r.tl() ",""+r.tl());   //return new Point(x, y);    //top left corner coordinate of boundingRect
            Log.d("r.br() ",""+r.br());   //return new Point(x + width, y + height);    //bottom right corner coordinate of boundingRect

            //===================================================================================================

        } catch (IndexOutOfBoundsException e) {
            Log.d("INDEX OUT OF BOUNDS EXCEPTION", "DETECTED" + e);
        }
        //        }catch(ArrayIndexOutOfBoundsException Ae){
        //            Log.d("ARRAYINDEX OUT OF BOUNDS EXCEPTION","DETECTED" + Ae);
        //        }

        //take note that contours is an ArrayList<>()  we define above, see line 1743!
        //is constantly filling in, we never clear it! Is constanly taking in background obj as Native Objects!
//        contours.removeAll();  //Removes from this list all of its elements that are contained in the specified collection (optional operation).
//        contours.clear();   //Removes all of the elements from this list (optional operation). The list will be empty after this call returns.
//        Runtime.getRuntime().gc();   //https://stackoverflow.com/questions/14016732/how-to-free-object-in-android
                                    //http://net-informations.com/java/cjava/gc.htm
                                    // https://stackoverflow.com/questions/44369385/is-it-a-good-practice-to-use-runtime-getruntime-gc-method-in-ondestroy-of

    }





    //    public void plotObjectBox1atAtimeOnUI(){
    public void plotObjectsOnUI(){

        //==== pLOT/DRaW 4 floodfilled RECTANGLES ON THE 4 CORNERS OF THE SCREEN 1 at a time.  Fixed positions!  =========================

        //Scalar color = new Scalar(0,255,0);   //r,g,b, so only show green box
        //https://qiita.com/yeb8jo/items/2ab97dc69b375b501fba - floodfill implementation in Android
        //@@--> THIS THE ONE :D -  https://github.com/zylo117/SpotSpotter/blob/master/src/pers/zylo117/spotspotter/patternrecognition/ROI_Irregular.java - floodfill in Android


        Rect r1 = new Rect(0,0,300,300);
        Rect r2 = new Rect(colWidth-300,0,300,300);
        Rect r3 = new Rect(0,rowWidth-300,300,300);
        Rect r4 = new Rect(colWidth-300,rowWidth-300,300,300);


        r1centroid.x =(r1.br().x +r1.tl().x) / 2;
        r1centroid.y =(r1.br().y +r1.tl().y) / 2;

        r2centroid.x =(r2.br().x +r2.tl().x) / 2;
        r2centroid.y =(r2.br().y +r2.tl().y) / 2;

        r3centroid.x =(r3.br().x +r3.tl().x) / 2;
        r3centroid.y =(r3.br().y +r3.tl().y) / 2;

        r4centroid.x =(r4.br().x +r4.tl().x) / 2;
        r4centroid.y =(r4.br().y +r4.tl().y) / 2;


        final Mat maskFloodFill = Mat.zeros(new Size(colWidth + 2, rowWidth + 2), CvType.CV_8UC1);


        //YUPPA DEEDEE DOODLE FK EM THREADS!  IT WAS THE FKING THREADS THAT CAUSED THE iSSUE/PROB earlier!
        if(flag1 == true){   // FLATHERLY FATHERLY FUCKING WORKS!  =D
//            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);
            Imgproc.rectangle(InputFrame, r1.br(), r1.tl(), new Scalar(0, 255, 0), 4);    //top left corner
            Imgproc.floodFill(InputFrame, maskFloodFill, r1centroid, new Scalar(0,255,0), new Rect() , new Scalar(20,20,20),new Scalar(20,20,20),4);   //mm,r, cos 120 sometimes will glitch whole screen becomes green, instead of just the 4 corners only.
            Log.d("running top LEFT corner object", "");
        }

        if(flag2 == true){
//            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);
            Imgproc.rectangle(InputFrame, r2.br(), r2.tl(), new Scalar(0, 255, 0), 4);    //top right corner
            Imgproc.floodFill(InputFrame, maskFloodFill,r2centroid, new Scalar(0,255,0), new Rect() , new Scalar(20,20,20),new Scalar(20,20,20),4);
            Log.d("running top RIGHT corner object", "");
        }

        if(flag3 == true){
//            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);
            Imgproc.rectangle(InputFrame, r3.br(), r3.tl(), new Scalar(0, 255, 0), 4);    //top right corner
            Imgproc.floodFill(InputFrame, maskFloodFill,r3centroid, new Scalar(0,255,0), new Rect() , new Scalar(20,20,20),new Scalar(20,20,20),4);
            Log.d("running BOTTOM LEFT corner object", "");    //logs not working, not printing/showing on the logcat in the IDE!  :(
        }

        if(flag4 == true){

//            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);
            Imgproc.rectangle(InputFrame, r4.tl(), r4.br(), new Scalar(0, 255, 0), 4);    //top right corner
            Imgproc.floodFill(InputFrame, maskFloodFill,r4centroid, new Scalar(0,255,0), new Rect() , new Scalar(20,20,20),new Scalar(20,20,20),4);
            Log.d("running BOTTOM RIGHT corner object", "");
        }


        if(flag5 == true){   // FLATHERLY FATHERLY FUCKING WORKS!  =D
            Log.d("f1ag1", "object 1 & 4");
//            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);

            if (clear1==true) {
                Imgproc.rectangle(InputFrame, r1.br(), r1.tl(), new Scalar(0, 255, 0), 4);    //Object 1: top left corner
                Imgproc.floodFill(InputFrame, maskFloodFill, r1centroid, new Scalar(0, 255, 0), new Rect(), new Scalar(20, 20, 20), new Scalar(20, 20, 20), 4);   //mm,r, cos 120 sometimes will glitch whole screen becomes green, instead of just the 4 corners only.
            }
            if (clear4==true) {
                Imgproc.rectangle(InputFrame, r4.br(), r4.tl(), new Scalar(0, 255, 0), 4);    //Object 4: bottom right corner
                Imgproc.floodFill(InputFrame, maskFloodFill, r4centroid, new Scalar(0, 255, 0), new Rect(), new Scalar(20, 20, 20), new Scalar(20, 20, 20), 4);
            }
        }

        if(flag6 == true){
            Log.d("f1ag2", "object 2 & 4");
//            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);

            if (clear2==true) {
                Imgproc.rectangle(InputFrame, r2.br(), r2.tl(), new Scalar(0, 255, 0), 4);    //top right corner
                Imgproc.floodFill(InputFrame, maskFloodFill, r2centroid, new Scalar(0, 255, 0), new Rect(), new Scalar(20, 20, 20), new Scalar(20, 20, 20), 4);
            }
            if (clear4==true) {
                Imgproc.rectangle(InputFrame, r4.br(), r4.tl(), new Scalar(0, 255, 0), 4);    //bottom right corner
                Imgproc.floodFill(InputFrame, maskFloodFill, r4centroid, new Scalar(0, 255, 0), new Rect(), new Scalar(20, 20, 20), new Scalar(20, 20, 20), 4);
            }
        }

        if(flag7 == true){
            Log.d("f1ag3", "object 3 & 1");    //logs not working, not printing/showing on the logcat in the IDE!  :(
//            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);

            if (clear3==true) {
                Imgproc.rectangle(InputFrame, r3.br(), r3.tl(), new Scalar(0, 255, 0), 4);    //bottom left corner
                Imgproc.floodFill(InputFrame, maskFloodFill, r3centroid, new Scalar(0, 255, 0), new Rect(), new Scalar(20, 20, 20), new Scalar(20, 20, 20), 4);
            }
            if (clear1==true) {
                Imgproc.rectangle(InputFrame, r1.br(), r1.tl(), new Scalar(0, 255, 0), 4);    //top left corner
                Imgproc.floodFill(InputFrame, maskFloodFill, r1centroid, new Scalar(0, 255, 0), new Rect(), new Scalar(20, 20, 20), new Scalar(20, 20, 20), 4);
            }
        }

        if(flag8 == true){
            Log.d("f1ag4", "object 3 & 4");
//            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);

            if (clear3==true) {
                Imgproc.rectangle(InputFrame, r3.tl(), r3.br(), new Scalar(0, 255, 0), 4);    //bottom left corner
                Imgproc.floodFill(InputFrame, maskFloodFill, r3centroid, new Scalar(0, 255, 0), new Rect(), new Scalar(20, 20, 20), new Scalar(20, 20, 20), 4);
            }
            if (clear4==true) {
                Imgproc.rectangle(InputFrame, r4.tl(), r4.br(), new Scalar(0, 255, 0), 4);    //bottom right corner
                Imgproc.floodFill(InputFrame, maskFloodFill, r4centroid, new Scalar(0, 255, 0), new Rect(), new Scalar(20, 20, 20), new Scalar(20, 20, 20), 4);
            }
        }

        if(flag9 == true){
            Log.d("f1ag5", "object 1 & 2");
//            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);

            if (clear1==true) {
                Imgproc.rectangle(InputFrame, r1.tl(), r1.br(), new Scalar(0, 255, 0), 4);    //bottom left corner
                Imgproc.floodFill(InputFrame, maskFloodFill, r1centroid, new Scalar(0, 255, 0), new Rect(), new Scalar(20, 20, 20), new Scalar(20, 20, 20), 4);
            }
            if (clear2==true) {
                Imgproc.rectangle(InputFrame, r2.tl(), r2.br(), new Scalar(0, 255, 0), 4);    //bottom right corner
                Imgproc.floodFill(InputFrame, maskFloodFill, r2centroid, new Scalar(0, 255, 0), new Rect(), new Scalar(20, 20, 20), new Scalar(20, 20, 20), 4);
            }
        }

        if(flag10 == true){
            Log.d("f1ag6", "object 2 & 3");
//            final Mat maskFloodFill = Mat.zeros(new Size(InputFrame.cols() + 2, InputFrame.rows() + 2), CvType.CV_8UC1);

            if (clear2==true) {
                Imgproc.rectangle(InputFrame, r2.tl(), r2.br(), new Scalar(0, 255, 0), 4);    //bottom left corner
                Imgproc.floodFill(InputFrame, maskFloodFill, r2centroid, new Scalar(0, 255, 0), new Rect(), new Scalar(20, 20, 20), new Scalar(20, 20, 20), 4);
            }
            if (clear3==true) {
                Imgproc.rectangle(InputFrame, r3.tl(), r3.br(), new Scalar(0, 255, 0), 4);    //bottom right corner
                Imgproc.floodFill(InputFrame, maskFloodFill, r3centroid, new Scalar(0, 255, 0), new Rect(), new Scalar(20, 20, 20), new Scalar(20, 20, 20), 4);
            }
        }


    }  //enclosing bracket for plotObjectsOnUI()






    public void matchObject(){    //Euclidean Dist Algorithm for Centroid Matching!


        if (flag1 == true){
            distance1 = computeEuclideanDistance(r1centroid,p1);
//            if (distance1 < 65){   //threshold too small, becomes a prob when user stands far away + object held is small.
            if (distance1 < 120){
                Log.d("scored","topLeft");
                computeScore();
                flag1=false;   //remove object from screen, stop displaying, cos scored alrdy
                clear1=false;  //responsible for keeping track of Lives, whether to deduct anot.
            }
        }


        if (flag2 == true){
            distance1 = computeEuclideanDistance(r2centroid,p1);
//            if (distance1 < 65){
            if (distance1 < 120){
//                Log.d("Ahh..","topRight & Object matched");
                Log.d("scored","topRight");
                computeScore();
                flag2=false;
                clear2=false;
            }
        }

        if (flag3 == true){
            distance1 = computeEuclideanDistance(r3centroid,p1);
//            if (distance1 < 65){   //
            if (distance1 < 120){   //threshold to qualify as a match!
                Log.d("scored","bottomLeft");
                computeScore();
                flag3=false;
                clear3=false;
            }
        }

        if (flag4 == true){
            distance1 = computeEuclideanDistance(r4centroid,p1);
//            if (distance1 < 65){   //threshold value in order to qualify as a positive match!
            if (distance1 < 120){   //threshold value in order to qualify as a positive match!
                Log.d("scored","bottomRight");
                computeScore();
                flag4=false;
                clear4=false;
            }
        }



        if (flag5 == true) {   //object 1 & 4
            Log.d("f1ag6", "object 1 & 4");
            distance1 = computeEuclideanDistance(r1centroid, p1);
//               if (distance1 < 65) {
            if (distance1 < 120) {   //increase dist detection threshold to +/- radius 120, cos when user stand at a far dist away from screen, object becomes vry small, vry hard to hit the rect centroid, so threshold acceptance must increase!
                //=D YUP, MUCH BETTER!  :D
                if (clear1 == true) {   //this helps us ensure that computeScore does not execute REPEATEDLY, cos EVEN THOUGH clear flag is down, but the fact that flag still remain true if either clear flag is still on, it will still silently execute that object in the background even though it does not show it, so nd another level of check! That's why earloer w/o this check, code keeps computing the score in the object's box in the background.
                    //so now once clear1 change to false, this condition will fail, will not enter this block to perform computeScore() again, saved from repeated executions.
                    Log.d("scored", "topLeft");
                    computeScore();
//                    flag1=false;   //remove object from screen, stop displaying, cos scored alrdy
                    clear1 = false;  //switch rect object off, stop displaying the rect on UI + don't deduct live cos scored in time.   //this updates the flag in the countDownTimer =D.
                }
//                   else {  //if (clear1==false) alrdy, we dun do anymore computation.
//                       //break;   //Error: break outside switch or loop
//                       //since else if-else loop just executes one, no nd to break out loop once it finish execution.
//                       //if no implementation, just leave it as blank  :)
//                   }
//
//                   //actl if that's the case, dun even nd the else loop
            }

            distance2 = computeEuclideanDistance(r4centroid, p1);   //p1 is the centroid value of our Hand held object, the largest blob, is computed frame by frame Real time.
//               if (distance2 < 65) {
            if (distance2 < 120) {
                if (clear4 == true) {
                    Log.d("scored", "topLeft");
                    computeScore();
//                   flag1=false;   //remove object from screen, stop displaying, cos scored alrdy
                    clear4 = false;  //Flag serves/affects 2 purpose:  Lives + Rect object display.    //this updates the flag in the countDownTimer =D.
                }
            }
//               else {
//                   //want it to reamain as status quo & do nothing, so, empty implementation.
//               }  //actl if that's the case, dun even nd the else loop


//               if (clear1==false && clear4==false){
//                     //actl kinda dun nd, cos once clear flags set to off, we dun nd to bother if flag still on, CountDownTimer's next OnTick() will help us to disable/settle it.
//               }

        }


        if (flag6 == true){   //object 2 & 4
            Log.d("f1ag6", "object 2 & 4");
            distance1 = computeEuclideanDistance(r2centroid,p1);
//            if (distance1 < 65) {
            if (distance1 < 120) {
                if (clear2 == true) {   //this ensures that computeScore will only be executed Once, cos no matter what flag will remain On/True/Open when either Object is still displayed (clear==true)
                    Log.d("scored", "topLeft");
                    computeScore();
//                flag2=false;   //remove object from screen, stop displaying, cos scored alrdy
                    clear2 = false;  //this updates the flag in the countDownTimer =D.
                }
            }

            distance2 = computeEuclideanDistance(r4centroid,p1);   //p1 is the centroid value of our Hand held object, the largest blob, is computed frame by frame Real time.
//            if (distance2 < 65) {
            if (distance2 < 120) {      //increase dist detection threshold to +/- radius 120, cos when user stand at a far dist away from screen, object becomes vry small, vry hard to hit the rect centroid, so threshold acceptance must increase!
                //=D YUP, MUCH BETTER!  :D

                if (clear4 == true) {
                    Log.d("scored", "topLeft");
                    computeScore();
//                flag2=false;   //remove object from screen, stop displaying, cos scored alrdy
                    clear4 = false;  //this updates the flag in the countDownTimer =D.
                }
            }

        }


        if (flag7 == true){    //object 3 & 1
            Log.d("f1ag6", "object 3 & 1");
            distance1 = computeEuclideanDistance(r3centroid,p1);
//            if (distance1 < 65) {
            if (distance1 < 120) {
                if (clear3 == true) {
                    Log.d("scored", "topLeft");
//                computeScore();
//                flag3=false;   //remove object from screen, stop displaying, cos scored alrdy
                    clear3 = false;  //responsible for keeping track of Lives, whether to deduct anot.
                }
            }

            distance2 = computeEuclideanDistance(r1centroid,p1);   //p1 is the centroid value of our Hand held object, the largest blob, is computed frame by frame Real time.
//            if (distance2 < 65) {
            if (distance2 < 120) {
                if (clear1 == true) {
                    Log.d("scored", "topLeft");
//                            computeScore();
//                flag3=false;   //remove object from screen, stop displaying, cos scored alrdy
                    clear1 = false;  //responsible for keeping track of Lives, whether to deduct anot.    //this updates the flag in the countDownTimer =D.
                }
            }

//            if (clear3==false || clear1==false){
//                computeScore();    //not a good idea, cos is compute after flag is done, not shown real time on user screen + does not prevent code from executing rect object in background.
//            }
        }

        if (flag8 == true){    //object 3 & 4
            Log.d("f1ag6", "object 3 & 4");
            distance1 = computeEuclideanDistance(r3centroid,p1);
//            if (distance1 < 65) {
            if (distance1 < 120) {
                if (clear3==true) {
                    Log.d("scored", "topLeft");
                    computeScore();
//                flag4=false;   //remove object from screen, stop displaying, cos scored alrdy
                    clear3 = false;  //responsible for keeping track of Lives, whether to deduct anot.    //this updates the flag in the countDownTimer =D.
                }
            }

            distance2 = computeEuclideanDistance(r4centroid,p1);   //p1 is the centroid value of our Hand held object, the largest blob, is computed frame by frame Real time.
//            if (distance2 < 65) {
            if (distance2 < 120) {
                if (clear4==true) {
                    Log.d("scored", "topLeft");
                    computeScore();
//                flag4=false;   //remove object from screen, stop displaying, cos scored alrdy
                    clear4 = false;  //responsible for keeping track of Lives, whether to deduct anot.
                }
            }
        }


        if (flag9 == true){    //object 1 & 2
            Log.d("f1ag6", "object 1 & 2");
            distance1 = computeEuclideanDistance(r1centroid,p1);
//            if (distance1 < 65) {
            if (distance1 < 120) {
                if (clear1==true) {
                    Log.d("scored", "topLeft");
                    computeScore();
//                flag4=false;   //remove object from screen, stop displaying, cos scored alrdy
                    clear1 = false;  //responsible for keeping track of Lives, whether to deduct anot.    //this updates the flag in the countDownTimer =D.
                }
            }

            distance2 = computeEuclideanDistance(r2centroid,p1);   //p1 is the centroid value of our Hand held object, the largest blob, is computed frame by frame Real time.
//            if (distance2 < 65) {
            if (distance2 < 120) {
                if (clear2==true) {
                    Log.d("scored", "topLeft");
                    computeScore();
//                flag4=false;   //remove object from screen, stop displaying, cos scored alrdy
                    clear2 = false;  //responsible for keeping track of Lives, whether to deduct anot.
                }
            }
        }


        if (flag10 == true){    //object 2 & 3
            Log.d("f1ag6", "object 2 & 3");
            distance1 = computeEuclideanDistance(r2centroid,p1);
//            if (distance1 < 65) {
            if (distance1 < 120) {
                if (clear2==true) {
                    Log.d("scored", "topLeft");
                    computeScore();
//                flag4=false;   //remove object from screen, stop displaying, cos scored alrdy
                    clear2 = false;  //responsible for keeping track of Lives, whether to deduct anot.    //this updates the flag in the countDownTimer =D.
                }
            }

            distance2 = computeEuclideanDistance(r3centroid,p1);   //p1 is the centroid value of our Hand held object, the largest blob, is computed frame by frame Real time.
//            if (distance2 < 65) {
            if (distance2 < 120) {
                if (clear3==true) {
                    Log.d("scored", "topLeft");
                    computeScore();
//                flag4=false;   //remove object from screen, stop displaying, cos scored alrdy
                    clear3 = false;  //responsible for keeping track of Lives, whether to deduct anot.
                }
            }
        }



    }   //enclosing bracket for matchObject()




    public double computeEuclideanDistance(Point a, Point b){
        double distance = 0.0;   //internal method var, only exists within the lifecycle of this var.

        try{
            if(a != null && b != null){
                double xDiff = a.x - b.x;
                double yDiff = a.y - b.y;
                distance = Math.sqrt(Math.pow(xDiff,2) + Math.pow(yDiff, 2));
            }
        }catch(Exception e){
            System.err.println("Something went wrong in euclideanDistance function in "+e.getMessage());
            //            System.err.println("Something went wrong in euclideanDistance function in "+ Utility.class+" "+e.getMessage());
        }
        return distance;
    }



    public void computeScore(){   //calculates & tracks the total number of objects matched successfully thus far.
        score += 1;
        scoreText.setText(String.valueOf(score));
    }



    //https://opencv-python-tutroals.readthedocs.io/en/latest/py_tutorials/py_gui/py_drawing_functions/py_drawing_functions.html  -  How to Draw stuffs on s
    //https://www.youtube.com/watch?v=QfQE1ayCzf8 - How to Start a Background Thread in Android, to let it run ASYNC in background, wont obstruct Main() thr


}
