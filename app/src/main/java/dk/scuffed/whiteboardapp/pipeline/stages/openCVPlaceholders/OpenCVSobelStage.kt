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
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.CvType.CV_8UC3
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


internal class OpenCVSobelStage(
    private val bitmap: Bitmap,
    pipeline: Pipeline) : BitmapOutputStage(pipeline, Size(bitmap.width, bitmap.height), bitmap.config)
{
    private var img = Mat()

    override fun update() {
        Utils.bitmapToMat(bitmap, img)

        var sobelEdgesX = Mat(img.size(), CvType.CV_8UC1)
        var sobelEdgesY = Mat(img.size(), CvType.CV_8UC1)
        var edges = Mat(img.size(), CvType.CV_8UC1)

        Imgproc.Sobel(img, sobelEdgesX, CvType.CV_8UC1,1, 0, 3)
        Imgproc.Sobel(img, sobelEdgesY, CvType.CV_8UC1,0, 1, 3)

        Core.addWeighted(sobelEdgesX, 0.5, sobelEdgesY, 0.5, 0.0, edges)

        Utils.matToBitmap(edges, outputBitmap)
    }

}