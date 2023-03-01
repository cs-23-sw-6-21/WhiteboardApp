package dk.scuffed.whiteboardapp.pipeline.stages

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import dk.scuffed.whiteboardapp.pipeline.*
import dk.scuffed.whiteboardapp.segmentation.PPSegmentation

internal class SegmentationStage(context: Context, segmentationModel: PPSegmentation.Model,  private val inputBitmap: Bitmap, private val pipeline: Pipeline)
    : BitmapOutputStage(pipeline, Size(inputBitmap.width, inputBitmap.height), inputBitmap.config) {

    private var segmentor: PPSegmentation

    init {
        segmentor = PPSegmentation(context, segmentationModel)
    }

    override fun update() {
        outputBitmap = segmentor.segment(inputBitmap)
    }
}