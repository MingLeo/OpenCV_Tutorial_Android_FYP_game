# FYP Interactive EXERGame w Yellow Object Detection using OpenCV

Libraries used: OpenCv 3.1.0
Gradele vers 3.5.0

Game performed in follow steps:

'''
   1st Phase: Yellow Object Detection
        - RGBA Frame
        - perform Color Segmentation in HSV color scheme using InRange(), threshold to just Yellow color range.
        - Binary Threshold mask returned detecting only the Yellow color objects
        - Perform Morphological operations (dilation & erosion) on the mask to remove any holes on the mask.
        - Perform a gaussian blur to smoothen the mask.
        - Perform canny edge detection to extract edges around the object in focus.
        - detect contous on the mask using findContours() to identify number of yellow objects detected in the frame.
        - Loop thru the contours and extract out the largest Contour using ContourArea().
        -Track only the largest object detected in the frame, which will be the object that our user will be holding on to as they perform the Exercise.
        -  Compute the moments of the largest contour to find Position Centroid, 
        - plot the centroid as a point, indicated by a circle, to display back on the frame as visual aid for the user to see.
'''


'''
    2nd phase: Displaying bitmaps on the screen at random arbitrary positions for the game. User has to match position of object held in hand to the position where the bitmap appears and scores a point when the coordinates of their location matches.
       - Load a URL resource
       - Extract the Bitmap of the URL resource
       - Run each copy of the Bitmap as an individual/independent Async Thread, bursting of the Bitmap can be burst in any particular order, no dependency, to ensure UI remains responsive!
       -
       -
       -
'''


'''
    3rd phase: Multiple Types of Game interaction!
       -
       -
       -
       -
'''