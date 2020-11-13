# FYP Interactive EXERGame w Yellow Object Detection using OpenCV
### Background

**Introduction**:
  * Musculoskeletal disorder is one of the leading causes of death and disability in Singapore. This is brought on by long duration of being in a sedentary position due to work or study that leads to bad posture. The goal of this project is to design a fun and interactive workout experience. By gamifying the workout experience, we hope to offer users a short 2-3 min workout to stretch their bodies in between breaks during work or study to relieve pressure on their body.
<br />

**Problem Statement**:
  * Research has shown that people are working long hours & not getting enough exercise. Unhealthy inactive lifestyle characterised by long hours of sitting in a sedentary position, that places alot of pressure on the musculoskeletal structure.
<br />

**Aim**:
  * To design a cheap, fun, interactive, convenient workout experience that allow users to stretch and exercise their body during short breaks in between work or study.
<br />

**Solution**:
  * Gamify the workout experience to offer users a short but interactive 2-3 min workout that can be performed at their own convenience via the mobile application anytime, anywhere.
<br />

**Why Mobile App**:
  * Mobile phone has become an intrinsic part of our life, an extension and enabler towards achieving our daily goals through greater interconnectedness, enhancing productivity and efficiency. However higher connectiveness has also resulted in longer hours behind the desk. As such, a mobile application that can provide a quick workout at the convenience of the user would help alleviate musculoskeletal health issues. The app is offered at zero cost so that the game can be enjoyed by users regardless of age or income status.
<br />

**Concept/Design**:
  * The app was developed using phone camera as the interfacing component between the app and the user, OpenCV (computer vision) Library to process frames and user’s motion. The use of OpenCV allows a greater range of motion detection instead of traditional sensors that restricts user to a specific type of motion in order for detection to work.
  * In this project we decide to explore the usage of the mobile phone's built-in Camera, instead of special motion detection sensors such as LIDAR used in XBOX and KINECT, or the gyroscopes and accelerometers in our phone or fitness band which are only able to detect a specific type of motion.
  * By utilizing the mobile phone's built in camera, we do not require the user to carry or wear any motion tracking devices on their body while at the same time able to track the full range of the user's body & motion.
<br />

**Goal of the user**:
  * To match the position of their hands to the position of the square boxes that appear on the screen. The number of boxes and frequency increases as the game progresses. The challenge is for the user to survive as long as possible, and to surpass their own personal highest score.
  * Users can customize the intensity of the workout experience by choosing to carry weights according to their level of comfort.
  * We hope that this game motivates the user to push beyond their limits, to surpass their previous high score, while at the same time encourage users to get up from their sit in between work or study breaks to offer a quick relieve on the pressure that is affecting their posture.
  <br />
  <br />
  
Libraries used: OpenCv 3.4.6
Gradle vers 3.5.0

Game development proceeded as follow:

'''
<br />
<br />
**1st Phase: Yellow Object Detection on each frame**
  * receive incoming frames from the camera
  * Perform Image processing on each individual frame.
  * map the cooridinates of the pixel on the frame into a Matrix for furthur computation and processing later.
  * Receive inputFrame as 4 channel RGBA.
  * Convert 4 channel to 3 Channel HSV color scheme. (chose HSV over RGB, cos it takes into consideration color saturation & brightness, which will help us achieve a more stable colour detection.)
  * perform Color Segmentation in HSV color scheme using InRange(), input value to threshold to Yellow color range.
  * Mask returns as a binary black and white image, with detected Yellow objects shown as white, the rest as black.
  * Perform Morphological operations on the mask, with dilation being performed first to fill up any gaps in the detected white region, to expand connected components, and later followed by erosion to shrink the number of features and remove any noise.
  * Perform gaussian blur to convolve & smooth the image as a precursor step to performing edge detection (since edge detection results are easily affected by image noise, so we filter out the noise to prevent false detection caused by noise).
  * Perform canny() edge detection to detect regions with huge colour gradient changes, this allows us to extract edges to bring the object detected into focus.
  * Next we use findContours() to identify continuous points that have the same color or intensity, along the boundary of the object detected.
  * The detected contours will be listed in a hierarhical structure. We loop through the entire list contours and  extract out the largest Contour by using ContourArea() and update the index of largest Contour detected to a variable that we asssign for us to identify and access it later.
  * This allows us to track the single largest Yellow object detected in the frame, which will be the object that our user will be holding on to as they perform the Exercise. (User is to refrain from wearing yellow outfit as it will interfere with the detection of the yellow object that player is to hold in hand).
  * Compute the moments of the largest contour to find the Centroid position.
  * plot the centroid as a point, indicated by a circle, to display back on the frame as visual aid for the user to see.
<br />

**2nd phase: Displaying visual aids(rectangles in this case) back on the frame(screen) to the user.** 
  * mark out positions of the rectangle to be displayed on the frame.
  * supply 2 coordinates, top left & bottom right coordinates pass it into rectangle() to display a rectangle on the frame.
  * Intialize and overlay an empty Mask on top of the InputFrame which we will use to perform floodfill(process of fill up a region w a particular color, sort of like coloring/shading that region)
<br />

**3rd phase: Match object in hand to position of rectangles displayed on frame (screen).**
  * Implemented a Euclidean Distance algorithm that computes the distance of the object centroid with the rectangle centroid.
  * When distance between the 2 centroid meets an acceptable threshold value (self-defined acceptable radius for detection), we remove the rectangle from the display(screen), which will serve as a positive indicator to the player that he has cleared it.
<br />

**4th phase: Display 1 or 2 rectangle at different positions at different time of the game.**
  * Tried to use thread/Async Task/delays to keep the rectangle display on the UI for a period of 4 secs, leads to fatal crashes as Android HandlerThread class does not know which frame to since multiple frames would have coming during that period of time. 20-23 frames a sec = 80 frames in 4 sec. lifetime of frame only lasts 0.04-0.05 secs (processing input of 20-23 frames a second). Lifetime of thread/delay outlasts frame, new frames are constantly coming in and will overlay ontop of current frame. Hence even if task finish execution, we will never get to observe result since new frames are constantly received in and only the latest frame will be shown on the display screen back to the player.
  * Solution: seperate control logic(implemented in OnCreate for it to exists during the entire lifetime of application) from the image proccesing logic(to be performed on every single frame, per frame computation of 0.04 secs).
  * Use concept of flag to determine which rectangle to display at any point in time.
  * Implement a Global CountDownTimer in OnCreate Method as the control logic to switch to a different flag to activate at every fixed interval (e.g 4 secs). Loop the CountDownTimer again once it reaches zero, essentially looping it infinitely to continously display 1/2 rectangles throughout the lifetime of the application albeit in a fixed pre-defined order.
<br />

**5th phase: Escape/Exit condition for CountDownTimer with concept of Lives**
  * Since the CountDownTimer loops on infinitely, in order for the game to end, the concept of Lives was conceived. 
  * At the start of the game, we initialize a value for the Lives, which also serves as a buffer, the number of rectangles that the player is allowed to miss(fail to match) in the game.
  * Whenever the player fails to match the object to a rectangle within the allocated time period, we deduct a Live.
  * Upon reaching Lives = 0, we call onPause() to pause the entire application of the game & display an alert dialog box to the player signifying that the game has ended & request the user to play the game again.
  * When player clicks on the 'ok' button, we call the recreate() method to trigger a restart for the entire Lifecycle of the application, starting all over once again from OnCreate().
<br />

**6th phase: Alter the pace of the game, to make it go faster over time**
  * Set multiple Timers with shorter intervals for each passing/consecutive timer.
<br />

**7th phase: Pseudo-randomize display of rectangle location for each individual Timer**
  * Tried implementing a do-while loop to ensure that a different randomly-generated index value is created to set a different flag each time. However, execution of while loop on the main thread is unable to complete its computation within the lifetime of the frame (0.04 secs time period) before the next frame arrives.
  * Self-assign order of flags for each timer, set different order.
<br />

**8th phase: Condition/Logic to switch Timer using concept of Score**
  * 2 appraoches were considered when deciding how to switch between timers. The first was to fixed it, after every 4 rounds, we switch to a faster timer, however that approach is not very heuristic as it does not take into account the player's ability to cope with the pace of the game at the current pace.
  * Hence to better match the game to the player's ability, we set the conditions for switching Timer to be tag to the player's score. Each Timer is tagged to a unique range of scores, when the Player's score falls within that range, we let the Timer loop inifinitely until the player either runs out of lives or hits the maximum score within the range, and we proceed to switch to the next timer with a shorter interval (faster pace).
<br />

**9th phase: Exercise component:**
  * This game is intended to be use/perform as an exercise activity. 
  * Yellow-coloured weights are provided to the players who will use it to play the game.
  * The objective of the game is to gamify the experience of exercise,
  * with the concept of lives, I hope to tap into our innate primal survival instincts.
  * and the concept of Time to add that sense of urgency to invoke the feeling/sensation of thrill/rush to the game, which can help to   alleviate the player's fatigue experienced strenuity of the game.
  * with the concept of score, we hope to encourage them to push beyond/surpass their physical limits, to outdo their previous workout score.
  * We hope that people can use the game as a motivating factor for exercising.
  * We hope to show that exercise can be performed in a fun and relax manner and not necessarily boring, uninspiring repetitive motions.
<br />

'''

### User Feedback
  * Users feedback that they find the game enjoyable and the pace comfortable and would recommend it to their friends and family.
<br />
