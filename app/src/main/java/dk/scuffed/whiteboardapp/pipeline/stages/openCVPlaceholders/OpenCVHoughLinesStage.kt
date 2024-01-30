package dk.scuffed.whiteboardapp.pipeline.stages.openCVPlaceholders

import android.graphics.Bitmap
import android.util.Size
import dk.scuffed.whiteboardapp.pipeline.*
import dk.scuffed.whiteboardapp.pipeline.stages.BitmapOutputStage
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.math.roundToLong


internal class OpenCVHoughLinesStage(
    private val bitmap: BitmapOutputStage,
    pipeline: Pipeline) : BitmapOutputStage(pipeline, Size(bitmap.outputBitmap.width, bitmap.outputBitmap.height), bitmap.outputBitmap.config)
{
    private var img = Mat()

    override fun update() {
        Utils.bitmapToMat(bitmap.outputBitmap, img)


        var edges = Mat(img.size(), CvType.CV_8UC1)

        Imgproc.Canny(img, edges, 80.0, 130.0)

        //var cdst: Mat = Mat()
        //Imgproc.cvtColor(input, cdst, Imgproc.COLOR_GRAY2BGR);

        var lines: Mat = Mat(img.size(), CvType.CV_8UC1)
        Imgproc.HoughLines(edges, lines, 1.0, Math.PI/180, 150); // runs the actual detection


        // Draw the lines
        // Draw the lines
        for (x in 0 until lines.rows()) {
            val rho = lines[x, 0][0]
            val theta = lines[x, 0][1]
            val a = Math.cos(theta)
            val b = Math.sin(theta)
            val x0 = a * rho
            val y0 = b * rho
            val pt1 = Point((x0 + 2000 * -b).roundToLong().toDouble(), (y0 + 2000 * a).roundToLong()
                .toDouble())
            val pt2 = Point((x0 - 2000 * -b).roundToLong().toDouble(), (y0 - 2000 * a).roundToLong()
                .toDouble())
            Imgproc.line(img, pt1, pt2, Scalar(0.0, 0.0, 255.0), 3, Imgproc.LINE_AA, 0)
        }

        Utils.matToBitmap(img, outputBitmap)
    }

}