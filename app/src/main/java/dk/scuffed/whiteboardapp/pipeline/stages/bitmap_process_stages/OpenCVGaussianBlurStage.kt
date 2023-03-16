package dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages

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
import dk.scuffed.whiteboardapp.pipeline.stages.BitmapOutputStage
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.CvType.CV_8UC3
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


/**
 * Blur bitmap using OpenCVs gaussian blur.
 */
internal class OpenCVGaussianBlurStage(
    private val bitmap: Bitmap,
    pipeline: Pipeline) : BitmapOutputStage(pipeline, Size(bitmap.width, bitmap.height), bitmap.config)
{
    private var img = Mat()

    override fun update() {
        Utils.bitmapToMat(bitmap, img)

        val blurredImg = Mat(img.size(), CvType.CV_8UC1)

        Imgproc.GaussianBlur(img, blurredImg, org.opencv.core.Size(5.0,5.0), 1.0, 1.0)

        Utils.matToBitmap(blurredImg, outputBitmap)

        img.release()
        blurredImg.release()
    }

}