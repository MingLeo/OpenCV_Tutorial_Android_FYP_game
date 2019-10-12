# FYP Interactive EXERGame w Yellow Object Detection using OpenCV

*Introduction:
  * Exercise has always been perceived as catered only to a particular niche of people who are physically active. However, most of us do not fall within that category. A 2018 study on global health conducted by WHO reports what 1 in 3 Singaporeans do not get enough physical activity and that the weekly average targets for physical activity falls short of the global average for both genders. Figures published by MOH (Ministry of Health) in 2018 has also indicated that 36.2% of Singaporeans aged 18-69 are overweight. The idea of achieving a healthy lifestyle through pain, fatigue, exhaustion simply pales in comparison to more comforting thoughts of Netflix to relax and food-delivery apps for indulgence. In addition, people with LOW SELF -ESTEEM fear the risk of embarrassment when they fall short of fitness level expectations, which makes the barrier to entry even higher for these group of ppl. 
  * The use of OpenCv on Android for mobile is an acknowledgement to the fact that the mobile phones has played an increasingly significant role in our everday lives. We spend many hours of our waking time on the mobile phone, hence what better way to design an exercise app that is able to work solely with the built-in equipments that comes with our smartphone device & not require setup of specialized sensors/equipments.
  * In this project we decide to explore the usage of Camera for Exercising, instead of using special motion detection sensors such as LIDAR which us use in XBOX, KINECT, or the gyroscopes, accelerometers in our phone or fitness band which can only track a limited/specific type of motion, we want to allow/give users the freedom to be able to perform unrestricted, full range of body motions.
  *By utilizing the camera, we do not require the user to carry tracking equipment / device on their body while at the same time able to track the full range of the user's body & motion.
  
  
Libraries used: OpenCv 3.4.6
Gradle vers 3.5.0

Game development proceeded as follow:

'''
>
*1st Phase:Yellow Object Detection on each frame
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


*2nd phase: Displaying visual aids(rectangles in this case) back on the frame(screen) to the user. 

  * mark out positions of the rectangle to be displayed on the frame.
  * supply 2 coordinates, top left & bottom right coordinates pass it into rectangle() to display a rectangle on the frame.
  * Intialize and overlay an empty Mask on top of the InputFrame which we will use to perform floodfill(process of fill up a region w a particular color, sort of like coloring/shading that region)


*3rd phase: Match object in hand to position of rectangles displayed on frame (screen).

  * Implemented a Euclidean Distance algorithm that computes the distance of the object centroid with the rectangle centroid.
  * When distance between the 2 centroid meets an acceptable threshold value (self-defined acceptable radius for detection), we remove the rectangle from the display(screen), which will serve as a positive indicator to the player that he has cleared it.


*4th phase: Display 1 or 2 rectangle at different positions at different time of the game
  * Tried to use thread/Async Task/delays to keep the rectangle display on the UI for a period of 4 secs, leads to fatal crashes as Android HandlerThread class does not know which frame to since multiple frames would have coming during that period of time. 20-23 frames a sec = 80 frames in 4 sec. lifetime of frame only lasts 0.04-0.05 secs (processing input of 20-23 frames a second). Lifetime of thread/delay outlasts frame, new frames are constantly coming in and will overlay ontop of current frame. Hence even if task finish execution, we will never get to observe result since new frames are constantly received in and only the latest frame will be shown on the display screen back to the player.
  * Solution: seperate control logic(implemented in OnCreate for it to exists during the entire lifetime of application) from the image proccesing logic(to be performed on every single frame, per frame computation of 0.04 secs).
  * Use concept of flag to determine which rectangle to display at any point in time.
  * Implement a Global CountDownTimer in OnCreate Method as the control logic to switch to a different flag to activate at every fixed interval (e.g 4 secs). Loop the CountDownTimer again once it reaches zero, essentially looping it infinitely to continously display 1/2 rectangles throughout the lifetime of the application albeit in a fixed pre-defined order.
  
  
*5th phase: Escape/Exit condition for CountDownTimer with concept of Lives
  * Since the CountDownTimer loops on infinitely, in order for the game to end, the concept of Lives was conceived. 
  * At the start of the game, we initialize a value for the Lives, which also serves as a buffer, the number of rectangles that the player is allowed to miss(fail to match) in the game.
  * Whenever the player fails to match the object to a rectangle within the allocated time period, we deduct a Live.
  * Upon reaching Lives = 0, we call onPause() to pause the entire application of the game & display an alert dialog box to the player signifying that the game has ended & request the user to play the game again.
  * When player clicks on the 'ok' button, we call the recreate() method to trigger a restart for the entire Lifecycle of the application, starting all over once again from OnCreate().
  
  
*6th phase: Alter the pace of the game, to make it go faster over time
  * Set multiple Timers with shorter intervals for each passing/consecutive timer.
  
  
*7th phase: Pseudo-randomize display of rectangle location for each individual Timer
  * Tried implementing a do-while loop to ensure that a different randomly-generated index value is created to set a different flag each time. However, execution of while loop on the main thread is unable to complete its computation within the lifetime of the frame (0.04 secs time period) before the next frame arrives.
  * Self-assign order of flags for each timer, set different order.
  
  
*8th phase: Condition/Logic to switch Timer using concept of Score
  * 2 appraoches were considered when deciding how to switch between timers. The first was to fixed it, after every 4 rounds, we switch to a faster timer, however that approach is not very heuristic as it does not take into account the player's ability to cope with the pace of the game at the current pace.
  * Hence to better match the game to the player's ability, we set the conditions for switching Timer to be tag to the player's score. Each Timer is tagged to a unique range of scores, when the Player's score falls within that range, we let the Timer loop inifinitely until the player either runs out of lives or hits the maximum score within the range, and we proceed to switch to the next timer with a shorter interval (faster pace).
  
  
*9th phase: Exercise component:
  * This game is intended to be use/perform as an exercise activity. 
  * Yellow-coloured weights are provided to the players who will use it to play the game.
  * The objective of the game is to gamify the experience of exercise,
  * with the concept of lives, I hope to tap into our innate primal survival instincts.
  * and the concept of Time to add that sense of urgency to invoke the feeling/sensation of thrill/rush to the game, which can help to   alleviate the player's fatigue experienced strenuity of the game.
  * with the concept of score, we hope to encourage them to push beyond/surpass their physical limits, to outdo their previous workout score.
  * We hope that people can use the game as a motivating factor for exercising.
  * We hope to show that exercise can be performed in a fun and relax manner and not necessarily boring, uninspiring repetitive motions.
  
>
'''
