package dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages

import android.graphics.Bitmap
import android.util.Size
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.BitmapOutputStage
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

// Does not need grayscale as input.
internal class OpenCVBinarization (
    private val bitmap: Bitmap,
    pipeline: Pipeline) : BitmapOutputStage(pipeline, Size(bitmap.width, bitmap.height), bitmap.config)
    {
        private var img = Mat(bitmap.width, bitmap.height, CvType.CV_8UC1)

        override fun update() {
            Utils.bitmapToMat(bitmap, img)

            var grayscale = Mat(img.size(), CvType.CV_8UC1)

            Imgproc.cvtColor(img, grayscale, Imgproc.COLOR_RGB2GRAY, 4)

            val binarized = Mat(img.size(), CvType.CV_8UC1)

            Imgproc.adaptiveThreshold(grayscale, binarized,255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2.0)

            Utils.matToBitmap(binarized, outputBitmap)

            img.release()
            binarized.release()
        }
}