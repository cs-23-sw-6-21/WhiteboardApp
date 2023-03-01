package dk.scuffed.whiteboardapp.pipeline.stages.openCVPlaceholders

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES11Ext.GL_BGRA
import android.opengl.GLES20
import android.opengl.GLES30.GL_PACK_ROW_LENGTH
import android.util.Log
import android.util.Size
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.pipeline.*
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.CvType.CV_8UC3
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


internal class OpenCVGrayScaleStage(
    private val bitmap: Bitmap,
    pipeline: Pipeline) : BitmapOutputStage(pipeline, Size(bitmap.width, bitmap.height), bitmap.config)
{
    private var img = Mat()

    override fun update() {
        Utils.bitmapToMat(bitmap, img)

        var edges = Mat(img.size(), CvType.CV_8UC1)

        Imgproc.cvtColor(img, edges, Imgproc.COLOR_RGB2GRAY, 4)

        Utils.matToBitmap(edges, outputBitmap)
    }

}