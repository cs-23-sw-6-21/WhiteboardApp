# WhiteboardApp


This app has been developed as a part of our bachelor project.
The goal of this app is to digitize a whiteboard in real-time using a smartphone camera.
This has been accomplished using OpenGL ES 2.0 and 3.0, the Paddle Paddle v2 Human Segmentor and OpenCV.

The app has been written using a pipeline that uses stages to accomplish these goals.
This is to make the pipeline highly reconfigurable to quickly test new changes.
The final pipeline can be seen below.

![The final pipeline](https://github.com/cs-23-sw-6-21/WhiteboardApp/blob/main/images/image.jpg?raw=true)

We first get the image from the camrea using the CameraXStage.
Then we use the Paddle Paddle Segmenter to create a mask of where the human is and then remove the human from the image using a previous image where the human is not in the masked area.
Afterwards this is sent to corner detection to find the corners of the whiteboard.
This is done in a seperate thread as it is by far the slowest part of the pipeline.
This means that corner detection is not always completely up to date with the latest input image.
The corners from corner detection and the image from the masking step is then used in perspective correction where the whiteboard is perspective corrected and cropped to only include the whiteboard.
After the perspective correction step the image is binarized to remove smudges, glare, shadows and other unwanted stuff.
This produces a black and white image, therefore we add colors back in by using the binarized image as a mask.

Using a Samsung Galaxy S10 (2019) we get around 25 fps.
