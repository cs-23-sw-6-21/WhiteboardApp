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

internal class FramebufferToBitmapStage(context: Context, private val inputFramebufferInfo: FramebufferInfo, config: Bitmap.Config, private val pipeline: Pipeline)
    : BitmapOutputStage(pipeline, Size(inputFramebufferInfo.textureSize.width, inputFramebufferInfo.textureSize.height), config) {

    override fun update() {
        readFrameBuffer(outputBitmap, inputFramebufferInfo)
    }

    private fun readFrameBuffer(bitmap: Bitmap, framebufferInfo: FramebufferInfo) {
        glActiveTexture(inputFramebufferInfo.textureUnitPair.textureUnit)
        var buffer = glReadPixels(0, 0, inputFramebufferInfo.textureSize.width, framebufferInfo.textureSize.height, GLES20.GL_RGBA)
        bitmap.copyPixelsFromBuffer(buffer)
    }
}