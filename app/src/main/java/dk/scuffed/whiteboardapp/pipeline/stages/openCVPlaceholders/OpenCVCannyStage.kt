package dk.scuffed.whiteboardapp.pipeline.stages.openCVPlaceholders

import android.graphics.Bitmap
import android.util.Size
import dk.scuffed.whiteboardapp.pipeline.*
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


internal class OpenCVCannyStage(
    private val bitmap: Bitmap,
    pipeline: Pipeline) : BitmapOutputStage(pipeline, Size(bitmap.width, bitmap.height), bitmap.config)
{
    private var img = Mat()

    override fun update() {
        Utils.bitmapToMat(bitmap, img)

        var edges = Mat(img.size(), CvType.CV_8UC1)

        Imgproc.Canny(edges, edges, 80.0, 130.0)

        Utils.matToBitmap(edges, outputBitmap)
    }

}