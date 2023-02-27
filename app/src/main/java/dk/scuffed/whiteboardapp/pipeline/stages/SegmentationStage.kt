package dk.scuffed.whiteboardapp.pipeline.stages

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.openGL.*
import dk.scuffed.whiteboardapp.pipeline.*
import dk.scuffed.whiteboardapp.pipeline.Stage
import dk.scuffed.whiteboardapp.segmentation.PPSegmentation
import java.nio.ByteBuffer

internal class SegmentationStage(context: Context, segmentationModel: PPSegmentation.Model,  private val inputBitmap: Bitmap, private val pipeline: Pipeline)
    : BitmapOutputStage(pipeline, Size(inputBitmap.width, inputBitmap.height), inputBitmap.config) {

    private lateinit var segmentor: PPSegmentation

    init {
        segmentor = PPSegmentation(context, segmentationModel)
    }


    override fun update() {
         outputBitmap = segmentor.Segment(inputBitmap)
    }
}