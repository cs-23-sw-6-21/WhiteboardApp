# WhiteboardApp


This app has been developed as a part of our bachelor project.
The goal of this app is to digitize a whiteboard in real-time using a smartphone camera.
This has been accomplished using OpenGL ES 2.0 and 3.0, the Paddle Paddle v2 Human Segmentor and OpenCV.

The app has been written using a pipeline that uses stages to accomplish these goals.
This is to make the pipeline highly reconfigurable to quickly test new changes.
The final pipeline can be seen below.

![The final pipeline](https://github.com/cs-23-sw-6-21/WhiteboardApp/blob/main/images/image.jpg?raw=true)

We first get the image from the camera using the CameraXStage.
Then we use the Paddle Paddle Segmentor to create a mask of where the human is and then remove the human from the image using a previous image where the human is not in the masked area.
Afterwards this is sent to corner detection to find the corners of the whiteboard.
This is done in a separate thread as it is by far the slowest part of the pipeline.
This means that corner detection is not always completely up to date with the latest input image.
The corners from corner detection and the image from the masking step is then used in perspective correction where the whiteboard is perspective corrected and cropped to only include the whiteboard.
After the perspective correction step the image is binarized to remove smudges, glare, shadows and other unwanted stuff.
This produces a black and white image, therefore we add colors back in by using the binarized image as a mask.

The pipeline implementation can be found in the [fullPipeline file](https://github.com/cs-23-sw-6-21/WhiteboardApp/blob/main/app/src/main/java/dk/scuffed/whiteboardapp/pipeline/stage_combinations/FullPipeline.kt). 


Latency test have been made in pullrequest [#78](https://github.com/cs-23-sw-6-21/WhiteboardApp/pull/78) and while using a Samsung Galaxy S10 (2019) we got around 25 fps on the full pipeline with 1920x1080 resolution and doublebuffering enabled. 

To do the testing we recommend adjusting the pipeline to whatever stages you are interested in testing. Furthermore it may be beneficial to use the [MainThreadPipeline.kt](https://github.com/cs-23-sw-6-21/WhiteboardApp/blob/csv-timing-output/app/src/main/java/dk/scuffed/whiteboardapp/pipeline/stage_combinations/MainThreadPipeLine.kt) file, as this will run all of the stages on a single thread, which makes it easier to test.
Its all important to set the variables in the [CSVWriter.kt](https://github.com/cs-23-sw-6-21/WhiteboardApp/blob/csv-timing-output/app/src/main/java/dk/scuffed/whiteboardapp/pipeline/CSVWriter.kt) file.
These variables will allow you to set whether you're recording frame time for the stage or the overall program, if you want to run with glFinish (recommended only getting accurate frame time for the individual stages) and lastly for how many frames you want to record the data.
The double buffering and 4k parameters can also be set at the top of the [Pipeline.kt](https://github.com/cs-23-sw-6-21/WhiteboardApp/blob/csv-timing-output/app/src/main/java/dk/scuffed/whiteboardapp/pipeline/Pipeline.kt).

The middle button in the app will dissapear once the app is done with writing to the csv file.

All of the data is written to a CSV file called "Latency-Data.csv" that will be placed in the phones root folder.


## Building

Download the project and open it in android studio.
