package dk.scuffed.whiteboardapp.pipeline.stages.openCVPlaceholders

import android.graphics.Bitmap
import android.util.Size
import dk.scuffed.whiteboardapp.pipeline.*
import dk.scuffed.whiteboardapp.pipeline.stages.BitmapOutputStage
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer


internal class OpenCVDilateStage(
    private val bitmap: BitmapOutputStage,
    pipeline: Pipeline) : BitmapOutputStage(pipeline, Size(bitmap.outputBitmap.width, bitmap.outputBitmap.height), bitmap.outputBitmap.config)
{
    private var img = Mat()

    override fun update() {
        Utils.bitmapToMat(bitmap.outputBitmap, img)

        var dilated = Mat(img.size(), CvType.CV_8UC1)
        val kernelSize = 1.0
        val elementType = Imgproc.CV_SHAPE_RECT;
        val element = Imgproc.getStructuringElement(
            elementType, org.opencv.core.Size(2 * kernelSize + 1, 2 * kernelSize + 1),
            Point(kernelSize, kernelSize)
        )

        Imgproc.dilate(img, dilated, element)
        //Imgproc.(img, blurredImg, org.opencv.core.Size(5.0,5.0), 1.0, 1.0)

        Utils.matToBitmap(dilated, outputBitmap)
    }

}