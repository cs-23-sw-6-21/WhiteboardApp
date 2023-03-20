package dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages

import android.util.Log
import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.BitmapOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.LinesOutputStage
import dk.scuffed.whiteboardapp.utils.LineFloat
import dk.scuffed.whiteboardapp.utils.Vec2Float
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import kotlin.math.cos
import kotlin.math.sin


internal class OpenCVLineDetectionStage(private val bitmapOutputStage: BitmapOutputStage, private val threshold: Int, pipeline: IPipeline) : LinesOutputStage(pipeline) {

    private val inputMat = Mat()
    override fun update() {
        Utils.bitmapToMat(bitmapOutputStage.outputBitmap, inputMat)

        val grayMat = Mat(inputMat.size(), CvType.CV_8UC1)
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_RGBA2GRAY)
        inputMat.release()

        val linesMat = Mat(grayMat.size(), CvType.CV_8UC1)
        Imgproc.HoughLines(grayMat, linesMat, 1.0, Math.PI/180.0, threshold)

        lines.clear()
        for (x in 0 until linesMat.rows()) {
            val rho = linesMat.get(x, 0)[0]
            val theta = linesMat.get(x, 0)[1]
            val a = cos(theta)
            val b = sin(theta)
            val x0 = a * rho
            val y0 = b * rho
            val pt1 = Vec2Float((x0 + 2000.0f * -b).toFloat(), (y0 + 2000.0f * a).toFloat())
            val pt2 = Vec2Float((x0 - 2000.0f * -b).toFloat(), (y0 - 2000.0f * a).toFloat())

            lines.add(LineFloat(pt1, pt2))
        }

        linesMat.release()
        grayMat.release()
    }
}
